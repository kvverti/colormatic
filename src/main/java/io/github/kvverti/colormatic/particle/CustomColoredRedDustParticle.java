/*
 * Colormatic
 * Copyright (C) 2019-2021  Thalia Nero
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
package io.github.kvverti.colormatic.particle;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3f;

/**
 * A redstone dust particle that takes its color from the custom colors
 * defined in resource packs.
 */
public class CustomColoredRedDustParticle extends DustParticleEffect {

    public CustomColoredRedDustParticle(Vec3f color, float a) {
        super(color, a);
    }

    @Override
    public Vec3f getColor() {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            var rgb = getFullColor();
            var r = ((rgb >> 16) & 0xff) / 255.0f;
            var g = ((rgb >> 8) & 0xff) / 255.0f;
            var b = (rgb & 0xff) / 255.0f;
            return new Vec3f(r, g, b);
        }
        return super.getColor();
    }

    @Override
    public float getScale() {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            return ((getFullColor() >> 24) & 0xff) / 255.0f;
        }
        return super.getScale();
    }

    private int getFullColor() {
        return Colormatic.REDSTONE_COLORS.getColorBounded(15);
    }
}
