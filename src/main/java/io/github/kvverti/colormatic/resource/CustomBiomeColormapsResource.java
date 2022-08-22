/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
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
package io.github.kvverti.colormatic.resource;

import java.util.Collection;
import java.util.regex.Pattern;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.properties.InvalidColormapException;
import io.github.kvverti.colormatic.properties.PropertyImage;
import io.github.kvverti.colormatic.properties.PropertyUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import static java.util.stream.Collectors.toList;

/**
 * Handles custom colormaps for blocks.
 */
public class CustomBiomeColormapsResource implements SimpleSynchronousResourceReloadListener {

    private static final Logger log = LogManager.getLogger(Colormatic.MODID);
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_/.-]+");

    private final Identifier id;
    private final Identifier optifineId;
    private final Identifier otherOptifineId;

    public CustomBiomeColormapsResource() {
        this.id = new Identifier(Colormatic.MODID, "colormap/custom");
        this.optifineId = new Identifier("minecraft", "optifine/colormap/custom");
        this.otherOptifineId = new Identifier("minecraft", "optifine/colormap/blocks");
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public void reload(ResourceManager manager) {
        BiomeColormaps.reset();
        addColormaps(manager, otherOptifineId, false);
        addColormaps(manager, optifineId, false);
        addColormaps(manager, id, true);
    }

    private static void addColormaps(ResourceManager manager, Identifier dir, boolean json) {
        String ext = json ? ".json" : ".properties";
        Collection<Identifier> files = manager.findResources(dir.getPath(),
            id -> id.getNamespace().equals(dir.getNamespace()) && (id.getPath().endsWith(ext) || id.getPath().endsWith(".png")))
            .keySet()
            .stream()
            .map(id -> {
                // count plain source images as properties so they're found
                var path = id.getPath();
                if(path.endsWith(".png")) {
                    var newPath = path.substring(0, path.length() - 4) + ext;
                    return new Identifier(id.getNamespace(), newPath);
                } else {
                    return id;
                }
            })
            .distinct()
            .collect(toList());
        for(Identifier id : files) {
            if(!ID_PATTERN.matcher(id.getPath()).matches()) {
                log.error("Colormap definition file '{}' does not name a valid resource location. Please contact resource pack author to fix.", id);
            }
            try {
                PropertyImage pi = PropertyUtil.loadColormap(manager, id, true);
                BiomeColormap colormap = new BiomeColormap(pi.properties(), pi.image());
                BiomeColormaps.add(colormap);
            } catch(InvalidColormapException e) {
                log.error("Error parsing {}: {}", id, e.getMessage());
            }
        }
    }
}
