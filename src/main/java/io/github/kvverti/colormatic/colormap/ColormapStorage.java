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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.kvverti.colormatic.Colormatic;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;

/**
 * Storage for colormaps. This separates storage by biome from fallback colormaps not specified by biome.
 */
final class ColormapStorage<K> {
    private final Table<K, Identifier, BiomeColormap> colormaps;
    private final Map<K, BiomeColormap> fallbackColormaps;
    private final Map<K, ExtendedColorResolver> resolvers;

    ColormapStorage() {
        this.colormaps = HashBasedTable.create();
        this.fallbackColormaps = new HashMap<>();
        this.resolvers = new HashMap<>();
    }

    /**
     * Retrieves the colormap that applies to the given key and biome, if any.
     */
    @Nullable
    public BiomeColormap get(DynamicRegistryManager manager, K key, Biome biome) {
        BiomeColormap res = this.colormaps.get(key, Colormatic.getBiomeId(manager, biome));
        if(res == null) {
            res = this.fallbackColormaps.get(key);
        }
        return res;
    }

    /**
     * Retrieves a colormap that applies to the given key, independent of biome.
     */
    @Nullable
    public BiomeColormap getFallback(K key) {
        return this.fallbackColormaps.get(key);
    }

    /**
     * Retrieves the color resolver for a given key.
     */
    @Nullable
    public ExtendedColorResolver getResolver(K key) {
        return this.resolvers.get(key);
    }

    public boolean contains(K key) {
        return !colormaps.row(key).isEmpty() || fallbackColormaps.containsKey(key);
    }

    public void addColormap(BiomeColormap colormap, Collection<? extends K> keys, Set<? extends Identifier> biomes) {
        if(biomes.isEmpty()) {
            for(K key : keys) {
                fallbackColormaps.put(key, colormap);
                resolvers.put(key, new ExtendedColorResolver(this, key));
            }
        } else {
            for(K key : keys) {
                for(Identifier b : biomes) {
                    colormaps.put(key, b, colormap);
                }
                resolvers.put(key, new ExtendedColorResolver(this, key));
            }
        }
    }

    public void clear() {
        colormaps.clear();
        fallbackColormaps.clear();
        resolvers.clear();
    }
}
