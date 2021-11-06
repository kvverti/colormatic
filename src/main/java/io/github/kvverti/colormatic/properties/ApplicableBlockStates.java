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
package io.github.kvverti.colormatic.properties;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

/**
 * Class used internally by ColormapProperties. This is public so its adapter
 * can be in the adapter package.
 */
public class ApplicableBlockStates {

    public Block block = Blocks.AIR;
    public Collection<BlockState> states = new ArrayList<>();
    public Identifier specialKey = null;
    public Collection<Identifier> specialIds = new ArrayList<>();
}
