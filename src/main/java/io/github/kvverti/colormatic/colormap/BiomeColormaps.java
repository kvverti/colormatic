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

import java.util.Set;
import io.github.kvverti.colormatic.properties.ColormapProperties;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraft.world.level.ColorResolver;

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
    public static BiomeColormap get(BlockState state, Biome biome) {
        // todo: does this allocate, and if so get rid of it
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
     * Stateful implementation of ColorResolver in order to provide
     * custom biome colors to Minecraft's color calculations.
     */
    public static class ColormaticResolver implements ColorResolver {

        // state from the current method call
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockState state = Blocks.AIR.getDefaultState();

        private Biome lastBiome = null;
        private BiomeColormap colormap = null;

        /**
         * Retrieves the custom color for the given biome and given
         * position. This method also uses the current object state.
         * This method does not directly allocate any objects, and
         * allocates as few objects indirectly as possible.
         */
        @Override
        public synchronized int getColor(Biome biome, double x, double z) {
            if (biome != lastBiome) {
                colormap = get(state, biome);
                lastBiome = biome;
            }
            // ClientWorld#method_23780 sends the BlockPos x and z coordinates
            // via the two double params, for some reason
            pos.setX((int)x);
            pos.setZ((int)z);
            try {
                return colormap != null ? colormap.getColor(biome, pos) : 0xffffff;
            } catch(IllegalArgumentException e) {
                System.out.format("%s %s %s\n", state, biome, colormap.getProperties().getApplicableBiomes());
                throw e;
            }
        }
    }

    public static final ColormaticResolver colormaticResolver = new ColormaticResolver();

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
        colormaticResolver.state = state;
        colormaticResolver.pos.setY(pos.getY());
        return world.method_23752(pos, colormaticResolver);
        // int r = 0;
        // int g = 0;
        // int b = 0;
        // int radius = MinecraftClient.getInstance().options.biomeBlendRadius;
        // Iterable<BlockPos> coll = BlockPos.iterate(
        //     pos.getX() - radius, pos.getY(), pos.getZ() - radius,
        //     pos.getX() + radius, pos.getY(), pos.getZ() + radius);
        // Biome lastBiome = world.getBiome(pos);
        // BiomeColormap colormap = get(state, lastBiome);
        // for(BlockPos curpos : coll) {
        //     Biome biome = world.getBiome(curpos);
        //     if(biome != lastBiome) {
        //         colormap = get(state, biome);
        //         lastBiome = biome;
        //     }
        //     int color = colormap != null ? colormap.getColor(biome, curpos) : 0xffffff;
        //     r += (color & 0xff0000) >> 16;
        //     g += (color & 0x00ff00) >> 8;
        //     b += (color & 0x0000ff);
        // }
        // int posCount = (radius * 2 + 1) * (radius * 2 + 1);
        // return ((r / posCount & 255) << 16) | ((g / posCount & 255) << 8) | (b / posCount & 255);
    }
}
