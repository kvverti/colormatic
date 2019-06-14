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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.dimension.DimensionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The global color.json file. It's a monster.
 */
public class GlobalColorProperties {

    private static final Logger log = LogManager.getLogger();

    private final Map<ColoredParticle, HexColor> particle;
    private final Map<DimensionType, HexColor> dimensionFog;
    private final Map<DimensionType, HexColor> dimensionSky;
    private final int lilypad;
    private final Map<StatusEffect, HexColor> potions;
    private final Map<DyeColor, HexColor> sheep;
    private final Map<DyeColor, HexColor> collar;
    private final Map<DyeColor, HexColor> banner;

    private GlobalColorProperties(Settings settings) {
        this.particle = settings.particle;
        this.dimensionFog = settings.fog;
        this.dimensionSky = settings.sky;
        this.lilypad = settings.lilypad != null ? settings.lilypad.get() : 0;
        this.potions = settings.potion;
        this.sheep = settings.sheep;
        this.collar = settings.collar;
        this.banner = settings.map;
    }

    private static <T> int getColor(T key, Map<T, HexColor> map) {
        if(map == null) {
            return 0;
        }
        HexColor col = map.get(key);
        return col != null ? col.get() : 0;
    }

    public int getParticle(ColoredParticle part) {
        return getColor(part, particle);
    }

    public int getDimensionFog(DimensionType type) {
        return getColor(type, dimensionFog);
    }

    public int getDimensionSky(DimensionType type) {
        return getColor(type, dimensionSky);
    }

    public int getLilypad() {
        return lilypad;
    }

    public int getPotion(StatusEffect effect) {
        return getColor(effect, potions);
    }

    public int getWool(DyeColor color) {
        return getColor(color, sheep);
    }

    public int getCollar(DyeColor color) {
        return getColor(color, collar);
    }

    public int getBanner(DyeColor color) {
        return getColor(color, banner);
    }

    public enum ColoredParticle implements StringIdentifiable {
        WATER("water"),
        PORTAL("portal");

        private final String name;

        private ColoredParticle(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public static GlobalColorProperties load(ResourceManager manager, Identifier id, boolean fall) {
        try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
            try(Reader r = PropertyUtil.getJsonReader(in, id)) {
                return loadFromJson(r, id);
            }
        } catch(IOException e) {
            return fall ? loadFromJson(new StringReader("{}"), id) : null;
        }
    }

    private static GlobalColorProperties loadFromJson(Reader rd, Identifier id) {
        Settings settings;
        try {
            settings = PropertyUtil.PROPERTY_GSON.fromJson(rd, Settings.class);
            if(settings == null) {
                settings = new Settings();
            }
        } catch(JsonParseException e) {
            log.error("Error parsing {}: {}", id, e.getMessage());
            settings = new Settings();
        }
        return new GlobalColorProperties(settings);
    }

    private static class Settings {
        Map<ColoredParticle, HexColor> particle;
        Map<DimensionType, HexColor> fog;
        Map<DimensionType, HexColor> sky;
        HexColor lilypad;
        Map<StatusEffect, HexColor> potion;
        Map<DyeColor, HexColor> sheep;
        Map<DyeColor, HexColor> collar;
        Map<DyeColor, HexColor> map;
    }
}
