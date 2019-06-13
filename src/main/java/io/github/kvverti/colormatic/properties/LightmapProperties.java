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
package io.github.kvverti.colormatic.properties;

import com.google.gson.JsonParseException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class LightmapProperties {

    private static final Logger log = LogManager.getLogger();

    /**
     * By default, sky light colors are interpolated smoothly when they change.
     * Set this to false to disable this behavior.
     */
    private final boolean blendAmbience;

    /**
     * Specifies at what sky light level block light should start waning. If the sky
     * light level is greater than this value, block light is taken from
     * `blocklight - (skylight - wane)`, floored at 0.
     */
    private final int blockWane;

    private LightmapProperties(Settings settings) {
        this.blendAmbience = settings.blendAmbience;
        this.blockWane = settings.blockWane;
    }

    public boolean shouldBlendAmbience() {
        return blendAmbience;
    }

    public int getBlockWane() {
        return blockWane;
    }

    private static final Properties defaults;
    static {
        defaults = new Properties();
        defaults.setProperty("blend.ambience", "true");
        defaults.setProperty("block.wane", "15");
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
        boolean blendAmbience = true;
        int blockWane = 15;
    }
}
