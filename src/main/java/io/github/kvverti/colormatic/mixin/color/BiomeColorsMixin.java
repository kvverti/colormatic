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
package io.github.kvverti.colormatic.mixin.color;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * Provides water color customization capability.
 */
@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {

    /**
     * The FluidRenderer (including the fluid rendering APIs) calls BiomeColors#getWaterColor directly, instead of
     * going through BlockColors. So, we must check for provided custom water color here instead of in BiomeColors.
     * Note that we already apply custom biome colors to fluids via
     * {@link io.github.kvverti.colormatic.iface.ColormaticFluidRenderHandler}. Also note that the provided water
     * colors apply to water cauldrons in addition to the water fluid.
     */
    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private static void onWaterColorPre(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.WATER_COLORS.hasCustomColormap()) {
            BiomeColormap colormap = Colormatic.WATER_COLORS.getColormap();
            info.setReturnValue(BiomeColormap.getBiomeColor(world, pos, colormap));
        }
    }
}
