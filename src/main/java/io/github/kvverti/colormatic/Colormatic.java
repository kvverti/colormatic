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
import io.github.kvverti.colormatic.resource.CustomBiomeColormapsResource;
import io.github.kvverti.colormatic.resource.GlobalColorResource;
import io.github.kvverti.colormatic.resource.GlobalLightmapResource;
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
        new BiomeColormapResource(new Identifier(MODID, "colormap/water"));
    public static final BiomeColormapResource UNDERWATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underwater"));
    public static final BiomeColormapResource UNDERLAVA_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underlava"));
    public static final BiomeColormapResource SKY_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/sky0"));
    public static final BiomeColormapResource FOG_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/fog0"));
    public static final BiomeColormapResource BIRCH_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/birch"));
    public static final BiomeColormapResource SPRUCE_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/pine"));
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
    public static final LinearColormapResource DURABILITY_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/durability.png"));
    public static final LinearColormapResource EXPERIENCE_ORB_COLORS =
        new LinearColormapResource(new Identifier(MODID, "colormap/xporb.png"));
    public static final CustomBiomeColormapsResource CUSTOM_BLOCK_COLORS =
        new CustomBiomeColormapsResource(new Identifier(MODID, "colormap/custom"));
    public static final GlobalLightmapResource LIGHTMAP_PROPS =
        new GlobalLightmapResource(new Identifier(MODID, "lightmap.json"));
    public static final GlobalColorResource COLOR_PROPS =
        new GlobalColorResource(new Identifier(MODID, "color"));

    private static final ColormaticConfig config = new ColormaticConfig();

    public static ColormaticConfig config() {
        return config;
    }

    @Override
    public void onInitializeClient() {

        ColormaticConfigController.load(config);
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
        client.registerReloadListener(DURABILITY_COLORS);
        client.registerReloadListener(EXPERIENCE_ORB_COLORS);
        client.registerReloadListener(CUSTOM_BLOCK_COLORS);
        client.registerReloadListener(LIGHTMAP_PROPS);
        // Note: we don't register this as a reload listener here because it
        // has to be loaded before vanilla resources (namely banner textures).
        // In order to do this, we mix in to TextureManager and directly call
        // its reloading method before any textures are loaded.
        // client.registerReloadListener(COLOR_PROPS);

        RegistryEntryAddedCallback.event(Registry.DIMENSION_TYPE)
            .register(Lightmaps::registerLightmapReload);
        // callbacks don't get run for already registered dim types
        for(DimensionType type : DimensionType.getAll()) {
            Lightmaps.registerLightmapReload(type.getRawId(), DimensionType.getId(type), type);
        }
    }
}
