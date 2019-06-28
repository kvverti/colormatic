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
package io.github.kvverti.colormatic.mixin.color;

import net.minecraft.block.Block;
import net.minecraft.block.AttachedStemBlock;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.ExtendedBlockView;

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
            int color = BiomeColormap.getBiomeColor(world, pos, Colormatic.BIRCH_COLORS.getColormap());
            info.setReturnValue(color);
        }
    }

    @Dynamic("spruce foliage lambda method")
    @Inject(method = "method_1695", at = @At("HEAD"), cancellable = true)
    private static void onSpruceColor(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.SPRUCE_COLORS.hasCustomColormap()) {
            int color = BiomeColormap.getBiomeColor(world, pos, Colormatic.SPRUCE_COLORS.getColormap());
            info.setReturnValue(color);
        }
    }

    @Dynamic("attached stem foliage lambda method")
    @Inject(method = "method_1698", at = @At("HEAD"), cancellable = true)
    private static void onAttachedStemColor(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        Block block = state.getBlock();
        if(block == Blocks.ATTACHED_PUMPKIN_STEM && Colormatic.PUMPKIN_STEM_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.PUMPKIN_STEM_COLORS.getColorBounded(Integer.MAX_VALUE));
        } else if(block == Blocks.ATTACHED_MELON_STEM && Colormatic.MELON_STEM_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.MELON_STEM_COLORS.getColorBounded(Integer.MAX_VALUE));
        }
    }

    @Dynamic("stem foliage lambda method")
    @Inject(method = "method_1696", at = @At("HEAD"), cancellable = true)
    private static void onStemColor(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        Block block = state.getBlock();
        if(block == Blocks.PUMPKIN_STEM && Colormatic.PUMPKIN_STEM_COLORS.hasCustomColormap()) {
            int age = state.get(StemBlock.AGE);
            info.setReturnValue(Colormatic.PUMPKIN_STEM_COLORS.getColorBounded(age));
        } else if(block == Blocks.MELON_STEM && Colormatic.MELON_STEM_COLORS.hasCustomColormap()) {
            int age = state.get(StemBlock.AGE);
            info.setReturnValue(Colormatic.MELON_STEM_COLORS.getColorBounded(age));
        }
    }

    @Dynamic("Lily pad lambda method")
    @Inject(method = "method_1684", at = @At("HEAD"), cancellable = true)
    private static void onLilyPadColor(CallbackInfoReturnable<Integer> info) {
        int color = Colormatic.COLOR_PROPS.getProperties().getLilypad();
        if(color != 0) {
            info.setReturnValue(color);
        }
    }

    @Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
    private void onColorMultiplier(BlockState state, ExtendedBlockView world, BlockPos pos, int tintIdx, CallbackInfoReturnable<Integer> info) {
        Biome biome = world != null && pos != null ? world.getBiome(pos) : null;
        BiomeColormap colormap = Colormatic.CUSTOM_BLOCK_COLORS.getColormap(state, biome);
        if(colormap != null) {
            info.setReturnValue(BiomeColormap.getBiomeColor(world, pos, colormap));
        }
    }
}
