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
package io.github.kvverti.colormatic.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.MapColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.Registry;

/**
 * The global color.json file. It's a monster.
 */
public class GlobalColorProperties {

    private static final Logger log = LogManager.getLogger();

    public static final GlobalColorProperties DEFAULT = new GlobalColorProperties(new Settings());

    private final Map<ColoredParticle, HexColor> particle;
    private final Map<Identifier, HexColor> dimensionFog;
    private final Map<Identifier, HexColor> dimensionSky;
    private final int lilypad;
    private final Map<StatusEffect, HexColor> potions;
    private final Map<DyeColor, HexColor> sheep;
    private final Map<DyeColor, float[]> sheepRgb;
    private final Map<DyeColor, HexColor> collar;
    private final Map<DyeColor, float[]> collarRgb;
    private final Map<DyeColor, HexColor> banner;
    private final Map<DyeColor, float[]> bannerRgb;
    private final Map<MapColor, HexColor> map;
    private final Map<EntityType<?>, int[]> spawnEgg;
    private final Map<Formatting, TextColor> textColor;
    private final TextColorSettings text;
    private final int xpOrbTime;
    private final ColormapProperties.Format defaultFormat;
    private final @Nullable ColormapProperties.ColumnLayout defaultLayout;

    private GlobalColorProperties(Settings settings) {
        this.particle = settings.particle;
        this.dimensionFog = convertIdMap(settings.fog);
        this.dimensionSky = convertIdMap(settings.sky);
        this.lilypad = settings.lilypad != null ? settings.lilypad.rgb() : 0;
        this.potions = convertMap(settings.potion, Registry.STATUS_EFFECT);
        this.sheep = settings.sheep;
        this.sheepRgb = toRgb(settings.sheep);
        this.collar = settings.collar;
        this.collarRgb = toRgb(settings.collar);
        this.banner = settings.banner;
        this.bannerRgb = toRgb(settings.banner);
        this.map = settings.map;
        this.spawnEgg = collateSpawnEggColors(settings);
        this.xpOrbTime = settings.xporb.time;
        if(settings.text != null) {
            TextColorSettings text = settings.text;
            this.textColor = new HashMap<>();
            for(Map.Entry<Integer, HexColor> entry : text.code.entrySet()) {
                int code = entry.getKey();
                if(code < 16) {
                    Formatting color = Formatting.byColorIndex(code);
                    textColor.put(color, TextColor.fromRgb(entry.getValue().rgb()));
                }
            }
            for(Map.Entry<Formatting, HexColor> entry : text.format.entrySet()) {
                this.textColor.put(entry.getKey(), TextColor.fromRgb(entry.getValue().rgb()));
            }
            text.code = Collections.emptyMap();
            text.format = Collections.emptyMap();
            this.text = text;
        } else {
            // settings.text == null
            this.textColor = Collections.emptyMap();
            this.text = new TextColorSettings();
        }
        this.defaultFormat = settings.palette.format;
        this.defaultLayout = settings.palette.layout;
        // water potions' color does not correspond to a status effect
        // so we use `null` for the key
        HexColor water = settings.potion.get("water");
        if(water == null) {
            water = settings.potion.get("minecraft:water");
        }
        if(water != null) {
            this.potions.put(null, water);
        }
    }

    private Map<Identifier, HexColor> convertIdMap(Map<String, HexColor> map) {
        Map<Identifier, HexColor> res = new HashMap<>();
        for(Map.Entry<String, HexColor> entry : map.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if(id != null) {
                res.put(id, entry.getValue());
            }
        }
        return res;
    }

    private static <T> Map<T, HexColor> convertMap(Map<String, HexColor> initial, Registry<T> registry) {
        Map<T, HexColor> res = new HashMap<>();
        for(Map.Entry<String, HexColor> entry : initial.entrySet()) {
            T key = registry.get(Identifier.tryParse(entry.getKey()));
            if(key != null) {
                res.put(key, entry.getValue());
            }
        }
        return res;
    }

