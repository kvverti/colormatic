/*
 * Colormatic
 * Copyright (C) 2024  Thalia Nero
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
package io.github.kvverti.colormatic.mixinsodium.color;

import java.util.Arrays;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {

    @Unique
    private static final ColorProvider<FluidState> COLORMATIC_PROVIDER = FluidRendererMixin::getColormaticFluidColors;

    /**
     * Redirect Sodium's fluid renderer to Colormatic fluid colors.
     */
    @WrapMethod(method = "getColorProvider(Lnet/minecraft/fluid/Fluid;Lnet/fabricmc/fabric/api/client/render/fluid/v1/FluidRenderHandler;)Lme/jellysquid/mods/sodium/client/model/color/ColorProvider;")
    private ColorProvider<FluidState> proxyColormaticFluidColorProvider(Fluid fluid, FluidRenderHandler handler, Operation<ColorProvider<FluidState>> original) {
        if(BiomeColormaps.isCustomColored(fluid.getDefaultState().getBlockState())) {
            return COLORMATIC_PROVIDER;
        }
        return original.call(fluid, handler);
    }

    @Unique
    private static void getColormaticFluidColors(WorldSlice world, BlockPos pos, FluidState state, ModelQuadView quadView, int[] out) {
        int color = BiomeColormaps.getBiomeColor(state.getBlockState(), world, pos);
        Arrays.fill(out, ColorARGB.toABGR(color));
    }
}
