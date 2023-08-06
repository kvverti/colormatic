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
package io.github.kvverti.colormatic.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import io.github.kvverti.colormatic.Lightmaps;
import io.github.kvverti.colormatic.colormap.Lightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class LightmapsResource implements SimpleResourceReloadListener<Map<Identifier, NativeImage>> {

    private static final Logger logger = LogManager.getLogger();

    private final Identifier id;
    private final Identifier optifineId;

    public LightmapsResource(Identifier id) {
        this.id = id;
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Map<Identifier, NativeImage>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, NativeImage> lightmaps = getLightmaps(manager, optifineId);
            lightmaps.putAll(getLightmaps(manager, id));
            return lightmaps;
        }, executor);
    }

    // Key = dimension ID, Value = lightmap image
    private static Map<Identifier, NativeImage> getLightmaps(ResourceManager manager, Identifier dir) {
        Map<Identifier, Resource> files = manager
            .findResources(dir.getPath(), s -> s.getPath().endsWith(".png") && s.getNamespace().equals(dir.getNamespace()));
        Map<Identifier, NativeImage> res = new HashMap<>(files.size());
        for(Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier id = entry.getKey();
            try(InputStream in = entry.getValue().getInputStream()) {
                // colormatic:lightmap/minecraft/overworld.png -> minecraft:overworld
                // colormatic:lightmap/the_nether.png -> minecraft:the_nether
                // minecraft:optifine/lightmap/world1.png -> minecraft:the_end
                int dirStrLen = dir.toString().length();
                String thisStr = id.toString();
                String dimIdStr = thisStr.substring(dirStrLen + 1, thisStr.length() - 4).replaceFirst("/", ":");
                Identifier dimId = Identifier.tryParse(fixOptifineDimId(dimIdStr));
                if(dimId != null) {
                    res.put(dimId, NativeImage.read(in));
                } else {
                    logger.error("Invalid lightmap dimension ID: " + thisStr);
                }
            } catch(IOException e) {
                // skip this file
                logger.error("Could not read lightmap file " + id, e);
            }
        }
        return res;
    }

    private static String fixOptifineDimId(String dimIdStr) {
        // fix Optifine dimension IDs
        return switch(dimIdStr) {
            case "world0" -> "minecraft:overworld";
            case "world1" -> "minecraft:the_end";
            case "world-1" -> "minecraft:the_nether";
            default -> dimIdStr;
        };
    }

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, NativeImage> lightmapData, ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Lightmaps.clearLightmaps();
            for(Map.Entry<Identifier, NativeImage> entry : lightmapData.entrySet()) {
                NativeImage img = entry.getValue();
                if(img.getWidth() < 2 || (img.getHeight() != 32 && img.getHeight() != 64)) {
                    logger.warn("Lightmap image dimensions must be nX32 or nX64: " + id);
                } else {
                    Lightmaps.addLightmap(entry.getKey(), new Lightmap(img));
                }
            }
        }, executor);
    }
}
