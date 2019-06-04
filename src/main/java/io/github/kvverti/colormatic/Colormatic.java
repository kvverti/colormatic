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
package io.github.kvverti.colormatic;

import io.github.kvverti.colormatic.resource.BiomeColormapResource;
import io.github.kvverti.colormatic.resource.LinearColormapResource;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class Colormatic implements ClientModInitializer {

    public static final String MODID = "colormatic";

    public static final BiomeColormapResource WATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/water.png"));
    public static final BiomeColormapResource UNDERWATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underwater.png"));
    public static final BiomeColormapResource SKY_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/sky0.png"));
    public static final BiomeColormapResource FOG_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/fog0.png"));
    public static final BiomeColormapResource BIRCH_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/birch.png"));
    public static final BiomeColormapResource SPRUCE_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/pine.png"));
    public static final LinearColormapResource REDSTONE_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/redstone.png"));

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper client = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
        client.registerReloadListener(WATER_COLORS);
        client.registerReloadListener(UNDERWATER_COLORS);
        client.registerReloadListener(SKY_COLORS);
        client.registerReloadListener(FOG_COLORS);
        client.registerReloadListener(BIRCH_COLORS);
        client.registerReloadListener(SPRUCE_COLORS);
        client.registerReloadListener(REDSTONE_COLORS);
    }
}
