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
package io.github.kvverti.colormatic.resource;

import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.properties.InvalidColormapException;
import io.github.kvverti.colormatic.properties.PropertyImage;
import io.github.kvverti.colormatic.properties.PropertyUtil;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * An optional biome colormap in the vanilla format. The colormap is expected
 * to be found at the path given by an instance's ID, or at the corresponding
 * Optifine path.
 */
public class BiomeColormapResource implements SimpleSynchronousResourceReloadListener {

    private final Identifier id;
    private final Identifier optifineId;
    private BiomeColormap colormap;

    public BiomeColormapResource(Identifier id) {
        this.id = new Identifier(id.getNamespace(), id.getPath() + ".json");
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath() + ".properties");
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    /**
     * Returns whether a resource pack has defined a custom colormap
     * for this resource.
     */
    public boolean hasCustomColormap() {
        return colormap != null;
    }

    /**
     * Returns the custom colormap specified by this resource.
     *
     * @throws IllegalStateException if no resource pack defines a custom
     *     colormap for this resource.
     */
    public BiomeColormap getColormap() {
        if(colormap == null) {
            throw new IllegalStateException("No custom colormap present: " + getFabricId());
        }
        return colormap;
    }

    @Override
    public void reload(ResourceManager manager) {
        PropertyImage pi;
        try {
            pi = PropertyUtil.loadColormap(manager, id, false);
        } catch(InvalidColormapException e) {
            // try Optifine directory
            try {
                pi = PropertyUtil.loadColormap(manager, optifineId, false);
            } catch(InvalidColormapException e2) {
                // no custom colormap
                pi = null;
            }
        }
        colormap = pi == null ? null : new BiomeColormap(pi.properties(), pi.image());
    }
}
