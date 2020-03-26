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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import io.github.kvverti.colormatic.properties.ColormapProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
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
    private static final Table<Block, Biome, BiomeColormap> colormapsByBlock = HashBasedTable.create();

    /**
     * Stores colormaps primarily by block state, and secondarily by biome.
     * Colormaps that apply to all biomes are stored under the key ALL.
     */
    private static final Table<BlockState, Biome, BiomeColormap> colormapsByState = HashBasedTable.create();

    /**
     * Cache of ColormaticResolvers corresponding to the applicable block
     * states in all biome colormaps.
     */
    private static final Map<BlockState, ColormaticResolver> resolversByState = new HashMap<>();

    /**
     * Cache of ColormaticResolbers corresponding to the applicable blocks
     * in all biome colormaps.
     */
    private static final Map<Block, ColormaticResolver> resolversByBlock = new HashMap<>();

    private BiomeColormaps() {}

    /**
     * Gets the Colormatic resolver for the given block state.
     */
    public static ColormaticResolver getResolver(BlockState state) {
        ColormaticResolver resolver = resolversByState.get(state);
        if(resolver == null) {
            resolver = resolversByBlock.get(state.getBlock());
        }
        if(resolver == null) {
            throw new IllegalArgumentException(String.valueOf(state));
        }
        return resolver;
    }

    /**
     * Retrieves the colormap that applies to the given block state and biome.
     * Returns `null` if there are no colormaps that apply.
     */
    public static BiomeColormap get(BlockState state, Biome biome) {
        // todo: does this allocate, and if so get rid of it
        Map<Biome, BiomeColormap> map = colormapsByState.row(state);
        BiomeColormap res = map.get(biome);
        if(res == null) {
            res = map.get(ALL);
        }
        if(res == null) {
            res = get(state.getBlock(), biome);
        }
        return res;
    }

    /**
     * Retrieves the colormap that applies to all states of
     * the given block and biome.
     */
    private static BiomeColormap get(Block block, Biome biome) {
        Map<Biome, BiomeColormap> map = colormapsByBlock.row(block);
        BiomeColormap res = map.get(biome);
        if(res == null) {
            res = map.get(ALL);
        }
        return res;
    }

    public static void add(BiomeColormap colormap) {
        ColormapProperties props = colormap.getProperties();
        Set<Biome> biomes = props.getApplicableBiomes();
        if(biomes.isEmpty()) {
            biomes = Collections.singleton(ALL);
        }
        for(BlockState state : props.getApplicableBlockStates()) {
            for(Biome b : biomes) {
                colormapsByState.put(state, b, colormap);
            }
            ThreadLocal<Biome> lastBiome = new ThreadLocal<>();
            ThreadLocal<BiomeColormap> map = new ThreadLocal<>();
            resolversByState.put(state, (biome, pos) -> {
                if(lastBiome.get() != biome) {
                    map.set(get(state, biome));
                    lastBiome.set(biome);
                }
                return map.get() != null ? map.get().getColor(biome, pos) : 0xffffff;
            });
        }
        for(Block block : props.getApplicableBlocks()) {
            for(Biome b : biomes) {
                colormapsByBlock.put(block, b, colormap);
            }
            ThreadLocal<Biome> lastBiome = new ThreadLocal<>();
            ThreadLocal<BiomeColormap> map = new ThreadLocal<>();
            resolversByBlock.put(block, (biome, pos) -> {
                if(lastBiome.get() != biome) {
                    map.set(get(block, biome));
                    lastBiome.set(biome);
                }
                return map.get() != null ? map.get().getColor(biome, pos) : 0xffffff;
            });
        }
    }

    public static void reset() {
        colormapsByBlock.clear();
        colormapsByState.clear();
        resolversByState.clear();
        resolversByBlock.clear();
    }

    /**
     * Returns whether the given state has any custom colormaps.
     */
    public static boolean isCustomColored(BlockState state) {
        return !colormapsByBlock.row(state.getBlock()).isEmpty() ||
            !colormapsByState.row(state).isEmpty();
    }

    /**
     * Retrieves the biome coloring for the given block position, taking into
     * account the client's biome blend options.
     */
    public static int getBiomeColor(BlockState state, BlockRenderView world, BlockPos pos) {
        if(world == null || pos == null) {
            // todo figure out held item colors
            BiomeColormap colormap = get(state, ALL);
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
        ColormaticResolver resolver = getResolver(state);
        return ((ColormaticBlockRenderView)world).colormatic_getColor(pos, resolver);
    }
}
