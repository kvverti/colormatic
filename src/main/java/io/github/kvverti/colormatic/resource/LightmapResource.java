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

import io.github.kvverti.colormatic.Colormatic;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A lightmap texture.
 */
public class LightmapResource implements SimpleResourceReloadListener<NativeImage> {

    private static final Logger log = LogManager.getLogger();

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
     * Returns the color for the given block light level.
     */
    public int getBlockLight(int level, float flicker, float nightVision) {
        if(lightmap == null) {
            throw new IllegalStateException("No custom lightmap present: " + id);
        }
        int width = lightmap.getWidth();
        int posX = (int)(flicker * width) % width;
        if(posX < 0) {
            posX = -posX;
        }
        return getPixel(posX, level + 16, nightVision);
    }

    /**
     * Returns the color for the given sky light level. The ambience controls
     * the position of the skylight spectrum, from night to day to lightning.
     * Non-lightning ambiences perform a weighted average of the color to the
     * right in order to provide a smooth transition between ambience levels.
     */
    public int getSkyLight(int level, float ambience, float nightVision) {
        if(lightmap == null) {
            throw new IllegalStateException("No custom lightmap present: " + id);
        }
        if(ambience < 0) {
            // lightning
            int posX = lightmap.getWidth() - 1;
            return getPixel(posX, level, nightVision);
        } else {
            float scaledAmbience = ambience * (lightmap.getWidth() - 2);
            float scaledAmbienceRemainder = scaledAmbience % 1.0f;
            int posX = (int)scaledAmbience;
            int light = getPixel(posX, level, nightVision);
            boolean blend = Colormatic.config().blendSkyLight;
            if(blend && posX < lightmap.getWidth() - 2) {
                int rightLight = getPixel(posX + 1, level, nightVision);
                light = mergeColors(rightLight, light, scaledAmbienceRemainder);
            }
            return light;
        }
    }

    /**
     * Returns the pixel at (x, y) with or without night vision.
     */
    private int getPixel(int x, int y, float nightVision) {
        if(nightVision > 0.0f) {
            int nightVisionColor;
            if(lightmap.getHeight() != 64) {
                // night vision is calculated as
                // newColor[r, g, b] = oldColor[r, g, b] / max(r, g, b)
                int color = lightmap.getPixelRgba(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >>  8) & 0xff;
                int b = (color >>  0) & 0xff;
                int scale = Math.max(Math.max(r, g), b);
                int ret = 0xff000000;
                ret |= (255 * r / scale) << 16;
                ret |= (255 * g / scale) <<  8;
                ret |= (255 * b / scale) <<  0;
                nightVisionColor = ret;
            } else {
                nightVisionColor = lightmap.getPixelRgba(x, y + 32);
            }
            if(nightVision >= 1.0f) {
                return nightVisionColor;
            } else {
                int normalColor = lightmap.getPixelRgba(x, y);
                return mergeColors(normalColor, nightVisionColor, nightVision);
            }
        } else {
            return lightmap.getPixelRgba(x, y);
        }
    }

    private int mergeColors(int a, int b, float aweight) {
        float oneMinusAweight = 1 - aweight;
        int cha, chb;
        int res = 0xff000000;
        cha = ((a >> 16) & 0xff);
        chb = ((b >> 16) & 0xff);
        res |= (int)(cha * aweight + chb * oneMinusAweight) << 16;
        cha = ((a >> 8) & 0xff);
        chb = ((b >> 8) & 0xff);
        res |= (int)(cha * aweight + chb * oneMinusAweight) << 8;
        cha = a & 0xff;
        chb = b & 0xff;
        res |= (int)(cha * aweight + chb * oneMinusAweight);
        return res;
    }

    @Override
    public CompletableFuture<NativeImage> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
                return NativeImage.read(in);
            } catch(IOException e) {
                // try optifine ID
                try(Resource rsc = manager.getResource(optifineId); InputStream in = rsc.getInputStream()) {
                    return NativeImage.read(in);
                } catch(IOException e2) {
                    // no lightmap
                    return null;
                }
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(NativeImage data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            if(data != null &&
                    (data.getWidth() < 2 ||
                    (data.getHeight() != 32 && data.getHeight() != 64))) {
                log.warn("Lightmap image dimensions must be nX32 or nX64: " + id);
                lightmap = null;
            } else {
                lightmap = data;
            }
        }, executor);
    }
}
