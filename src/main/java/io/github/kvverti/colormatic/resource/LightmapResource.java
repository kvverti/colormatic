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
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

/**
 * A lightmap texture.
 */
public class LightmapResource implements SimpleResourceReloadListener<NativeImage> {

    private final Identifier id;
    private final Identifier optifineId;
    private NativeImage lightmap;

    public LightmapResource(Identifier id, String optifinePath) {
        this.id = id;
        this.optifineId = new Identifier("minecraft", "optifine/" + optifinePath);
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    /**
     * Returns whether a resource pack defines this custom lightmap.
     */
    public boolean hasCustomColormap() {
        return lightmap != null;
    }

    /**
     * Returns the color for the given block light level. The Random parameter
     * controls block light flicker.
     */
    public int getBlockLight(int level, Random rand, boolean nightVision) {
        if(lightmap == null) {
            throw new IllegalStateException("No custom lightmap present: " + id);
        }
        int width = lightmap.getWidth();
        int offset = nightVision ? 48 : 16;
        return lightmap.getPixelRGBA(rand.nextInt(width), offset + level);
    }

    /**
     * Returns the color for the given sky light level. The ambience controls
     * the position of the skylight spectrum, from night to day to lightning.
     * Non-lightning ambiences perform a weighted average of adjacent colors
     * in order to provide a smooth transition between ambience levels.
     */
    public int getSkyLight(int level, float ambience, boolean nightVision) {
        if(lightmap == null) {
            throw new IllegalStateException("No custom lightmap present: " + id);
        }
        int posY = level + (nightVision ? 32 : 0);
        if(ambience < 0) {
            // lightning
            int posX = lightmap.getWidth() - 1;
            return lightmap.getPixelRGBA(posX, posY);
        } else {
            float scaledAmbience = ambience * (lightmap.getWidth() - 2);
            float scaledAmbienceRemainder = scaledAmbience % 1.0f;
            int posX = (int)scaledAmbience;
            int light = lightmap.getPixelRGBA(posX, posY);
            if(posX < lightmap.getWidth() - 2) {
                int rightLight = lightmap.getPixelRGBA(posX + 1, posY);
                light = mergeColors(rightLight, light, scaledAmbienceRemainder);
            }
            return light;
        }
    }

    private int mergeColors(int a, int b, float aweight) {
        int cha, chb;
        int res = 0xff000000;
        cha = ((a >> 16) & 0xff);
        chb = ((b >> 16) & 0xff);
        res |= (int)(cha * aweight + chb * (1 - aweight)) << 16;
        cha = ((a >> 8) & 0xff);
        chb = ((b >> 8) & 0xff);
        res |= (int)(cha * aweight + chb * (1 - aweight)) << 8;
        cha = a & 0xff;
        chb = b & 0xff;
        res |= (int)(cha * aweight + chb * (1 - aweight));
        return res;
    }

    @Override
    public CompletableFuture<NativeImage> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
                return NativeImage.fromInputStream(rsc.getInputStream());
            } catch(IOException e) {
                // try optifine ID
                try(Resource rsc = manager.getResource(optifineId); InputStream in = rsc.getInputStream()) {
                    return NativeImage.fromInputStream(rsc.getInputStream());
                } catch(IOException e2) {
                    // no lightmap
                    return null;
                }
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(NativeImage data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> lightmap = data, executor);
    }
}