    private static <T> Map<T, float[]> toRgb(Map<T, HexColor> map) {
        Map<T, float[]> res = new HashMap<>();
        for(Map.Entry<T, HexColor> entry : map.entrySet()) {
            int col = entry.getValue().rgb();
            float[] rgb = new float[3];
            rgb[0] = ((col >> 16) & 0xff) / 255.0f;
            rgb[1] = ((col >> 8) & 0xff) / 255.0f;
            rgb[2] = (col & 0xff) / 255.0f;
            res.put(entry.getKey(), rgb);
        }
        return res;
    }

    private static Map<EntityType<?>, int[]> collateSpawnEggColors(Settings settings) {
        Map<EntityType<?>, int[]> res = new HashMap<>();
        Registry<EntityType<?>> registry = Registry.ENTITY_TYPE;
        // handle legacy egg color structure
        if(settings.egg != null) {
            LegacyEggColor legacy = settings.egg;
            for(Map.Entry<String, HexColor> entry : legacy.shell.entrySet()) {
                EntityType<?> type = registry.get(Identifier.tryParse(entry.getKey()));
                res.put(type, new int[]{ entry.getValue().rgb(), 0 });
            }
            for(Map.Entry<String, HexColor> entry : legacy.spots.entrySet()) {
                EntityType<?> type = registry.get(Identifier.tryParse(entry.getKey()));
                int[] colors = res.computeIfAbsent(type, t -> new int[2]);
                colors[1] = entry.getValue().rgb();
            }
        }
        // handle colormatic egg colors
        for(Map.Entry<String, HexColor[]> entry : settings.spawnegg.entrySet()) {
            EntityType<?> type = registry.get(Identifier.tryParse(entry.getKey()));
            int[] colors = res.computeIfAbsent(type, t -> new int[2]);
            HexColor[] hexColors = entry.getValue();
            for(int i = 0; i < Math.min(2, hexColors.length); i++) {
                colors[i] = hexColors[i].rgb();
            }
        }
        return res;
    }

    private static <T> int getColor(T key, Map<T, HexColor> map) {
        HexColor col = map.get(key);
        return col != null ? col.rgb() : 0;
    }

    public int getParticle(ColoredParticle part) {
        return getColor(part, particle);
    }

    public int getDimensionFog(Identifier dimId) {
        return getColor(dimId, dimensionFog);
    }

