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
package io.github.kvverti.colormatic.resource;

import java.util.Random;

import net.minecraft.util.Identifier;

public class LinearColormapResource extends ColormapResource {

    private final Random rand = new Random();

    public LinearColormapResource(Identifier id) {
        super(id);
    }

    /**
     * Returns the color at the given index, or at the last index if
     * greater than the length of the colormap.
     */
    public int getColorBounded(int idx) {
        if(idx >= this.colormap.length) {
            idx = this.colormap.length - 1;
        }
        return this.colormap[idx];
    }

    /**
     * Returns the color at the given index modulo the length of the
     * ccolormap.
     */
    public int getColorModulo(int idx) {
        idx %= this.colormap.length;
        return this.colormap[idx];
    }

    /**
     * Returns the color at a random index.
     */
    public int getRandomColor() {
        return this.colormap[rand.nextInt(this.colormap.length)];
    }

    /**
     * Returns the color at the index `frac` way through the colormap.
     */
    public int getColorFraction(float frac) {
        return this.colormap[(int)(frac * (this.colormap.length - 1))];
    }
}
