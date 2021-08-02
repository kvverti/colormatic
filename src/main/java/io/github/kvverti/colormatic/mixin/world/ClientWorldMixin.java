/*
 * Colormatic
 * Copyright (C) 2021  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.world;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.colormap.ExtendedColorResolver;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ColorResolver;

/**
 * Provides global sky color customization capability.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    @Shadow
    @Final
    private Object2ObjectArrayMap<ColorResolver, BiomeColorCache> colorCache;

    private ClientWorldMixin() {
        super(null, null, null, null, false, false, 0L);
    }

    @Redirect(
        method = "method_23777",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    private Vec3d proxySkyColor(Vec3d noisePos, CubicSampler.RgbFetcher fetcher, Vec3d pos, float partial) {
        BlockPos blockPos = new BlockPos(pos);
        int skyColor;
        if(BiomeColormaps.isSkyCustomColored(this)) {
            skyColor = BiomeColormaps.getSkyColor(this, blockPos);
        } else if(Colormatic.SKY_COLORS.hasCustomColormap() && Colormatic.getDimId(this).equals(DimensionType.OVERWORLD_ID)) {
            BiomeColormap colormap = Colormatic.SKY_COLORS.getColormap();
            skyColor = BiomeColormap.getBiomeColor(this, blockPos, colormap);
        } else {
            skyColor = Colormatic.COLOR_PROPS.getProperties().getDimensionSky(this);
        }
        if(skyColor != 0) {
            var skyColorVec = Vec3d.unpackRgb(skyColor);
            return CubicSampler.sampleColor(noisePos, (x, y, z) -> skyColorVec);
        } else {
            return CubicSampler.sampleColor(noisePos, fetcher);
        }
    }

    /**
     * Vanilla doesn't check if the color cache exists before retrieving it. We fix this here.
     */
    @Inject(method = "getColor", at = @At("HEAD"))
    private void fixVanillaColorCache(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> info) {
        // todo: cache vertically differentiated color properly. Apparently broken since 1.16.x
        if(this.colorCache.get(resolver) == null) {
            this.colorCache.put(resolver, new BiomeColorCache());
        }
    }

    /**
     * Reset custom colors for the entire world. We remove caches for Colormatic's custom resolvers, as their
     * identities are not consistent.
     */
    @Inject(method = "reloadColor", at = @At("RETURN"))
    private void reloadColormaticColor(CallbackInfo info) {
        this.colorCache.entrySet().removeIf(entry -> entry.getKey() instanceof ExtendedColorResolver);
    }
}
