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
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class Colormatic implements ClientModInitializer {

    public static final String MODID = "colormatic";

    public static final BiomeColormapResource WATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/water.properties"));
    public static final BiomeColormapResource UNDERWATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underwater.properties"));
    public static final BiomeColormapResource UNDERLAVA_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underlava.properties"));
    public static final BiomeColormapResource SKY_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/sky0.properties"));
    public static final BiomeColormapResource FOG_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/fog0.properties"));
    public static final BiomeColormapResource BIRCH_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/birch.properties"));
    public static final BiomeColormapResource SPRUCE_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/pine.properties"));
    public static final LinearColormapResource PUMPKIN_STEM_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/pumpkinstem.png"));
    public static final LinearColormapResource MELON_STEM_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/melonstem.png"));
    public static final LinearColormapResource REDSTONE_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/redstone.png"));
    public static final LinearColormapResource MYCELIUM_PARTICLE_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/myceliumparticle.png"));
    public static final LinearColormapResource LAVA_DROP_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/lavadrop.png"));

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper client = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
        client.registerReloadListener(WATER_COLORS);
        client.registerReloadListener(UNDERWATER_COLORS);
        client.registerReloadListener(UNDERLAVA_COLORS);
        client.registerReloadListener(SKY_COLORS);
        client.registerReloadListener(FOG_COLORS);
        client.registerReloadListener(BIRCH_COLORS);
        client.registerReloadListener(SPRUCE_COLORS);
        client.registerReloadListener(REDSTONE_COLORS);
        client.registerReloadListener(PUMPKIN_STEM_COLORS);
        client.registerReloadListener(MELON_STEM_COLORS);
        client.registerReloadListener(MYCELIUM_PARTICLE_COLORS);
        client.registerReloadListener(LAVA_DROP_COLORS);

        RegistryEntryAddedCallback.event(Registry.DIMENSION)
            .register(Lightmaps::registerLightmapReload);
        // callbacks don't get run for already registered dim types
        for(DimensionType type : DimensionType.getAll()) {
            Lightmaps.registerLightmapReload(type.getRawId(), DimensionType.getId(type), type);
        }
    }
}
