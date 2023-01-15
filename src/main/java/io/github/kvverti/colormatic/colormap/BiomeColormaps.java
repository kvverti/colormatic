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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.kvverti.colormatic.properties.ColormapProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

/**
 * Class that provides efficient access to biome colors on
 * a per block state basis. These methods are usually called multiple
 * times each frame.
 */
public final class BiomeColormaps {

    /**
     * Stores colormaps primarily by block.
     */
    private static final ColormapStorage<Block> colormapsByBlock = new ColormapStorage<>(DefaultColormaticResolverProviders.BLOCK);

    /**
     * Stores colormaps primarily by block state.
     */
    private static final ColormapStorage<BlockState> colormapsByState = new ColormapStorage<>(DefaultColormaticResolverProviders.BLOCK_STATE);

    private static final ColormapStorage<Identifier> skyColormaps = new ColormapStorage<>(DefaultColormaticResolverProviders.SKY);
    private static final ColormapStorage<Identifier> skyFogColormaps = new ColormapStorage<>(DefaultColormaticResolverProviders.SKY_FOG);
    private static final ColormapStorage<Fluid> fluidFogColormaps = new ColormapStorage<>(DefaultColormaticResolverProviders.FLUID_FOG);

    private BiomeColormaps() {
    }

    public static ColormaticResolver getTotalSky(Identifier dimId) {
        return skyColormaps.getColormaticResolver(dimId);
    }

    public static ColormaticResolver getTotalSkyFog(Identifier dimId) {
        return skyFogColormaps.getColormaticResolver(dimId);
    }

    public static BiomeColormap getFluidFog(DynamicRegistryManager manager, Fluid fluid, Biome biome) {
        return fluidFogColormaps.get(manager, fluid, biome);
    }

    public static void add(BiomeColormap colormap) {
        ColormapProperties props = colormap.getProperties();
        Set<Identifier> biomes = props.getApplicableBiomes();
        colormapsByState.addColormap(colormap, props.getApplicableBlockStates(), biomes);
        colormapsByBlock.addColormap(colormap, props.getApplicableBlocks(), biomes);
        for(Map.Entry<Identifier, Collection<Identifier>> entry : props.getApplicableSpecialIds().entrySet()) {
            switch(entry.getKey().toString()) {
                case "colormatic:sky" -> skyColormaps.addColormap(colormap, entry.getValue(), biomes);
                case "colormatic:sky_fog" -> skyFogColormaps.addColormap(colormap, entry.getValue(), biomes);
                case "colormatic:fluid_fog" -> {
                    Collection<Fluid> fluids = entry.getValue().stream().map(Registries.FLUID::get).collect(Collectors.toList());
                    fluidFogColormaps.addColormap(colormap, fluids, biomes);
                }
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

    /**
     * Items don't have a world position, so the corresponding block state takes default values,
     * which may not be present even if the block state has custom coloring.
     */
    public static boolean isItemCustomColored(BlockState state) {
        return colormapsByBlock.getFallback(state.getBlock()) != null || colormapsByState.getFallback(state) != null;
    }

    public static boolean isFluidFogCustomColored(Fluid fluid) {
        return fluidFogColormaps.contains(fluid);
    }

    public static int getBiomeColor(BlockState state, BlockRenderView world, BlockPos pos) {
        if(world != null && pos != null) {
            var resolver = colormapsByState.getResolver(state);
            if(resolver == null) {
                resolver = colormapsByBlock.getResolver(state.getBlock());
            }
            if(resolver == null) {
                throw new IllegalArgumentException(String.valueOf(state));
            }
            return resolver.resolveExtendedColor(world, pos);
        } else {
            BiomeColormap colormap = colormapsByState.getFallback(state);
            if(colormap == null) {
                colormap = colormapsByBlock.getFallback(state.getBlock());
            }
            if(colormap != null) {
                return colormap.getDefaultColor();
            } else {
                return 0xffffff;
            }
        }
    }
}
