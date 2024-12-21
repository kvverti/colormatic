/*
 * Colormatic
 * Copyright (C) 2024  Thalia Nero
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

/**
 * Exposes what the sky color of a Biome would be without Colormatic customization.
 */
public interface DefaultSkyColorAccess {
    /**
     * Get the vanilla sky color for this biome. Colormatic fallback
     * code should call this instead of getSkyColor to avoid recursion.
     */
    int colormatic$getDefaultSkyColor();

    /**
     * Get the vanilla fog color for this biome. Colormatic fallback
     * code should call this instead of getFogColor to avoid recursion.
     */
    int colormatic$getDefaultFogColor();
}
