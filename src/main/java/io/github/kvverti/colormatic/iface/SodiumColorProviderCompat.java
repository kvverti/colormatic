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
package io.github.kvverti.colormatic.iface;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadColorProvider;
import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;

import net.minecraft.block.BlockState;

/**
 * Compatibility interface for mixing into Sodium. This interface effectively "forward declares" the methods
 * declared in {@link BlockColorsExtended} in order to prevent a non-fatal exception from being thrown within
 * mixin when Sodium is not installed.
 */
public interface SodiumColorProviderCompat {
    ModelQuadColorProvider<BlockState> getColorProvider(BlockState state);
}

@SuppressWarnings("unused")
final class EnsureCorrectSignatures implements SodiumColorProviderCompat, BlockColorsExtended {
    @Override
    public ModelQuadColorProvider<BlockState> getColorProvider(BlockState state) {
        throw new AssertionError("this class should not be instantiated");
    }
}
