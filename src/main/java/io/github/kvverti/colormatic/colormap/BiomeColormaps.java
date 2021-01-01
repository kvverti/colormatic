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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.properties.ColormapProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Class that provides efficient access to biome colors on
 * a per block state basis. These methods are usually called multiple
 * times each frame.
 */
public final class BiomeColormaps {

    /**
     * Storage for colormaps. This separates storage by biome from fallback colormaps not specified by biome.
     */
    private static final class ColormapStorage<K> {
        final Table<K, Identifier, BiomeColormap> colormaps;
        final Map<K, BiomeColormap> fallbackColormaps;
        final Map<K, ColormaticResolver> resolvers;

        private ColormapStorage() {
            this.colormaps = HashBasedTable.create();
            this.fallbackColormaps = new HashMap<>();
            this.resolvers = new HashMap<>();
        }

        public boolean contains(K key) {
            return !colormaps.row(key).isEmpty() || fallbackColormaps.containsKey(key);
        }

        public void addColormap(BiomeColormap colormap, Collection<? extends K> keys, Set<? extends Identifier> biomes) {
            if(biomes.isEmpty()) {
                for(K key : keys) {
                    fallbackColormaps.put(key, colormap);
                    resolvers.put(key, createResolver(key));
                }
            } else {
                for(K key : keys) {
                    for(Identifier b : biomes) {
                        colormaps.put(key, b, colormap);
                    }
                    resolvers.put(key, createResolver(key));
                }
            }
        }

        private ColormaticResolver createResolver(K key) {
            ThreadLocal<Biome> lastBiome = new ThreadLocal<>();
            ThreadLocal<BiomeColormap> map = new ThreadLocal<>();
            return (manager, biome, pos) -> {
                if(lastBiome.get() != biome) {
                    map.set(get(this, manager, key, biome));
                    lastBiome.set(biome);
                }
                return map.get() != null ? map.get().getColor(manager, biome, pos) : 0xffffff;
            };
        }

        public void clear() {
            colormaps.clear();
            fallbackColormaps.clear();
            resolvers.clear();
        }
    }

    /**
     * Stores colormaps primarily by block.
     */
    private static final ColormapStorage<Block> colormapsByBlock = new ColormapStorage<>();

    /**
     * Stores colormaps primarily by block state.
     */
    private static final ColormapStorage<BlockState> colormapsByState = new ColormapStorage<>();

    private static final ColormapStorage<Identifier> skyColormaps = new ColormapStorage<>();
    private static final ColormapStorage<Identifier> skyFogColormaps = new ColormapStorage<>();
    private static final ColormapStorage<Fluid> fluidFogColormaps = new ColormapStorage<>();

    private BiomeColormaps() {
    }

    /**
     * Gets the Colormatic resolver for the given block state.
     */
    public static ColormaticResolver getResolver(BlockState state) {
        ColormaticResolver resolver = colormapsByState.resolvers.get(state);
        if(resolver == null) {
            resolver = colormapsByBlock.resolvers.get(state.getBlock());
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
    public static BiomeColormap get(DynamicRegistryManager manager, BlockState state, Biome biome) {
        BiomeColormap res = get(colormapsByState, manager, state, biome);
        if(res == null) {
            res = get(colormapsByBlock, manager, state.getBlock(), biome);
        }
        return res;
    }

    public static BiomeColormap getFluidFog(DynamicRegistryManager manager, Fluid fluid, Biome biome) {
        return get(fluidFogColormaps, manager, fluid, biome);
    }

    /**
     * Retrieves the colormap that applies to the given map
     */
    private static <K> BiomeColormap get(ColormapStorage<K> storage, DynamicRegistryManager manager, K key, Biome biome) {
        BiomeColormap res = storage.colormaps.get(key, Colormatic.getBiomeId(manager, biome));
        if(res == null) {
            res = storage.fallbackColormaps.get(key);
        }
        return res;
    }

    public static void add(BiomeColormap colormap) {
        ColormapProperties props = colormap.getProperties();
        Set<Identifier> biomes = props.getApplicableBiomes();
        colormapsByState.addColormap(colormap, props.getApplicableBlockStates(), biomes);
        colormapsByBlock.addColormap(colormap, props.getApplicableBlocks(), biomes);
        for(Map.Entry<Identifier, Collection<Identifier>> entry : props.getApplicableSpecialIds().entrySet()) {
            switch(entry.getKey().toString()) {
                case "colormatic:sky":
                    skyColormaps.addColormap(colormap, entry.getValue(), biomes);
                    break;
                case "colormatic:sky_fog":
                    skyFogColormaps.addColormap(colormap, entry.getValue(), biomes);
                    break;
                case "colormatic:fluid_fog":
                    Collection<Fluid> fluids = entry.getValue().stream().map(Registry.FLUID::get).collect(Collectors.toList());
                    fluidFogColormaps.addColormap(colormap, fluids, biomes);
                    break;
            }
        }
    }

    public static void reset() {
        colormapsByBlock.clear();
        colormapsByState.clear();
        skyColormaps.clear();
        skyFogColormaps.clear();
        fluidFogColormaps.clear();
    }

    /**
     * Returns whether the given state has any custom colormaps.
     */
    public static boolean isCustomColored(BlockState state) {
        return colormapsByBlock.contains(state.getBlock()) || colormapsByState.contains(state);
    }

    public static boolean isSkyCustomColored(World world) {
        return skyColormaps.contains(Colormatic.getDimId(world));
    }

    public static boolean isSkyFogCustomColored(World world) {
        return skyFogColormaps.contains(Colormatic.getDimId(world));
    }

    public static boolean isFluidFogCustomColored(Fluid fluid) {
        return fluidFogColormaps.contains(fluid);
    }

    public static int getBiomeColor(BlockState state, BlockRenderView world, BlockPos pos) {
        if(world == null || pos == null) {
            // todo figure out held item colors
            BiomeColormap colormap = colormapsByState.fallbackColormaps.get(state);
            if(colormap == null) {
                colormap = colormapsByBlock.fallbackColormaps.get(state.getBlock());
            }
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
        ColormaticResolver resolver = getResolver(state);
        return ((ColormaticBlockRenderView)world).colormatic_getColor(pos, resolver);
    }

    public static int getSkyColor(World world, BlockPos pos) {
        Identifier id = Colormatic.getDimId(world);
        return getBiomeColor(skyColormaps, id, world, pos);
    }

    public static int getSkyFogColor(World world, BlockPos pos) {
        Identifier id = Colormatic.getDimId(world);
        return getBiomeColor(skyFogColormaps, id, world, pos);
    }

    public static int getFluidFogColor(Fluid fluid, BlockRenderView world, BlockPos pos) {
        return getBiomeColor(fluidFogColormaps, fluid, world, pos);
    }

    /**
     * Retrieves the biome coloring for the given block position, taking into
     * account the client's biome blend options.
     */
    private static <K> int getBiomeColor(ColormapStorage<K> storage, K key, BlockRenderView world, BlockPos pos) {
        if(world == null || pos == null) {
            BiomeColormap colormap = storage.fallbackColormaps.get(key);
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
        ColormaticResolver resolver = storage.resolvers.get(key);
        if(resolver == null) {
            throw new IllegalStateException("Resolver for existing colormap cannot be null: " + key);
        }
        return ((ColormaticBlockRenderView)world).colormatic_getColor(pos, resolver);
    }
}
