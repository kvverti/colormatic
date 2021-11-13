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

import io.github.kvverti.colormatic.Colormatic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.texture.NativeImage;

/**
 * A lightmap texture.
 */
public class Lightmap {

    private static final Logger log = LogManager.getLogger();

    private final NativeImage lightmap;

    public Lightmap(NativeImage lightmap) {
        this.lightmap = lightmap;
    }

    /**
     * Returns whether a resource pack defines this custom lightmap.
     */
    @Deprecated
    public boolean hasCustomColormap() {
        return lightmap != null;
    }

    /**
     * Returns the color for the given block light level.
     */
    public int getBlockLight(int level, float flicker, float nightVision) {
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
                // But if max(r, g, b) is 0, We will use just white. (divide 0 exception)
                int color = lightmap.getColor(x, y);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = (color >> 0) & 0xff;
                int scale = Math.max(Math.max(r, g), b);
                int ret = 0xff000000;
                if (scale != 0) {
                    ret |= (255 * r / scale) << 16;
                    ret |= (255 * g / scale) << 8;
                    ret |= (255 * b / scale) << 0;
                } else {
                    ret |= 0x00ffffff; // white :)
                }
                nightVisionColor = ret;
            } else {
                nightVisionColor = lightmap.getColor(x, y + 32);
            }
            if(nightVision >= 1.0f) {
                return nightVisionColor;
            } else {
                int normalColor = lightmap.getColor(x, y);
                return mergeColors(normalColor, nightVisionColor, nightVision);
            }
        } else {
            return lightmap.getColor(x, y);
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
}
