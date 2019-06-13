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
import io.github.kvverti.colormatic.properties.InvalidColormapException;
import io.github.kvverti.colormatic.properties.PropertyImage;
import io.github.kvverti.colormatic.properties.PropertyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.block.BlockState;
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
    private final List<BiomeColormap> colormaps;

    public CustomBiomeColormapsResource(Identifier id) {
        this.id = id;
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath());
        this.colormaps = new ArrayList<>();
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    /**
     * Returns the colormap for the given block state, or `null` if no custom
     * colormap is defined for this block state. In the case where multiple
     * colormaps apply to the same block state, one of them is returned.
     */
    public BiomeColormap getColormap(BlockState state) {
        for(BiomeColormap colormap : colormaps) {
            if(colormap.appliesTo(state)) {
                return colormap;
            }
        }
        return null;
    }

    @Override
    public void apply(ResourceManager manager) {
        colormaps.clear();
        addColormaps(manager, id, colormaps, true);
        addColormaps(manager, optifineId, colormaps, false);
    }

    private static void addColormaps(ResourceManager manager, Identifier dir, List<BiomeColormap> colormaps, boolean json) {
        String ext = json ? ".json" : ".properties";
        Collection<Identifier> files = manager.findResources(dir.getPath(), s -> s.endsWith(ext))
            .stream()
            .filter(id -> id.getNamespace().equals(dir.getNamespace()))
            .distinct()
            .collect(toList());
        for(Identifier id : files) {
            try {
                PropertyImage pi = PropertyUtil.loadColormap(manager, id);
                colormaps.add(new BiomeColormap(pi.properties, pi.image));
            } catch(InvalidColormapException e) {
                log.warn("Error parsing {}: {}", id, e.getMessage());
            }
        }
    }
}