    public int getDimensionSky(Identifier dimId) {
        return getColor(dimId, dimensionSky);
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

    public float[] getWoolRgb(DyeColor color) {
        return sheepRgb.get(color);
    }

    public int getCollar(DyeColor color) {
        return getColor(color, collar);
    }

    public float[] getCollarRgb(DyeColor color) {
        return collarRgb.get(color);
    }

    public int getBanner(DyeColor color) {
        return getColor(color, banner);
    }

    public float[] getBannerRgb(DyeColor color) {
        return bannerRgb.get(color);
    }

    public int getMap(MapColor color) {
        return getColor(color, map);
    }

    public int getSpawnEgg(EntityType<?> type, int idx) {
        int[] colors = spawnEgg.get(type);
        return colors != null ? colors[idx] : 0;
    }

    private int getColor(HexColor col) {
        return col != null ? col.rgb() : 0;
    }

    public int getXpText() {
        return getColor(text.xpbar);
    }

    public int getButtonTextHovered() {
        return getColor(text.button.hover);
    }

    public int getButtonTextDisabled() {
        return getColor(text.button.disabled);
    }

    public int getSignText(DyeColor color) {
        return getColor(color, text.sign);
    }

    public TextColor getText(Formatting color) {
        return textColor.get(color);
    }

    public int getXpOrbTime() {
        return xpOrbTime;
    }

    public ColormapProperties.Format getDefaultFormat() {
        return defaultFormat;
    }

    public @Nullable ColormapProperties.ColumnLayout getDefaultLayout() {
        return defaultLayout;
    }

    public enum ColoredParticle implements StringIdentifiable {
        WATER("water"),
        LAVA("lava"),
        PORTAL("portal");

        private final String name;

        ColoredParticle(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    // not sure if Optifine changed their color.properties keys over to
    // the official string IDs yet, but in case they haven't here they are
    private static final Map<String, String> keyRemap;

    static {
        keyRemap = new HashMap<>();
        keyRemap.put("nether", "the_nether");
        keyRemap.put("end", "the_end");
        keyRemap.put("lightBlue", "light_blue");
        keyRemap.put("silver", "light_gray");
        keyRemap.put("moveSpeed", "speed");
        keyRemap.put("moveSlowdown", "slowness");
        keyRemap.put("digSpeed", "haste");
        keyRemap.put("digSlowDown", "mining_fatigue");
        keyRemap.put("damageBoost", "strength");
        keyRemap.put("heal", "instant_health");
        keyRemap.put("harm", "instant_damage");
        keyRemap.put("jump", "jump_boost");
        keyRemap.put("confusion", "nausea");
        keyRemap.put("fireResistance", "fire_resistance");
        keyRemap.put("waterBreathing", "water_breathing");
        keyRemap.put("nightVision", "night_vision");
        keyRemap.put("healthBoost", "health_boost");
    }

    public static GlobalColorProperties load(ResourceManager manager, Identifier id, boolean fall) {
        try(InputStream in = manager.getResourceOrThrow(id).getInputStream()) {
            try(Reader r = PropertyUtil.getJsonReader(in, id, k -> keyRemap.getOrDefault(k, k), k -> false)) {
                return loadFromJson(r, id);
            }
        } catch(IOException e) {
            return fall ? GlobalColorProperties.DEFAULT : null;
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
        // Some of the maps use string keys because the keys are identifiers
        // for registry elements. Referencing registry elements from mods not
        // present at runtime results in a null key. Multiple null keys result
        // in an exception from GSON, so we delay resolving identifiers until
        // construction when we can handle missing registry elements ourselves.
        Map<ColoredParticle, HexColor> particle = Collections.emptyMap();
        Map<String, HexColor> fog = Collections.emptyMap();
        Map<String, HexColor> sky = Collections.emptyMap();
        HexColor lilypad;
        Map<String, HexColor> potion = Collections.emptyMap();
        Map<DyeColor, HexColor> sheep = Collections.emptyMap();
        Map<DyeColor, HexColor> collar = Collections.emptyMap();
        Map<MapColor, HexColor> map = Collections.emptyMap();
        Map<DyeColor, HexColor> banner = Collections.emptyMap();
        Map<String, HexColor[]> spawnegg = Collections.emptyMap();
        LegacyEggColor egg;
        TextColorSettings text;
        XpOrb xporb = XpOrb.DEFAULT;
        Palette palette = Palette.DEFAULT;
    }

    /**
     * Optifine color.properties splits spawn egg colors between shell
     * and spots.
     */
    private static class LegacyEggColor {
        Map<String, HexColor> shell = Collections.emptyMap();
        Map<String, HexColor> spots = Collections.emptyMap();
    }

    private static class TextColorSettings {
        HexColor xpbar;
        ButtonText button = new ButtonText();
        Map<DyeColor, HexColor> sign = Collections.emptyMap();
        Map<Formatting, HexColor> format = Collections.emptyMap();
        Map<Integer, HexColor> code = Collections.emptyMap();

        static class ButtonText {
            HexColor hover;
            HexColor disabled;
        }
    }

    private static class XpOrb {
        static XpOrb DEFAULT = new XpOrb();

        int time = 628; // milliseconds
    }

    private static class Palette {
        static Palette DEFAULT = new Palette();

        ColormapProperties.Format format = ColormapProperties.Format.VANILLA;
        @Nullable ColormapProperties.ColumnLayout layout = null;
    }
}
