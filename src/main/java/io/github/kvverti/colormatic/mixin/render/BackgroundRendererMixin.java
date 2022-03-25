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
package io.github.kvverti.colormatic.mixin.render;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;
import io.github.kvverti.colormatic.resource.BiomeColormapResource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;

/**
 * Provides air and underlava fog color and customization capability.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    // shadow members

    @Shadow
    private static float red;
    @Shadow
    private static float green;
    @Shadow
    private static float blue;

    /**
     * If Colormatic has a color for lava, redirect the submersion type to water so we can reuse water
     * fog blending.
     */
    @ModifyVariable(
        method = "render",
        at = @At(
            value = "LOAD",
            ordinal = 0
        ),
        ordinal = 0
    )
    private static CameraSubmersionType detectShouldColormaticFluidBlend(CameraSubmersionType submersionType) {
        if(submersionType == CameraSubmersionType.LAVA) {
            if(BiomeColormaps.isFluidFogCustomColored(Fluids.LAVA) || Colormatic.UNDERLAVA_COLORS.hasCustomColormap()) {
                return CameraSubmersionType.WATER;
            }
        }
        return submersionType;
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getWaterFogColor()I"
        )
    )
    private static int proxyWaterLavaFogColor(Biome biome, Camera camera, float tickDelta, ClientWorld world, int i, float f) {
        Fluid fluid;
        BiomeColormapResource colormapResource;
        var submersionType = camera.getSubmersionType();
        if(submersionType == CameraSubmersionType.LAVA) {
            fluid = Fluids.LAVA;
            colormapResource = Colormatic.UNDERLAVA_COLORS;
        } else {
            fluid = Fluids.WATER;
            colormapResource = Colormatic.UNDERWATER_COLORS;
        }
        int color = 0;
        if(BiomeColormaps.isFluidFogCustomColored(fluid)) {
            BiomeColormap colormap = BiomeColormaps.getFluidFog(world.getRegistryManager(), fluid, biome);
            if(colormap != null) {
                BlockPos pos = camera.getBlockPos();
                color = colormap.getColor(world.getRegistryManager(), biome, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        if(color == 0) {
            if(colormapResource.hasCustomColormap()) {
                BlockPos pos = camera.getBlockPos();
                color = colormapResource
                    .getColormap()
                    .getColor(world.getRegistryManager(), biome, pos.getX(), pos.getY(), pos.getZ());
            } else {
                if(submersionType == CameraSubmersionType.LAVA) {
                    color = 0x991900;
                } else {
                    color = biome.getWaterFogColor();
                }
            }
        }
        return color;
    }

    @Dynamic("RgbFetcher lambda method in #render")
    @Redirect(
        method = "method_24873",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getFogColor()I"
        )
    )
    private static int proxyCustomFogColor(Biome self, ClientWorld world, BiomeAccess access, float angleDelta, int x, int y, int z) {
        if(Colormatic.config().clearSky && world.getDimension().hasSkyLight()) {
            return self.getFogColor();
        }
        var dimId = Colormatic.getDimId(world);
        ColormaticResolver resolver = BiomeColormaps.getTotalSkyFog(dimId);
        return resolver.getColor(world.getRegistryManager(), self, BiomeCoords.toBlock(x), BiomeCoords.toBlock(y), BiomeCoords.toBlock(z));
    }

    /**
     * Set the fog color to exactly the same color as the sky when clear skies are enabled.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/BackgroundRenderer;blue:F",
            opcode = Opcodes.PUTSTATIC,
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"
            )
        )
    )
    private static void setFogColorToSkyColor(Camera camera, float partialTicks, ClientWorld world, int i, float f, CallbackInfo info) {
        if(Colormatic.config().clearSky && world.getDimension().hasSkyLight()) {
            Vec3d color = world.getSkyColor(camera.getPos(), partialTicks);
            BackgroundRendererMixin.red = (float)color.x;
            BackgroundRendererMixin.green = (float)color.y;
            BackgroundRendererMixin.blue = (float)color.z;
        }
    }

    @Unique
    private static float redStore;

    @Unique
    private static float greenStore;

    @Unique
    private static float blueStore;

    /**
     * Save old colors before rain and thunder is applied when clear skies
     * are enabled. World#getSkyColor does this for us.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"
        )
    )
    private static void saveColorsToRestRainAndThunder(CallbackInfo info) {
        if(Colormatic.config().clearSky) {
            redStore = BackgroundRendererMixin.red;
            greenStore = BackgroundRendererMixin.green;
            blueStore = BackgroundRendererMixin.blue;
        }
    }

    /**
     * Reset colors after rain and thunder are applied if clear skies are
     * enabled.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/BackgroundRenderer;lastWaterFogColorUpdateTime:J"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"
            )
        )
    )
    private static void resetRainAndThunderColors(CallbackInfo info) {
        if(Colormatic.config().clearSky) {
            BackgroundRendererMixin.red = redStore;
            BackgroundRendererMixin.green = greenStore;
            BackgroundRendererMixin.blue = blueStore;
        }
    }

    /**
     * When clear void is enabled, prevent black fog when in the void.
     */
    @ModifyVariable(
        method = "render",
        at = @At(value = "STORE", ordinal = 2),
        index = 7
    )
    private static float modifyVoidColor(float scale) {
        if(Colormatic.config().clearVoid) {
            scale = 1.0f;
        }
        return scale;
    }
}
