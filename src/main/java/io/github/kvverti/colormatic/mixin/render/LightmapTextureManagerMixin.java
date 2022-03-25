/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As an additional permission, when conveying the Corresponding Source of an
 * object code form of this work, you may exclude the Corresponding Source for
 * "Minecraft" by Mojang Studios, AB.
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
import io.github.kvverti.colormatic.ColormaticConfig;
import io.github.kvverti.colormatic.Lightmaps;
import io.github.kvverti.colormatic.colormap.Lightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Provides custom lightmap update capability.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    @Shadow
    @Final
    private NativeImage image;

    @Shadow
    private float flickerIntensity;

    @Shadow
    @Final
    private MinecraftClient client;

    // Vanilla block light flicker calculation is no longer compatible
    // with Colormatic (as of 1.15)

    /**
     * The current flicker target, in the range [0.0, 1.0).
     */
    @Unique
    private float flickerTarget;

    /**
     * The current flicker position, in the range [0.0, 1.0).
     */
    @Unique
    private float flickerPos;

    /**
     * How many ticks until the next flicker target is calculated
     */
    @Unique
    private int flickerTicksRemaining;

    // relative block light intensity fields

    /**
     * The exponent scale for block light relative intensity.
     */
    @Unique
    private double relativeIntensityExpScale;

    /**
     * Stored light index as 0bSSSSBBBB, in sync with the loops over sky and block light levels.
     */
    @Unique
    private int lightIndex = 0;

    @Unique
    private final int[] SKY_LIGHT_COLORS = new int[16];

    @Unique
    private final int[] BLOCK_LIGHT_COLORS = new int[16];

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickTickFlicker(CallbackInfo info) {
        if(Colormatic.config().flickerBlockLight) {
            // Compute the next flicker target if there are no
            // more ticks remaining, then (regardless) set the
            // current flicker pos closer to the flicker target
            if(flickerTicksRemaining == 0) {
                // ticks between setting flicker targets
                flickerTicksRemaining = 4;
                flickerTarget = (float)Math.random();
            }
            // interpolate between the flickerPos and flickerTarget
            flickerPos = MathHelper.lerp(1.0f / flickerTicksRemaining, flickerPos, flickerTarget);
            flickerTicksRemaining--;
        } else {
            flickerPos = 0.0f;
            // set the vanilla flicker indicator to zero as <well></well>
            // (this is why the injection is at RETURN and not HEAD)
            this.flickerIntensity = 0.0f;
        }
    }

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
        ClientWorld world = this.client.world;
        if(world == null) {
            return;
        }
        // ambience is a value between 0.2 and 1.0, inclusive.
        // we want it to be between 0.0 and 1.0, inclusive.
        // Note: the overworld ambience ranges between 0.2 and 1.0
        // depending on the time of day. The nether ambience is always
        // 0.2, and the end ambience is always 1.0.
        float ambience = (world.getStarBrightness(partialTicks) - 0.2f) * 1.25f;
        // relative intensity curve = exp2(ax)
        relativeIntensityExpScale = ambience * ColormaticConfig.scaled(Colormatic.config().relativeBlockLightIntensityExponent) / 16.0;
        // set this to -1.0 to signal a lightning strike. Relative intensity still follows the normal ambience.
        if(world.getLightningTicksLeft() > 0) {
            ambience = -1.0f;
        }
        Lightmap map = Lightmaps.get(world);
        if(map != null) {
            float nightVision;
            PlayerEntity player = this.client.player;
            if(player.isSubmergedInWater() && player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
                nightVision = 1.0f;
            } else if(player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                nightVision = GameRenderer.getNightVisionStrength(player, partialTicks);
            } else {
                nightVision = 0.0f;
            }
            for(int i = 0; i < 16; i++) {
                SKY_LIGHT_COLORS[i] = map.getSkyLight(i, ambience, nightVision);
                BLOCK_LIGHT_COLORS[i] = map.getBlockLight(i, flickerPos, nightVision);
            }
            for(int skyLight = 0; skyLight < 16; skyLight++) {
                float blockIntensityScale = (float)Math.exp(relativeIntensityExpScale * skyLight);
                for(int blockLight = 0; blockLight < 16; blockLight++) {
                    int skyColor = SKY_LIGHT_COLORS[skyLight];
                    int blockColor = BLOCK_LIGHT_COLORS[blockLight];
                    // color will add the channels and cap at white
                    float scale = blockLight == 15 ? 1 : blockIntensityScale;
                    float r = Math.min(255.0f, ((skyColor & 0xff0000) >> 16) + scale * ((blockColor & 0xff0000) >> 16)) / 255.0f;
                    float g = Math.min(255.0f, ((skyColor & 0xff00) >> 8) + scale * ((blockColor & 0xff00) >> 8)) / 255.0f;
                    float b = Math.min(255.0f, (skyColor & 0xff) + scale * (blockColor & 0xff)) / 255.0f;
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
                    float brightnessAdditive = brightness > 1.0f ? 0 : (1.0f - brightness);
                    r = r * brightnessAdditive + rbright * brightness;
                    g = g * brightnessAdditive + gbright * brightness;
                    b = b * brightnessAdditive + bbright * brightness;
                    int color = 0xff000000;
                    color |= (int)(r * 255.0f) << 16;
                    color |= (int)(g * 255.0f) << 8;
                    color |= (int)(b * 255.0f);
                    this.image.setColor(blockLight, skyLight, color);
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

    /**
     * Scale the vanilla block light intensity by the relative intensity.
     */
    @ModifyVariable(
        method = "update",
        at = @At(
            value = "STORE",
            ordinal = 0
        ),
        index = 13
    )
    private float modifyFlickerIntensity(float blockLight) {
        int sky = lightIndex >>> 4;
        int block = lightIndex & 0b1111;
        lightIndex = (lightIndex + 1) & 0xff;
        if(block != 15) {
            return blockLight * (float)Math.exp(relativeIntensityExpScale * sky);
        }
        return blockLight;
    }
}
