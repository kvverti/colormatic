/*
 * Colormatic
 * Copyright (C) 2019  Thalia Nero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.kvverti.colormatic.mixin.render;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.Lightmaps;
import io.github.kvverti.colormatic.resource.LightmapResource;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Provides custom lightmap update capability.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Shadow @Final private NativeImageBackedTexture texture;
    @Shadow @Final private NativeImage image;
    @Shadow private float field_21528;
    @Shadow @Final private GameRenderer worldRenderer;
    @Shadow @Final private MinecraftClient client;

    /* Relevant bytecode:
     *  17: invokeinterface #156,  2          // InterfaceMethod net/minecraft/util/profiler/Profiler.push:(Ljava/lang/String;)V
     *  22: aload_0
     *  23: getfield      #41                 // Field client:Lnet/minecraft/client/MinecraftClient;
     *  26: getfield      #160                // Field net/minecraft/client/MinecraftClient.world:Lnet/minecraft/client/world/ClientWorld;
     *  29: astore_2
     *  <injection point>
     *  30: aload_2
     *  31: ifnonnull     35     <--- if (world_1 != null)
     *  34: return
     *  35: aload_2
     */
    @Inject(
        method = "update",
        at = @At(
            value = "JUMP",
            ordinal = 1,
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void onUpdate(float partialTicks, CallbackInfo info) {
        // todo: figure out block light flicker
        if(!Colormatic.config().flickerBlockLight) {
            this.field_21528 = 0.0f;
        }
        ClientWorld world = this.client.world;
        LightmapResource map = Lightmaps.get(world.getDimension().getType());
        if(world != null && map.hasCustomColormap()) {
            int wane = Colormatic.LIGHTMAP_PROPS.getProperties().getBlockWane();
            float nightVision;
            PlayerEntity player = this.client.player;
            if(player.isInWater() && player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
                nightVision = 1.0f;
            } else if(player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                nightVision = GameRenderer.getNightVisionStrength(player, partialTicks);
            } else {
                nightVision = 0.0f;
            }
            float ambience;
            if(world.method_23789() > 0) {
                ambience = -1.0f;
            } else {
                ambience = world.method_23783(partialTicks);
                // ambience is a value between 0.2 and 1.0, inclusive.
                // we want it to be between 0.0 and 1.0, inclusive.
                // Note: the overworld ambience ranges between 0.2 and 1.0
                // depending on the time of day. The nether ambience is always
                // 0.2, and the end ambience is always 1.0.
                ambience = (ambience - 0.2f) * 1.25f;
            }
            for(int skyLight = 0; skyLight < 16; skyLight++) {
                for(int blockLight = 0; blockLight < 16; blockLight++) {
                    int trueBlockLight = blockLight;
                    if(wane < 15 && ambience >= 0) {
                        // adjust block light levels
                        int trueSkyLight = (int)(skyLight * ambience);
                        if(trueSkyLight > wane) {
                            trueBlockLight = Math.max(0, blockLight - (trueSkyLight - wane));
                        }
                    }
                    int skyColor = map.getSkyLight(skyLight, ambience, nightVision);
                    int blockColor = map.getBlockLight(trueBlockLight, this.field_21528, nightVision);
                    // color will merge the brightest channels
                    float r = (Math.max(skyColor & 0xff0000, blockColor & 0xff0000) >> 16) / 255.0f;
                    float g = (Math.max(skyColor & 0x00ff00, blockColor & 0x00ff00) >>  8) / 255.0f;
                    float b = (Math.max(skyColor & 0x0000ff, blockColor & 0x0000ff) >>  0) / 255.0f;
                    float rbright = 1.0f - r;
                    float gbright = 1.0f - g;
                    float bbright = 1.0f - b;
                    rbright *= rbright;
                    gbright *= gbright;
                    bbright *= bbright;
                    rbright *= rbright;
                    gbright *= gbright;
                    bbright *= bbright;
                    rbright = 1.0f - rbright;
                    gbright = 1.0f - gbright;
                    bbright = 1.0f - bbright;
                    float brightness = (float)this.client.options.gamma;
                    r = r * (1.0f - brightness) + rbright * brightness;
                    g = g * (1.0f - brightness) + gbright * brightness;
                    b = b * (1.0f - brightness) + bbright * brightness;
                    int color = 0xff000000;
                    color |= (int)(r * 255.0f) << 16;
                    color |= (int)(g * 255.0f) <<  8;
                    color |= (int)(b * 255.0f) <<  0;
                    this.image.setPixelRgba(blockLight, skyLight, color);
                }
            }
            // do the cleanup because we cancel the default
            this.texture.upload();
            this.client.getProfiler().pop();
            info.cancel();
        }
    }

    /**
     * Step the ambience into discrete intervals if sky light blending
     * is disabled. Only necessary for the vanilla lightmap.
     */
    @ModifyVariable(
        method = "update",
        at = @At(value = "STORE", ordinal = 0), // World#getAmbientLight
        ordinal = 1
    )
    private float modifySkyAmbience(float ambience) {
        if(!Colormatic.config().blendSkyLight) {
            ambience = (int)(ambience * 16) / 16.0f;
        }
        return ambience;
    }
}
