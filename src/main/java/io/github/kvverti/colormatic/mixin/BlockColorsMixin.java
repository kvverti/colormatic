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
package io.github.kvverti.colormatic.mixin;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.biome.Biome;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Provides block color customization capability.
 */
@Mixin(BlockColors.class)
public abstract class BlockColorsMixin {

    @Dynamic("birch foliage lambda method")
    @Inject(method = "method_1687", at = @At("HEAD"), cancellable = true)
    private static void onBirchColor(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.BIRCH_COLORS.hasCustomColormap()) {
            int color;
            if(state != null && world != null) {
                Biome biome = world.getBiome(pos);
                double double_1 = (double)MathHelper.clamp(biome.getTemperature(pos), 0.0F, 1.0F);
                double double_2 = (double)MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                color = Colormatic.BIRCH_COLORS.getColor(double_1, double_2);
            } else {
                color = Colormatic.BIRCH_COLORS.getDefaultColor();
            }
            info.setReturnValue(color);
        }
    }

    @Dynamic("spruce foliage lambda method")
    @Inject(method = "method_1695", at = @At("HEAD"), cancellable = true)
    private static void onSpruceColor(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.SPRUCE_COLORS.hasCustomColormap()) {
            int color;
            if(state != null && world != null) {
                Biome biome = world.getBiome(pos);
                double double_1 = (double)MathHelper.clamp(biome.getTemperature(pos), 0.0F, 1.0F);
                double double_2 = (double)MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                color = Colormatic.SPRUCE_COLORS.getColor(double_1, double_2);
            } else {
                color = Colormatic.SPRUCE_COLORS.getDefaultColor();
            }
            info.setReturnValue(color);
        }
    }
}
