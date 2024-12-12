/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
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

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.mixin.color.BlockColorsAccessor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;

/**
 * These must be kept in sync with the mixins for provided colors and vanilla defaults.
 */
final class DefaultColormaticResolverProviders {

    public static final ColormaticResolverProvider<BlockState> BLOCK_STATE = DefaultColormaticResolverProviders::byBlockState;
    public static final ColormaticResolverProvider<Block> BLOCK = DefaultColormaticResolverProviders::byBlock;
    public static final ColormaticResolverProvider<Identifier> SKY = DefaultColormaticResolverProviders::bySky;
    public static final ColormaticResolverProvider<Identifier> SKY_FOG = DefaultColormaticResolverProviders::byFog;
    // currently unused because Minecraft doesn't have a unified framework for fluid fog
    // also, fluid fog is not blended so resolvers are not used anyway
    public static final ColormaticResolverProvider<Fluid> FLUID_FOG = key -> (manager, biome, posX, posY, posZ) -> -1;

    private DefaultColormaticResolverProviders() {
    }

    private static ColormaticResolver byBlockState(BlockState key) {
        return (manager, biome, posX, posY, posZ) -> {
            // we can't access anything more granular than color resolvers, in general
            // therefore we pay the potential penalty of running through the biome blending twice
            var colorProvider = ((BlockColorsAccessor)MinecraftClient.getInstance().getBlockColors())
                .getProviders()
                .get(Registries.BLOCK.getRawId(key.getBlock()));
            if(colorProvider != null) {
                var world = MinecraftClient.getInstance().world;
                return colorProvider.getColor(key, world, new BlockPos(posX, posY, posZ), 0);
            } else {
                return -1;
            }
        };
    }

    private static ColormaticResolver byBlock(Block key) {
        return byBlockState(key.getDefaultState());
    }

    private static ColormaticResolver bySky(Identifier key) {
        return (manager, biome, posX, posY, posZ) -> {
            int color;
            if(Colormatic.SKY_COLORS.hasCustomColormap() && key.equals(Colormatic.OVERWORLD_ID)) {
                color = Colormatic.SKY_COLORS.getColormap().getColor(manager, biome, posX, posY, posZ);
            } else {
                color = Colormatic.COLOR_PROPS.getProperties().getDimensionSky(key);
                if(color == 0) {
                    color = biome.getSkyColor();
                }
            }
            return color;
        };
    }

    private static ColormaticResolver byFog(Identifier key) {
        return (manager, biome, posX, posY, posZ) -> {
            int color;
            if(Colormatic.FOG_COLORS.hasCustomColormap() && key.equals(Colormatic.OVERWORLD_ID)) {
                color = 0xff000000 | Colormatic.FOG_COLORS.getColormap().getColor(manager, biome, posX, posY, posZ);
            } else {
                color = Colormatic.COLOR_PROPS.getProperties().getDimensionFog(key);
                if(color == 0) {
                    color = biome.getFogColor();
                }
            }
            return color;
        };
    }
}
