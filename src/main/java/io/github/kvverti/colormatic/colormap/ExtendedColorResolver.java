/*
 * Colormatic
 * Copyright (C) 2021  Thalia Nero
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

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.level.ColorResolver;

public final class ExtendedColorResolver implements ColorResolver {

    /**
     * A reference to the client's dynamic registry manager.
     */
    @Nullable
    private static DynamicRegistryManager registryManager;

    private final ThreadLocal<CoordinateY> posY;
    private final ColormaticResolver wrappedResolver;

    <K> ExtendedColorResolver(ColormapStorage<K> storage, K key) {
        this.posY = ThreadLocal.withInitial(CoordinateY::new);
        this.wrappedResolver = createResolver(storage, key);
    }

    ExtendedColorResolver(ColormaticResolver wrappedResolver) {
        this.posY = ThreadLocal.withInitial(CoordinateY::new);
        this.wrappedResolver = wrappedResolver;
    }

    public void setY(int y) {
        this.posY.get().y = y;
    }

    @Override
    public int getColor(Biome biome, double x, double z) {
        return wrappedResolver.getColor(registryManager, biome, (int)x, this.posY.get().y, (int)z);
    }

    /**
     * Called from the client upon world reload.
     */
    public static void setRegistryManager(@Nullable DynamicRegistryManager manager) {
        registryManager = manager;
    }

    private static <K> ColormaticResolver createResolver(ColormapStorage<K> storage, K key) {
        final class StoredData {
            @Nullable
            Biome lastBiome;
            @Nullable
            BiomeColormap lastColormap;
        }
        ThreadLocal<StoredData> data = ThreadLocal.withInitial(StoredData::new);
        return (manager, biome, posX, posY, posZ) -> {
            StoredData storedData = data.get();
            if(storedData.lastBiome != biome) {
                storedData.lastColormap = storage.get(manager, key, biome);
                storedData.lastBiome = biome;
            }
            BiomeColormap colormap = storedData.lastColormap;
            return colormap != null ? colormap.getColor(manager, biome, posX, posY, posZ) : 0xffffff;
        };
    }

    private static final class CoordinateY {
        int y;
    }
}
