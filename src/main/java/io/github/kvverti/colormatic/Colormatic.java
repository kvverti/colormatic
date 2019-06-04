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

import io.github.kvverti.colormatic.resource.ColormapResource;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class Colormatic implements ClientModInitializer {

    public static final String MODID = "colormatic";

    public static final ColormapResource WATER_COLORS =
        new ColormapResource(new Identifier(MODID, "colormap/water.png"));
    public static final ColormapResource UNDERWATER_COLORS =
        new ColormapResource(new Identifier(MODID, "colormap/underwater.png"));
    public static final ColormapResource SKY_COLORS =
        new ColormapResource(new Identifier(MODID, "colormap/sky0.png"));
    public static final ColormapResource FOG_COLORS =
        new ColormapResource(new Identifier(MODID, "colormap/fog0.png"));

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper client = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
        client.registerReloadListener(WATER_COLORS);
        client.registerReloadListener(UNDERWATER_COLORS);
        client.registerReloadListener(SKY_COLORS);
        client.registerReloadListener(FOG_COLORS);
    }
}
