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
package io.github.kvverti.colormatic.colormap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.properties.ColormapProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.dimension.DimensionType;

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

    // tables of special colormaps. These replace the pseudo block state system from 1.15.

    private static final Table<Identifier, Biome, BiomeColormap> skyColormaps = HashBasedTable.create();
    private static final Table<Identifier, Biome, BiomeColormap> skyFogColormaps = HashBasedTable.create();
    private static final Table<Fluid, Biome, BiomeColormap> fluidFogColormaps = HashBasedTable.create();

    private static final Map<Identifier, ColormaticResolver> skyResolvers = new HashMap<>();
    private static final Map<Identifier, ColormaticResolver> skyFogResolvers = new HashMap<>();
    private static final Map<Fluid, ColormaticResolver> fluidFogResolvers = new HashMap<>();

    private BiomeColormaps() {
    }

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
        BiomeColormap res = get(colormapsByState, state, biome);
        if(res == null) {
            res = get(colormapsByBlock, state.getBlock(), biome);
        }
        return res;
    }

    public static BiomeColormap getFluidFog(Fluid fluid, Biome biome) {
        return get(fluidFogColormaps, fluid, biome);
    }

    /**
     * Retrieves the colormap that applies to the given map
     */
    private static <K> BiomeColormap get(Table<K, Biome, BiomeColormap> table, K key, Biome biome) {
        // todo: does this allocate, and if so get rid of it
        Map<Biome, BiomeColormap> map = table.row(key);
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
            resolversByState.put(state, createResolver(colormapsByState, state));
        }
        for(Block block : props.getApplicableBlocks()) {
            for(Biome b : biomes) {
                colormapsByBlock.put(block, b, colormap);
            }
            resolversByBlock.put(block, createResolver(colormapsByBlock, block));
        }
        for(Map.Entry<Identifier, Collection<Identifier>> entry : props.getApplicableSpecialIds().entrySet()) {
            switch(entry.getKey().toString()) {
            case "colormatic:sky":
                for(Identifier id : entry.getValue()) {
                    for(Biome b : biomes) {
                        skyColormaps.put(id, b, colormap);
                    }
                    skyResolvers.put(id, createResolver(skyColormaps, id));
                }
                break;
            case "colormatic:sky_fog":
                for(Identifier id : entry.getValue()) {
                    for(Biome b : biomes) {
                        skyFogColormaps.put(id, b, colormap);
                    }
                    skyFogResolvers.put(id, createResolver(skyFogColormaps, id));
                }
                break;
            case "colormatic:fluid_fog":
                for(Identifier id : entry.getValue()) {
                    Fluid f = Registry.FLUID.get(id);
                    for(Biome b : biomes) {
                        fluidFogColormaps.put(f, b, colormap);
                    }
                    fluidFogResolvers.put(f, createResolver(fluidFogColormaps, f));
                }
                break;
            }
        }
    }

    private static <K> ColormaticResolver createResolver(Table<K, Biome, BiomeColormap> table, K key) {
        ThreadLocal<Biome> lastBiome = new ThreadLocal<>();
        ThreadLocal<BiomeColormap> map = new ThreadLocal<>();
        return (biome, pos) -> {
            if(lastBiome.get() != biome) {
                map.set(get(table, key, biome));
                lastBiome.set(biome);
            }
            return map.get() != null ? map.get().getColor(biome, pos) : 0xffffff;
        };
    }

    public static void reset() {
        colormapsByBlock.clear();
        colormapsByState.clear();
        skyColormaps.clear();
        skyFogColormaps.clear();
        fluidFogColormaps.clear();
        resolversByState.clear();
        resolversByBlock.clear();
        skyResolvers.clear();
        skyFogResolvers.clear();
        fluidFogResolvers.clear();
    }

    /**
     * Returns whether the given state has any custom colormaps.
     */
    public static boolean isCustomColored(BlockState state) {
        return !colormapsByBlock.row(state.getBlock()).isEmpty() ||
            !colormapsByState.row(state).isEmpty();
    }

    public static boolean isSkyCustomColored(DimensionType dim) {
        return !skyColormaps.row(Colormatic.getDimId(dim)).isEmpty();
    }

    public static boolean isSkyFogCustomColored(DimensionType dim) {
        return !skyFogColormaps.row(Colormatic.getDimId(dim)).isEmpty();
    }

    public static boolean isFluidFogCustomColored(Fluid fluid) {
        return !fluidFogColormaps.row(fluid).isEmpty();
    }

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

    public static int getSkyColor(DimensionType dim, BlockRenderView world, BlockPos pos) {
        Identifier id = Colormatic.getDimId(dim);
        return getBiomeColor(skyColormaps, skyResolvers, id, world, pos);
    }

    public static int getSkyFogColor(DimensionType dim, BlockRenderView world, BlockPos pos) {
        Identifier id = Colormatic.getDimId(dim);
        return getBiomeColor(skyFogColormaps, skyFogResolvers, id, world, pos);
    }

    public static int getFluidFogColor(Fluid fluid, BlockRenderView world, BlockPos pos) {
        return getBiomeColor(fluidFogColormaps, fluidFogResolvers, fluid, world, pos);
    }

    /**
     * Retrieves the biome coloring for the given block position, taking into
     * account the client's biome blend options.
     */
    private static <K> int getBiomeColor(Table<K, Biome, BiomeColormap> table, Map<K, ColormaticResolver> resolvers, K key, BlockRenderView world, BlockPos pos) {
        if(world == null || pos == null) {
            BiomeColormap colormap = get(table, key, ALL);
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
        ColormaticResolver resolver = resolvers.get(key);
        if(resolver == null) {
            throw new IllegalStateException("Resolver for existing colormap cannot be null: " + key);
        }
        return ((ColormaticBlockRenderView)world).colormatic_getColor(pos, resolver);
    }
}
