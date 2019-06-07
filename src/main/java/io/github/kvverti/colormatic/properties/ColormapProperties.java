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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import net.minecraft.block.BlockState;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * A colormap properties structure, specified by a `.properties` file.
 */
public class ColormapProperties {

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
        this.source = settings.source;
        this.color = settings.color;
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
     * Loads the colormap properties defined by the given identifier.
     * If not present, returns a default properties taken from the
     * identifier name.
     */
    public static ColormapProperties load(ResourceManager manager, Identifier id) {
        Properties data = new Properties(computeDefaults(id));
        Settings settings = new Settings();
        try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
            data.load(in);
        } catch(IOException e) {
            // ignored
        }
        settings.format = Format.byName(data.getProperty("format"));
        settings.color = 0xff000000 | parseOrDefault(data.getProperty("color"), 16, 0xffffff);
        settings.yVariance = parseOrDefault(data.getProperty("yVariance"), 10, 0);
        settings.yOffset = parseOrDefault(data.getProperty("yOffset"), 10, 0);
        String srcPath = data.getProperty("source");
        if(srcPath.startsWith("./")) {
            // relative path
            String thisPath = id.toString();
            srcPath = thisPath.substring(0, thisPath.lastIndexOf('/')) + srcPath.substring(1);
        } else if(srcPath.startsWith("~/")) {
            // ~ is the optifine directory
            srcPath = "optifine" + srcPath.substring(1);
        }
        settings.source = new Identifier(srcPath);
        settings.blocks = new ArrayList<>();
        String[] blockPreds = data.getProperty("blocks").split("\\s+");
        for(String s : blockPreds) {
            settings.blocks.add(PropertyUtil.createBlockPredicate(s));
        }
        return new ColormapProperties(settings);
    }

    /**
     * Default properties for all biome colormaps.
     */
    private static final Properties baseProperties;

    static {
        baseProperties = new Properties();
        baseProperties.setProperty("format", "vanilla");
        baseProperties.setProperty("color", "ffffff");
        baseProperties.setProperty("yVariance", "0");
        baseProperties.setProperty("yOffset", "0");
    }

    private static Properties computeDefaults(Identifier id) {
        String path = id.toString();
        path = path.substring(0, path.lastIndexOf('.')) + ".png";
        String blockId = id.getPath();
        blockId = blockId.substring(blockId.lastIndexOf('/') + 1, blockId.lastIndexOf('.'));
        Properties res = new Properties(baseProperties);
        res.setProperty("source", path);
        res.setProperty("blocks", blockId);
        return res;
    }

    private static int parseOrDefault(String s, int base, int fallback) {
        try {
            return Integer.parseInt(s, base);
        } catch(NumberFormatException e) {
            return fallback;
        }
    }

    private static class Settings {

        Format format;
        Collection<BlockStatePredicate> blocks;
        Identifier source;
        int color;
        int yVariance;
        int yOffset;
    }
}
