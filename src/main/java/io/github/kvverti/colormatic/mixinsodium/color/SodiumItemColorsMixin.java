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
package io.github.kvverti.colormatic.mixinsodium.color;

import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import me.jellysquid.mods.sodium.client.world.biome.ItemColorsExtended;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(value = ItemColors.class, priority = 2000)
@Implements(@Interface(iface = ItemColorsExtended.class, prefix = "i$", remap = Interface.Remap.NONE))
public abstract class SodiumItemColorsMixin implements ItemColorsExtended {

    @Unique
    private static final ItemColorProvider COLORMATIC_PROVIDER =
        (stack, tintIndex) -> BiomeColormaps.getBiomeColor(((BlockItem)stack.getItem()).getBlock().getDefaultState(), null, null);

    /**
     * Displace Sodium's implementation to first check Colormatic's custom item colors.
     */
    @Intrinsic(displace = true)
    public ItemColorProvider i$getColorProvider(ItemStack stack) {
        if(stack.getItem() instanceof BlockItem blockItem) {
            if(BiomeColormaps.isItemCustomColored(blockItem.getBlock().getDefaultState())) {
                return COLORMATIC_PROVIDER;
            }
        }
        return this.getColorProvider(stack);
    }
}
