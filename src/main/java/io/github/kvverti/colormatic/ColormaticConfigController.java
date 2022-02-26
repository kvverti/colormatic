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
package io.github.kvverti.colormatic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

/**
 * This class holds functions that control loading, opening, and persisting
 * the Colormatic config.
 */
public final class ColormaticConfigController {

    private static final Logger log = LogManager.getLogger();

    private static final File configFile;

    private static final ColormaticConfig defaults = new ColormaticConfig();

    private ColormaticConfigController() {
    }

    public static Screen getConfigScreen(ColormaticConfig config, Screen parent) {
        return new ColormaticConfigScreen(LiteralText.EMPTY, parent, config);
    }

    private static final String CLEAR_SKY = "fog.clearSky";
    private static final String CLEAR_VOID = "fog.clearVoid";
    private static final String BLEND_SKY_LIGHT = "light.blendSkyLight";
    private static final String FLICKER_BLOCK_LIGHT = "light.flickerBlockLight";
    private static final String RELATIVE_BLOCK_LIGHT_INTENSITY = "light.relativeBlockLightIntensity";

    public static void load(ColormaticConfig config) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFile));
            config.clearSky = loadOrDefault(props, CLEAR_SKY, Boolean::valueOf, defaults.clearSky);
            config.clearVoid = loadOrDefault(props, CLEAR_VOID, Boolean::valueOf, defaults.clearVoid);
            config.blendSkyLight = loadOrDefault(props, BLEND_SKY_LIGHT, Boolean::valueOf, defaults.blendSkyLight);
            config.flickerBlockLight = loadOrDefault(props, FLICKER_BLOCK_LIGHT, Boolean::valueOf, defaults.flickerBlockLight);
            config.relativeBlockLightIntensityExponent = loadOrDefault(props, RELATIVE_BLOCK_LIGHT_INTENSITY, Double::valueOf, defaults.relativeBlockLightIntensityExponent);
        } catch(IOException e) {
            log.warn("Could not load configuration settings");
        }
    }

    private static <T> T loadOrDefault(Properties props, String key, Function<String, T> parse, T fallback) {
        var value = props.getProperty(key);
        if(value == null) {
            return fallback;
        }
        return parse.apply(value);
    }

    public static void persist(ColormaticConfig config) {
        Properties props = new Properties();
        props.setProperty(CLEAR_SKY, String.valueOf(config.clearSky));
        props.setProperty(CLEAR_VOID, String.valueOf(config.clearVoid));
        props.setProperty(BLEND_SKY_LIGHT, String.valueOf(config.blendSkyLight));
        props.setProperty(FLICKER_BLOCK_LIGHT, String.valueOf(config.flickerBlockLight));
        props.setProperty(RELATIVE_BLOCK_LIGHT_INTENSITY, String.valueOf(config.relativeBlockLightIntensityExponent));
        try {
            configFile.createNewFile();
            props.store(new FileOutputStream(configFile), "Colormatic Config");
        } catch(IOException e) {
            log.warn("Could not save configuration settings");
        }
    }

    static {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "colormatic.properties");
        try {
            if(configFile.createNewFile()) {
                persist(new ColormaticConfig());
            }
        } catch(IOException e) {
            log.warn("Could not create configuration file");
        }
    }
}
