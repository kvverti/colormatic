/*
 * Colormatic
 * Copyright (C) 2019  Thalia Nero
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
package io.github.kvverti.colormatic.resource;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class BiomeColormapResource extends ColormapResource {

    public BiomeColormapResource(Identifier id) {
        super(id);
    }

    /**
     * Returns a color given by the custom colormap for the given biome
     * temperature and humidity.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    private int getColor(double temp, double rain) {
        if(this.colormap == null) {
            throw new IllegalStateException("No custom colormap present: " + this.getFabricId());
        }
        rain *= temp;
        int x = (int)((1.0D - temp) * 255.0D);
        int y = (int)((1.0D - rain) * 255.0D);
        int idx = y << 8 | x;
        return idx > this.colormap.length ? 0xffff00ff : this.colormap[idx];
    }

    /**
     * Returns a color given by the custom colormap for the given biome.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    public int getColor(Biome biome) {
        double temp = MathHelper.clamp(biome.getTemperature(), 0.0F, 1.0F);
        double rain = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return getColor(rain, temp);
    }

    /**
     * Returns a color given by the custom colormap for the given biome and
     * BlockPos.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    public int getColor(Biome biome, BlockPos pos) {
        double temp = MathHelper.clamp(biome.getTemperature(pos), 0.0F, 1.0F);
        double rain = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return getColor(rain, temp);
    }

    /**
     * Returns the default color given by the custom colormap.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource.
     */
    public int getDefaultColor() {
        if(this.colormap == null) {
            throw new IllegalStateException("No custom colormap present: " + this.getFabricId());
        }
        return this.colormap[(128 << 8) | 128];
    }
}
