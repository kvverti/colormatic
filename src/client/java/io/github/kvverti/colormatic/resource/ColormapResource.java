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
    protected int[] colormap = null;

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
