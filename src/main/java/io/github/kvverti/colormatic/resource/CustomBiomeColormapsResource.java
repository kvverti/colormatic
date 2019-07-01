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

import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.properties.InvalidColormapException;
import io.github.kvverti.colormatic.properties.PropertyImage;
import io.github.kvverti.colormatic.properties.PropertyUtil;

import java.util.Collection;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Handles custom colormaps for blocks.
 */
public class CustomBiomeColormapsResource implements SimpleSynchronousResourceReloadListener {

    private static final Logger log = LogManager.getLogger();

    private final Identifier id;
    private final Identifier optifineId;

    public CustomBiomeColormapsResource(Identifier id) {
        this.id = id;
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public void apply(ResourceManager manager) {
        BiomeColormaps.reset();
        addColormaps(manager, optifineId, false);
        addColormaps(manager, id, true);
    }

    private static void addColormaps(ResourceManager manager, Identifier dir, boolean json) {
        String ext = json ? ".json" : ".properties";
        Collection<Identifier> files = manager.findResources(dir.getPath(), s -> s.endsWith(ext))
            .stream()
            .filter(id -> id.getNamespace().equals(dir.getNamespace()))
            .distinct()
            .collect(toList());
        for(Identifier id : files) {
            try {
                PropertyImage pi = PropertyUtil.loadColormap(manager, id);
                BiomeColormap colormap = new BiomeColormap(pi.properties, pi.image);
                BiomeColormaps.add(colormap);
            } catch(InvalidColormapException e) {
                log.warn("Error parsing {}: {}", id, e.getMessage());
            }
        }
    }
}
