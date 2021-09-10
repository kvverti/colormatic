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
package io.github.kvverti.colormatic.properties;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * @param blockWane Specifies at what sky light level block light should start waning. If the sky
 *                  light level is greater than this value, block light is taken from
 *                  `blocklight - (skylight - wane)`, floored at 0.
 */
public record LightmapProperties(int blockWane) {

    private static final Logger log = LogManager.getLogger();

    private LightmapProperties(Settings settings) {
        this(settings.blockWane);
    }

    /**
     * Loads the lightmap properties defined by the given identifier.
     * If not present, returns a default properties.
     */
    public static LightmapProperties load(ResourceManager manager, Identifier id) {
        Settings settings;
        try(Resource rsc = manager.getResource(id); Reader in = new InputStreamReader(rsc.getInputStream())) {
            settings = PropertyUtil.PROPERTY_GSON.fromJson(in, Settings.class);
        } catch(JsonParseException e) {
            log.error("Error parsing {}: {}", id, e.getMessage());
            settings = new Settings();
        } catch(IOException e) {
            settings = new Settings();
        }
        return new LightmapProperties(settings);
    }

    private static class Settings {
        int blockWane = 15;
    }
}
