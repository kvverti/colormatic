/*
 * Colormatic
 * Copyright (C) 2019-2020  Thalia Nero
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
package io.github.kvverti.colormatic;

import java.util.HashMap;
import java.util.Map;

import io.github.kvverti.colormatic.colormap.Lightmap;

import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

/**
 * Container class for lightmap resource associations.
 */
public final class Lightmaps {

    private static final Map<Identifier, Lightmap> lightmaps = new HashMap<>();

    public static Lightmap get(DimensionType type) {
        return lightmaps.get(Colormatic.getDimId(type));
    }

    public static void addLightmap(Identifier id, Lightmap lightmap) {
        lightmaps.put(id, lightmap);
    }

    public static void clearLightmaps() {
        lightmaps.clear();
    }
}
