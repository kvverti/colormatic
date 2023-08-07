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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.kvverti.colormatic.Colormatic;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;

/**
 * Storage for colormaps. This separates storage by biome from fallback colormaps not specified by biome.
 */
final class ColormapStorage<K> {
    private final Table<K, Identifier, BiomeColormap> colormaps;
    private final Map<K, BiomeColormap> fallbackColormaps;
    private final Map<K, ExtendedColorResolver> resolvers;
    private final Map<K, ColormaticResolver> defaultResolvers;
    private final ColormaticResolverProvider<K> defaultResolverProvider;

    ColormapStorage(ColormaticResolverProvider<K> defaultResolverProvider) {
        this.colormaps = HashBasedTable.create();
        this.fallbackColormaps = new HashMap<>();
        this.resolvers = new HashMap<>();
        this.defaultResolvers = new HashMap<>();
        this.defaultResolverProvider = defaultResolverProvider;
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

    public ColormaticResolver getColormaticResolver(K key) {
        var extendedResolver = getResolver(key);
        if(extendedResolver != null) {
            return extendedResolver.getWrappedResolver();
        }
        return defaultResolvers.computeIfAbsent(key, defaultResolverProvider::create);
    }

    public boolean contains(K key) {
        return !colormaps.row(key).isEmpty() || fallbackColormaps.containsKey(key);
    }

    public void addColormap(BiomeColormap colormap, Collection<? extends K> keys, Set<? extends Identifier> biomes) {
        if(biomes.isEmpty()) {
            for(K key : keys) {
                fallbackColormaps.put(key, colormap);
                resolvers.put(key, new ExtendedColorResolver(this, key, defaultResolvers.computeIfAbsent(key, defaultResolverProvider::create)));
            }
        } else {
            for(K key : keys) {
                for(Identifier b : biomes) {
                    colormaps.put(key, b, colormap);
                }
                resolvers.put(key, new ExtendedColorResolver(this, key, defaultResolvers.computeIfAbsent(key, defaultResolverProvider::create)));
            }
        }
    }

    public void clear() {
        colormaps.clear();
        fallbackColormaps.clear();
        resolvers.clear();
        defaultResolvers.clear();
    }
}
