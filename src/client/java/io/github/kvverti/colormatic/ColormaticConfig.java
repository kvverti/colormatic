/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
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
package io.github.kvverti.colormatic;

public final class ColormaticConfig {

    public boolean clearSky = false;

    public boolean clearVoid = false;

    public boolean blendSkyLight = true;

    public boolean flickerBlockLight = true;

    public double relativeBlockLightIntensityExponent = -13.0;

    public static double scaled(double relativeBlockLightIntensityExponent) {
        final double LOG_2 = 0.69314718056;
        return LOG_2 * 0.25 * relativeBlockLightIntensityExponent;
    }
}
