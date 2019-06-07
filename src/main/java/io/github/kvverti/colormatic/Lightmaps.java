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

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.resource.LightmapResource;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

/**
 * Container class for lightmap resource associations.
 */
public final class Lightmaps {

    private static final Map<DimensionType, LightmapResource> lightmaps = new HashMap<>();

    public static LightmapResource get(DimensionType type) {
        return lightmaps.get(type);
    }

    /**
     * Callback method called when a DimensionType is registered.
     */
    static void registerLightmapReload(int rawId, Identifier id, DimensionType type) {
        String filepart;
        if(id.getNamespace().equals("minecraft")) {
            filepart = id.getPath();
        } else {
            filepart = id.toString().replace(':', '/');
        }
        String filename = String.format("lightmap/%s.png", filepart);
        String optifine = String.format("lightmap/world%d.png", rawId);
        LightmapResource rsc =
            new LightmapResource(new Identifier(Colormatic.MODID, filename), optifine);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(rsc);
        lightmaps.put(type, rsc);
    }
}
