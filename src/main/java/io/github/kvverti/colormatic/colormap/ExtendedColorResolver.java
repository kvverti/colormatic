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

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;

public final class ExtendedColorResolver implements ColorResolver {

    /**
     * A reference to the client's dynamic registry manager.
     */
    @Nullable
    private static DynamicRegistryManager registryManager;

    /**
     * The Y coordinate to use for calculating color. As ColorResolvers are called concurrently
     * from multiple threads, this must be thread local data.
     */
    private final ThreadLocal<CoordinateY> posY;
    private final ColormaticResolver wrappedResolver;

    <K> ExtendedColorResolver(ColormapStorage<K> storage, K key, ColormaticResolver fallback) {
        this.posY = ThreadLocal.withInitial(CoordinateY::new);
        this.wrappedResolver = createResolver(storage, key, fallback);
    }

    ExtendedColorResolver(ColormaticResolver wrappedResolver) {
        this.posY = ThreadLocal.withInitial(CoordinateY::new);
        this.wrappedResolver = wrappedResolver;
    }

    /**
     * Prefer to use this instead of {@link BlockRenderView#getColor(BlockPos, ColorResolver)}.
     */
    public int resolveExtendedColor(BlockRenderView world, BlockPos pos) {
        this.posY.get().y = pos.getY();
        return world.getColor(pos, this);
    }

    public ColormaticResolver getWrappedResolver() {
        return wrappedResolver;
    }

    @Override
    public int getColor(Biome biome, double x, double z) {
        // work-around for Sodium treating pure white as a "no-cache" sentinel
        return 0xfffefefe & wrappedResolver.getColor(registryManager, biome, (int)x, this.posY.get().y, (int)z);
    }

    /**
     * Called from the client upon world reload.
     */
    public static void setRegistryManager(@Nullable DynamicRegistryManager manager) {
        registryManager = manager;
    }

    private static <K> ColormaticResolver createResolver(ColormapStorage<K> storage, K key, ColormaticResolver fallback) {
        final class StoredData {
            @Nullable
            Biome lastBiome;
            @Nullable
            BiomeColormap lastColormap;
        }
        var data = ThreadLocal.withInitial(StoredData::new);
        return (manager, biome, posX, posY, posZ) -> {
            var storedData = data.get();
            if(storedData.lastBiome != biome) {
                storedData.lastColormap = storage.get(manager, key, biome);
                storedData.lastBiome = biome;
            }
            var colormap = storedData.lastColormap;
            return colormap != null ? colormap.getColor(manager, biome, posX, posY, posZ)
                : fallback.getColor(manager, biome, posX, posY, posZ);
        };
    }

    private static final class CoordinateY {
        int y;
    }
}
