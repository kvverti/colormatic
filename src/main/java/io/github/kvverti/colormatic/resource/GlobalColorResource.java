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

import io.github.kvverti.colormatic.properties.GlobalColorProperties;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class GlobalColorResource implements SimpleSynchronousResourceReloadListener {

    private final Identifier id;
    private final Identifier optifineId;
    private GlobalColorProperties properties = GlobalColorProperties.DEFAULT;

    public GlobalColorResource(Identifier id) {
        this.id = new Identifier(id.getNamespace(), id.getPath() + ".json");
        this.optifineId = new Identifier("minecraft", "optifine/" + id.getPath() + ".properties");
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    public GlobalColorProperties getProperties() {
        return properties;
    }

    @Override
    public void reload(ResourceManager manager) {
        GlobalColorProperties props = GlobalColorProperties.load(manager, id, false);
        if(props == null) {
            props = GlobalColorProperties.load(manager, optifineId, true);
        }
        properties = props;
    }
}
