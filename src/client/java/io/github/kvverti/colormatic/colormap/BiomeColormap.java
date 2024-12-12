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
package io.github.kvverti.colormatic.colormap;

import java.util.Random;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.properties.ColormapProperties;
import io.github.kvverti.colormatic.properties.ColormapProperties.ColumnBounds;
import io.github.kvverti.colormatic.properties.HexColor;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeColormap implements ColormaticResolver {

    private static final Random GRID_RANDOM = new Random(47L);

    private final ColormapProperties properties;
    private final NativeImage colormap;
    private transient final int defaultColor;
    private transient final ExtendedColorResolver resolver;

    public BiomeColormap(ColormapProperties props, NativeImage image) {
        properties = props;
        colormap = image;
        HexColor col = props.getColor();
        if(col != null) {
            defaultColor = col.rgb();
        } else {
            defaultColor = computeDefaultColor(props);
        }
        this.resolver = new ExtendedColorResolver(this);
    }

    private int computeDefaultColor(ColormapProperties props) {
        switch(props.getFormat()) {
            case VANILLA:
                return colormap.getColor(128, 128);
            case GRID:
                try {
                    int x = 0;
                    int y = MathHelper.clamp(63 - props.getOffset(), 0, colormap.getHeight() - 1);
                    return colormap.getColor(x, y);
                } catch(IllegalArgumentException e) {
                    return 0xffffffff;
                }
            case FIXED:
                return 0xffffffff;
        }
        throw new AssertionError();
    }

    public ColormapProperties getProperties() {
        return properties;
    }

    /**
     * Returns a color given by the custom colormap for the given biome
     * temperature and humidity.
     */
    private int getColor(double temp, double rain) {
        rain *= temp;
        int x = (int)((1.0D - temp) * 255.0D);
        int y = (int)((1.0D - rain) * 255.0D);
        if(x >= colormap.getWidth() || y >= colormap.getHeight()) {
            return 0xffff00ff;
        }
        return colormap.getColor(x, y);
    }

    /**
     * Returns a color given by the custom colormap for the given biome and position.
     */
    @Override
    public int getColor(DynamicRegistryManager manager, Biome biome, int posX, int posY, int posZ) {
        switch(properties.getFormat()) {
            case VANILLA:
                double temp = biome.getTemperature();
                temp = MathHelper.clamp(temp, 0.0f, 1.0f);
                double rain = MathHelper.clamp(0.5, 0.0F, 1.0F); // todo figure out downfall
                return getColor(temp, rain);
            case GRID:
                ColumnBounds cb = properties.getColumn(Colormatic.getBiomeKey(manager, biome), manager.get(RegistryKeys.BIOME));
                // mojang uses this still so I don't know why they marked it for removal
                @SuppressWarnings("removal")
                double frac = Biome.FOLIAGE_NOISE.sample(posX * 0.0225, posZ * 0.0225, false);
                frac = (frac + 1.0) / 2; // normalize
                int x = cb.column + (int)(frac * cb.count);
                int y = posY - properties.getOffset();
                int variance = properties.getVariance();
                GRID_RANDOM.setSeed(posX * 31L + posZ);
                y += GRID_RANDOM.nextInt(variance * 2 + 1) - variance;
                x %= colormap.getWidth();
                y = MathHelper.clamp(y, 0, colormap.getHeight() - 1);
                return colormap.getColor(x, y);
            case FIXED:
                return getDefaultColor();
        }
        throw new AssertionError();
    }

    /**
     * Returns the default color given by the custom colormap.
     */
    public int getDefaultColor() {
        return defaultColor;
    }

    /**
     * Retrieves the biome coloring for the given block position, taking into
     * account the client's biome blend options If either `world` or `pos` is
     * null, this returns the colormap's default color.
     */
    public static int getBiomeColor(BlockRenderView world, BlockPos pos, BiomeColormap colormap) {
        if(world == null || pos == null) {
            return colormap.getDefaultColor();
        }
        return colormap.resolver.resolveExtendedColor(world, pos);
    }
}
