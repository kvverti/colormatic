/*
 * Colormatic
 * Copyright (C) 2019-2020  Thalia Nero
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
package io.github.kvverti.colormatic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

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
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(new TranslatableText("colormatic.config.title"))
            .setSavingRunnable(() -> persist(config));
        builder.getOrCreateCategory(new TranslatableText("colormatic.config.category.fog"))
            .addEntry(ConfigEntryBuilder.create()
                .startBooleanToggle(new TranslatableText("colormatic.config.option.clearSky"), config.clearSky)
                .setDefaultValue(defaults.clearSky)
                .setSaveConsumer(value -> config.clearSky = value)
                .build())
            .addEntry(ConfigEntryBuilder.create()
                .startBooleanToggle(new TranslatableText("colormatic.config.option.clearVoid"), config.clearVoid)
                .setDefaultValue(defaults.clearVoid)
                .setSaveConsumer(value -> config.clearVoid = value)
                .build());
        builder.getOrCreateCategory(new TranslatableText("colormatic.config.category.light"))
            .addEntry(ConfigEntryBuilder.create()
                .startBooleanToggle(new TranslatableText("colormatic.config.option.blendSkyLight"), config.blendSkyLight)
                .setDefaultValue(defaults.blendSkyLight)
                .setSaveConsumer(value -> config.blendSkyLight = value)
                .build())
            .addEntry(ConfigEntryBuilder.create()
                .startBooleanToggle(new TranslatableText("colormatic.config.option.flickerBlockLight"), config.flickerBlockLight)
                .setDefaultValue(defaults.flickerBlockLight)
                .setSaveConsumer(value -> config.flickerBlockLight = value)
                .build());
        return builder.build();
    }

    public static void load(ColormaticConfig config) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFile));
            config.clearSky = Boolean.parseBoolean(props.getProperty("fog.clearSky"));
            config.clearVoid = Boolean.parseBoolean(props.getProperty("fog.clearVoid"));
            config.blendSkyLight = Boolean.parseBoolean(props.getProperty("light.blendSkyLight"));
            config.flickerBlockLight = Boolean.parseBoolean(props.getProperty("light.flickerBlockLight"));
        } catch(IOException e) {
            log.warn("Could not load configuration settings");
        }
    }

    private static void persist(ColormaticConfig config) {
        Properties props = new Properties();
        props.setProperty("fog.clearSky", String.valueOf(config.clearSky));
        props.setProperty("fog.clearVoid", String.valueOf(config.clearVoid));
        props.setProperty("light.blendSkyLight", String.valueOf(config.blendSkyLight));
        props.setProperty("light.flickerBlockLight", String.valueOf(config.flickerBlockLight));
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
