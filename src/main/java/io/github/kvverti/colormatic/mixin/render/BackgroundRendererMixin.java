/*
 * Colormatic
 * Copyright (C) 2019-2020  Thalia Nero
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
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

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

    @Unique
    private static int storedFogColor;

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    private static void storeCustomFogColor(Camera camera, float partialTicks, ClientWorld world, int i, float f, CallbackInfo info) {
        DimensionType dimType = world.getDimension();
        BlockPos pos = camera.getBlockPos();
        if(Colormatic.config().clearSky && dimType.hasSkyLight()) {
            storedFogColor = 0;
            return;
        }
        int color = Colormatic.COLOR_PROPS.getProperties().getDimensionFog(dimType);
        if(BiomeColormaps.isSkyFogCustomColored(dimType)) {
            color = 0xff000000 | BiomeColormaps.getSkyFogColor(dimType, world, pos);
        } else if(Colormatic.FOG_COLORS.hasCustomColormap() && Colormatic.getDimId(dimType) == DimensionType.OVERWORLD_ID) {
            // overworld colors fog by biome
            color = 0xff000000 | BiomeColormap.getBiomeColor(
                world,
                pos,
                Colormatic.FOG_COLORS.getColormap());
        }
        storedFogColor = color;
    }

    @Dynamic("RgbFetcher lambda method in #render")
    @Redirect(
        method = "method_24873",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getFogColor()I"
        )
    )
    private static int proxyCustomFogColor(Biome self) {
        return storedFogColor != 0 ? storedFogColor : self.getFogColor();
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
            Vec3d color = world.method_23777(camera.getBlockPos(), partialTicks);
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
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 0
    )
    private static double modifyVoidColor(double scale) {
        if(Colormatic.config().clearVoid) {
            scale = 1.0;
        }
        return scale;
    }

    /* Relevant bytecode:
     *  50: ldc           #122                // float 0.6f
     *  52: putfield      #124                // Field red:F
     *  55: aload_0
     *  56: ldc           #125                // float 0.1f
     *  58: putfield      #127                // Field green:F
     *  61: aload_0
     *  62: fconst_0
     *  63: putfield      #129                // Field blue:F
     *  <injection point>
     *  66: aload_0
     *  67: ldc2_w        #60                 // long -1l
     *  70: putfield      #63                 // Field lastWaterFogColorUpdateTime:J
     */
    @Inject(
        method = "render",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/tag/FluidTags;LAVA:Lnet/minecraft/tag/Tag$Identified;"
            )
        ),
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/BackgroundRenderer;blue:F",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private static void onRenderLavaFog(Camera camera, float partialTicks, ClientWorld world, int int1, float float1, CallbackInfo info) {
        if(BiomeColormaps.isFluidFogCustomColored(Fluids.LAVA)) {
            int color = BiomeColormaps.getFluidFogColor(Fluids.LAVA, world, camera.getBlockPos());
            BackgroundRendererMixin.red = ((color >> 16) & 0xff) / 255.0f;
            BackgroundRendererMixin.green = ((color >> 8) & 0xff) / 255.0f;
            BackgroundRendererMixin.blue = ((color >> 0) & 0xff) / 255.0f;
        } else if(Colormatic.UNDERLAVA_COLORS.hasCustomColormap()) {
            int color = BiomeColormap.getBiomeColor(
                world,
                camera.getBlockPos(),
                Colormatic.UNDERLAVA_COLORS.getColormap());
            BackgroundRendererMixin.red = ((color >> 16) & 0xff) / 255.0f;
            BackgroundRendererMixin.green = ((color >> 8) & 0xff) / 255.0f;
            BackgroundRendererMixin.blue = ((color >> 0) & 0xff) / 255.0f;
        }
    }
}
