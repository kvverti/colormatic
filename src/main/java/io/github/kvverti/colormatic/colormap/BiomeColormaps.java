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

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import java.util.Set;
import io.github.kvverti.colormatic.properties.ColormapProperties;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.OceanBiome;

/**
 * Class that provides efficient access to biome colors on
 * a per block state basis. These methods are usually called multiple
 * times each frame.
 */
public final class BiomeColormaps {

    /**
     * Marker object to represent all biomes.
     */
    private static final Biome ALL = new OceanBiome();

    /**
     * Stores colormaps primarily by block, and secondarily by biome. Colormaps
     * that apply to all biomes are stored under the key ALL.
     */
    private static final Table<Block, Biome, BiomeColormap> colormaps = HashBasedTable.create();

    /**
     * Stores colormaps primarily by block state, and secondarily by biome.
     * Colormaps that apply to all biomes are stored under the key ALL.
     */
    private static final Table<BlockState, Biome, BiomeColormap> colormapsByState = HashBasedTable.create();

    private BiomeColormaps() {}

    /**
     * Retrieves the colormap that applies to the given block state and biome.
     * Returns `null` if there are no colormaps that apply.
     */
    private static BiomeColormap get(BlockState state, Biome biome) {
        Map<Biome, BiomeColormap> map = colormapsByState.row(state);
        BiomeColormap res = map.get(biome);
        if(res == null) {
            res = map.get(ALL);
        }
        if(res == null) {
            map = colormaps.row(state.getBlock());
            res = map.get(biome);
        }
        if(res == null) {
            res = map.get(ALL);
        }
        return res;
    }

    public static void add(BiomeColormap colormap) {
        ColormapProperties props = colormap.getProperties();
        Set<Biome> biomes = props.getApplicableBiomes();
        if(biomes.isEmpty()) {
            for(BlockState state : props.getApplicableBlockStates()) {
                colormapsByState.put(state, ALL, colormap);
            }
            for(Block block : props.getApplicableBlocks()) {
                colormaps.put(block, ALL, colormap);
            }
        } else {
            for(Biome b : biomes) {
                for(BlockState state : props.getApplicableBlockStates()) {
                    colormapsByState.put(state, b, colormap);
                }
                for(Block block : props.getApplicableBlocks()) {
                    colormaps.put(block, b, colormap);
                }
            }
        }
    }

    public static void reset() {
        colormaps.clear();
        colormapsByState.clear();
    }

    /**
     * Returns whether the given state has any custom colormaps.
     */
    public static boolean isCustomColored(BlockState state) {
        return !colormaps.row(state.getBlock()).isEmpty() ||
            !colormapsByState.row(state).isEmpty();
    }

    /**
     * Retrieves the biome coloring for the given block position, taking into
     * account the client's biome blend options.
     */
    public static int getBiomeColor(BlockState state, ExtendedBlockView world, BlockPos pos) {
        if(world == null || pos == null) {
            // todo figure out held item colors
            BiomeColormap colormap = get(state, ALL);
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
        int r = 0;
        int g = 0;
        int b = 0;
        int radius = MinecraftClient.getInstance().options.biomeBlendRadius;
        Iterable<BlockPos> coll = BlockPos.iterate(
            pos.getX() - radius, pos.getY(), pos.getZ() - radius,
            pos.getX() + radius, pos.getY(), pos.getZ() + radius);
        Biome lastBiome = world.getBiome(pos);
        BiomeColormap colormap = get(state, lastBiome);
        for(BlockPos curpos : coll) {
            Biome biome = world.getBiome(curpos);
            if(biome != lastBiome) {
                colormap = get(state, biome);
                lastBiome = biome;
            }
            int color = colormap != null ? colormap.getColor(biome, curpos) : 0xffffff;
            r += (color & 0xff0000) >> 16;
            g += (color & 0x00ff00) >> 8;
            b += (color & 0x0000ff);
        }
        int posCount = (radius * 2 + 1) * (radius * 2 + 1);
        return ((r / posCount & 255) << 16) | ((g / posCount & 255) << 8) | (b / posCount & 255);
    }
}
