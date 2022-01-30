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
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;

@Mixin(value = BlockColors.class, priority = 2000)
@Implements(@Interface(iface = BlockColorsExtended.class, prefix = "i$", remap = Interface.Remap.NONE))
public abstract class SodiumBlockColorsMixin implements BlockColorsExtended {

    @Unique
    private static final ColorSampler<BlockState> COLORMATIC_PROVIDER =
        (state, world, pos, tintIndex) -> BiomeColormaps.getBiomeColor(state, world, pos);

    /**
     * Displace Sodium's implementation to first check Colormatic's custom block colors.
     */
    @Intrinsic(displace = true)
    public ColorSampler<BlockState> i$getColorProvider(BlockState state) {
        if(BiomeColormaps.isCustomColored(state)) {
            return COLORMATIC_PROVIDER;
        }
        return this.getColorProvider(state);
    }
}
