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

import com.google.common.collect.ImmutableMap;

import io.github.kvverti.colormatic.resource.BiomeColormapResource;
import io.github.kvverti.colormatic.resource.LightmapResource;
import io.github.kvverti.colormatic.resource.LinearColormapResource;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class Colormatic implements ClientModInitializer {

    public static final String MODID = "colormatic";

    public static final BiomeColormapResource WATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/water.png"));
    public static final BiomeColormapResource UNDERWATER_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underwater.png"));
    public static final BiomeColormapResource UNDERLAVA_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/underlava.png"));
    public static final BiomeColormapResource SKY_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/sky0.png"));
    public static final BiomeColormapResource FOG_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/fog0.png"));
    public static final BiomeColormapResource BIRCH_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/birch.png"));
    public static final BiomeColormapResource SPRUCE_COLORS =
        new BiomeColormapResource(new Identifier(MODID, "colormap/pine.png"));
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

    public static final ImmutableMap<DimensionType, LightmapResource> LIGHTMAPS;

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

        for(LightmapResource rsc : LIGHTMAPS.values()) {
            client.registerReloadListener(rsc);
        }
    }

    static {
        // TODO: support mod-added dimensions
        ImmutableMap.Builder<DimensionType, LightmapResource> builder = ImmutableMap.builder();
        for(DimensionType type : Registry.DIMENSION) {
            Identifier id = DimensionType.getId(type);
            String filepart;
            if(id.getNamespace().equals("minecraft")) {
                filepart = id.getPath();
            } else {
                filepart = id.toString().replace(':', '/');
            }
            String filename = String.format("lightmap/%s.png", filepart);
            String optifine = String.format("lightmap/world%d.png", type.getRawId());
            LightmapResource rsc =
                new LightmapResource(new Identifier(MODID, filename), optifine);
            builder.put(type, rsc);
        }
        LIGHTMAPS = builder.build();
    }
}
