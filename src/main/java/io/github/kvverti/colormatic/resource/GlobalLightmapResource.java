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

import io.github.kvverti.colormatic.properties.LightmapProperties;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class GlobalLightmapResource implements SimpleSynchronousResourceReloadListener {

    private final Identifier id;
    private LightmapProperties properties;

    public GlobalLightmapResource(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    public LightmapProperties getProperties() {
    	return properties;
    }

    @Override
    public void apply(ResourceManager manager) {
        properties = LightmapProperties.load(manager, id);
    }
}
