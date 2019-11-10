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

import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.properties.PseudoBlockStates;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Lazy;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Provides air and underlava fog color and customization capability.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    // shadow members

    @Shadow private static float red;
    @Shadow private static float green;
    @Shadow private static float blue;

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;method_23786(F)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    private static Vec3d proxyFogColor(ClientWorld self, float partialTicks, Camera camera, float foo1, ClientWorld self2, int foo2, float partialTicks2) {
        Dimension dim = self.getDimension();
        if(Colormatic.config().clearSky && dim.hasVisibleSky()) {
            return self.method_23777(camera.getBlockPos(), partialTicks);
        }
        DimensionType dimType = dim.getType();
        int color = Colormatic.COLOR_PROPS.getProperties().getDimensionFog(dimType);
        BlockState state = PseudoBlockStates.SKY_FOG.getDefaultState()
            .with(PseudoBlockStates.DIMENSION, Registry.DIMENSION.getId(dimType));
        if(BiomeColormaps.isCustomColored(state)) {
            color = 0xff000000 | BiomeColormaps.getBiomeColor(state, self, camera.getBlockPos());
        } else if(dimType == DimensionType.OVERWORLD && Colormatic.FOG_COLORS.hasCustomColormap()) {
            // overworld colors fog by biome
            color = 0xff000000 | BiomeColormap.getBiomeColor(
                self,
                camera.getBlockPos(),
                Colormatic.FOG_COLORS.getColormap());
        }
        if(color != 0) {
            double r = ((color >> 16) & 0xff) / 255.0;
            double g = ((color >>  8) & 0xff) / 255.0;
            double b = ((color >>  0) & 0xff) / 255.0;
            // time-of-day calculations, assumes typical day-night cycle
            // (i.e. time 6000 = noon and 18000 = midnight)
            if(dimType.hasSkyLight()) {
                float daytimeAngle = self.getSkyAngle(partialTicks);
                float float_3 = MathHelper.cos(daytimeAngle * 2.0f * (float)Math.PI) * 2.0F + 0.5F;
                float_3 = MathHelper.clamp(float_3, 0.0F, 1.0F);
                r *= float_3 * 0.94F + 0.06F;
                g *= float_3 * 0.94F + 0.06F;
                b *= float_3 * 0.91F + 0.09F;
            }
            return new Vec3d(r, g, b);
        } else {
            return self.method_23786(partialTicks);
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

    @Unique
    private static final Lazy<BlockState> LAVA_FOG = new Lazy<>(() ->
        PseudoBlockStates.FLUID_FOG.getDefaultState()
        .with(PseudoBlockStates.FLUID, Registry.FLUID.getId(Fluids.LAVA)));

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
                target = "Lnet/minecraft/tag/FluidTags;LAVA:Lnet/minecraft/tag/Tag;"
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
        if(BiomeColormaps.isCustomColored(LAVA_FOG.get())) {
            int color = BiomeColormaps.getBiomeColor(LAVA_FOG.get(), world, camera.getBlockPos());
            BackgroundRendererMixin.red = ((color >> 16) & 0xff) / 255.0f;
            BackgroundRendererMixin.green = ((color >>  8) & 0xff) / 255.0f;
            BackgroundRendererMixin.blue = ((color >>  0) & 0xff) / 255.0f;
        } else if(Colormatic.UNDERLAVA_COLORS.hasCustomColormap()) {
            int color = BiomeColormap.getBiomeColor(
                world,
                camera.getBlockPos(),
                Colormatic.UNDERLAVA_COLORS.getColormap());
            BackgroundRendererMixin.red = ((color >> 16) & 0xff) / 255.0f;
            BackgroundRendererMixin.green = ((color >>  8) & 0xff) / 255.0f;
            BackgroundRendererMixin.blue = ((color >>  0) & 0xff) / 255.0f;
        }
    }
}
