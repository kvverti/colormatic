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

import com.google.gson.JsonSyntaxException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import net.minecraft.block.BlockState;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A colormap properties structure, specified by a `.properties` file.
 */
public class ColormapProperties {

    private static final Logger log = LogManager.getLogger();

    /**
     * The format of the corresponding colormap.
     */
    private final Format format;

    /**
     * A list of BlockStates that this colormap applies to. If not specified,
     * it is taken from the name of the properties file (minecraft namespace only).
     */
    private final Collection<BlockStatePredicate> blocks;

    /**
     * The colormap image. If not specified, it is taken from the file name
     * of the properties file.
     */
    private final Identifier source;

    /**
     * For format = fixed only, the single color that is applied to all relevant
     * block states.
     */
    private final int color;

    /**
     * For format = grid only, the amount of noise to add to the y coordinate
     * of the block position in order to determine the color.
     */
    private final int yVariance;

    /**
     * For format = grid only, the amount to add to the y corrdinate of the
     * block position before determining the color.
     */
    private final int yOffset;

    private ColormapProperties(Settings settings) {
        this.format = settings.format;
        this.blocks = settings.blocks;
        this.source = new Identifier(settings.source);
        this.color = settings.color.get();
        this.yVariance = settings.yVariance;
        this.yOffset = settings.yOffset;
    }

    public Format getFormat() {
    	return format;
    }

    public int getColor() {
        return color;
    }

    public Identifier getSource() {
        return source;
    }

    public int getVariance() {
    	return yVariance;
    }

    public int getOffset() {
        return yOffset;
    }

    public boolean isForBlock(BlockState state) {
        for(BlockStatePredicate p : blocks) {
            if(p.test(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ColormapProperties { format=%s, blocks=%s, source=%s, color=%x, yVariance=%x, yOffset=%x }",
            format,
            blocks,
            source,
            color,
            yVariance,
            yOffset);
    }

    public enum Format implements StringIdentifiable {
        FIXED("fixed"),
        VANILLA("vanilla"),
        GRID("grid");

        private final String name;

        private Format(String s) {
            name = s;
        }

        @Override
        public String asString() {
            return name;
        }

        public static Format byName(String name) {
            switch(name) {
                case "fixed": return FIXED;
                case "grid": return GRID;
                default: return VANILLA;
            }
        }
    }

    /**
     * Loads the colormap properties defined by the given identifier. The properties
     * should be in JSON format. If not present, returns a default properties taken
     * from the identifier name.
     */
    public static ColormapProperties load(ResourceManager manager, Identifier id) {
        try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
            Reader jsonInput;
            if(id.getPath().endsWith(".properties")) {
                // properties file
                Properties data = new Properties();
                data.load(in);
                jsonInput = new StringReader(PropertyUtil.toJson(data));
            } else {
                // json file
                jsonInput = new InputStreamReader(in);
            }
            try(Reader r = jsonInput) {
                return loadFromJson(r, id);
            }
        } catch(IOException e) {
            return loadFromJson(new StringReader("{}"), id);
        }
    }

    private static ColormapProperties loadFromJson(Reader json, Identifier id) {
        Settings settings;
        try {
            settings = PropertyUtil.PROPERTY_GSON.fromJson(json, Settings.class);
            if(settings == null) {
                settings = new Settings();
            }
        } catch(JsonSyntaxException e) {
            log.error("Error parsing {}: {}", id, e.getCause());
            settings = new Settings();
        }
        if(settings.blocks == null) {
            String blockId = id.getPath();
            blockId = blockId.substring(blockId.lastIndexOf('/') + 1, blockId.lastIndexOf('.'));
            settings.blocks = new ArrayList<>();
            try {
                settings.blocks.add(PropertyUtil.createBlockPredicate(blockId));
            } catch(InvalidPredicateException e) {
                log.warn("Error parsing {}: {}", id, e);
            }
        }
        if(settings.source == null) {
            String path = id.toString();
            path = path.substring(0, path.lastIndexOf('.')) + ".png";
            settings.source = path;
        }
        settings.source = PropertyUtil.resolve(settings.source, id);
        return new ColormapProperties(settings);
    }

    private static class Settings {

        Format format = Format.VANILLA;
        Collection<BlockStatePredicate> blocks;
        String source;
        HexColor color = HexColor.WHITE;
        int yVariance = 0;
        int yOffset = 0;
    }
}
