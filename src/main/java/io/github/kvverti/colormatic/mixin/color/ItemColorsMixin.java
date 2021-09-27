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

import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * Provides item color customization capability.
 */
@Mixin(ItemColors.class)
public abstract class ItemColorsMixin {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void onColorMultiplier(ItemStack stack, int tintIdx, CallbackInfoReturnable<Integer> info) {
        if(stack.getItem() instanceof BlockItem) {
            BlockState state = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
            if(BiomeColormaps.isItemCustomColored(state)) {
                int color = BiomeColormaps.getBiomeColor(state, null, null);
                info.setReturnValue(color);
            }
        }
    }
}
