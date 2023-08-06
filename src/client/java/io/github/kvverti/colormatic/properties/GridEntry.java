/*
 * Colormatic
 * Copyright (C) 2022  Thalia Nero
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

import java.util.Collections;
import java.util.List;

import net.minecraft.util.Identifier;

/**
 * Class used internally by ColormapProperties. This is public so its adapter
 * can be in the adapter package.
 */
public class GridEntry {

    public List<Identifier> biomes = Collections.emptyList();
    public int column = -1; // auto-increment by default
    public int width = 1;
}
