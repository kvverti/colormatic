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
package io.github.kvverti.colormatic.resource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.minecraft.client.util.RawTextureDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

/**
 * An optional biome colormap in the vanilla format. The colormap is expected
 * to be found at the path given by an instance's ID, or at the corresponding
 * Optifine path.
 */
public class ColormapResource implements SimpleResourceReloadListener<int[]> {

    private final Identifier id;
    private final Identifier optifineId;
    private int[] colormap = null;

    public ColormapResource(Identifier id) {
        this.id = id;
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    /**
     * Returns whether a resource pack has defined a custom colormap
     * for this resource.
     */
    public boolean hasCustomColormap() {
        return colormap != null;
    }

    /**
     * Returns a color given by the custom colormap for the given biome
     * temperature and humidity.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource
     */
    public int getColor(double temp, double rain) {
        if(colormap == null) {
            throw new IllegalStateException("No custom colormap present: " + id);
        }
        rain *= temp;
        int x = (int)((1.0D - temp) * 255.0D);
        int y = (int)((1.0D - rain) * 255.0D);
        int idx = y << 8 | x;
        return idx > colormap.length ? 0xffff00ff : colormap[idx];
    }

    /**
     * Returns the default color given by the custom colormap.
     *
     * @throws IllegalStateException if no resource pack defines a custom colormap
     *     for this resource.
     */
    public int getDefaultColor() {
        if(colormap == null) {
            throw new IllegalStateException("No custom colormap present: " + id);
        }
        return colormap[(128 << 8) | 128];
    }

    @Override
    public CompletableFuture<int[]> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return RawTextureDataLoader.loadRawTextureData(manager, id);
            } catch(IOException e) {
                // try OptiFine directory
                try {
                    return RawTextureDataLoader.loadRawTextureData(manager, optifineId);
                } catch(IOException e2) {
                    // fallback to vanilla
                    return null;
                }
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(int[] data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> colormap = data, executor);
    }
}
