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
package io.github.kvverti.colormatic.colormap;

import net.minecraft.util.registry.Registry;
import io.github.kvverti.colormatic.properties.ColormapProperties;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class BiomeColormap {

    private final ColormapProperties properties;
    private final NativeImage colormap;

    public BiomeColormap(ColormapProperties props, NativeImage image) {
        properties = props;
        colormap = image;
    }

    /**
     * Returns a color given by the custom colormap for the given biome
     * temperature and humidity.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    private int getColor(double temp, double rain) {
        rain *= temp;
        int x = (int)((1.0D - temp) * 255.0D);
        int y = (int)((1.0D - rain) * 255.0D);
        if(x >= colormap.getWidth() || y >= colormap.getHeight()) {
            return 0xffff00ff;
        }
        return colormap.getPixelRGBA(x, y);
    }

    /**
     * Returns a color given by the custom colormap for the given biome.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    public int getColor(Biome biome) {
        return getColor(biome, null);
    }

    /**
     * Returns a color given by the custom colormap for the given biome and
     * BlockPos.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    public int getColor(Biome biome, BlockPos pos) {
        switch(properties.getFormat()) {
            case VANILLA:
                double temp = pos == null ? biome.getTemperature() : biome.getTemperature(pos);
                temp = MathHelper.clamp(temp, 0.0f, 1.0f);
                double rain = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                return getColor(rain, temp);
            case GRID:
                int x = Registry.BIOME.getRawId(biome) % colormap.getWidth();
                int y = pos.getY() + properties.getOffset();
                y = MathHelper.clamp(y, 0, colormap.getHeight());
                return colormap.getPixelRGBA(x, y);
            case FIXED:
                return getDefaultColor();
        }
        throw new AssertionError();
    }

    /**
     * Returns the default color given by the custom colormap.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource.
     */
    public int getDefaultColor() {
        return properties.getColor();
    }
}
