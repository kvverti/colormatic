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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

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
    private final Collection<ApplicableBlockStates> blocks;

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

    /**
     * Maps biomes to columns in grid format. If a biome is not present, then
     * there is no mapping for the biome in this colormap. If null, this colormap
     * uses the default mechanism of mapping the biome to a column via its raw ID.
     */
    private final Object2IntMap<Biome> columnsByBiome;

    private ColormapProperties(Settings settings) {
        this.format = settings.format;
        this.blocks = settings.blocks;
        this.source = new Identifier(settings.source);
        this.color = settings.color.get();
        this.yVariance = settings.yVariance;
        this.yOffset = settings.yOffset;
        if(settings.biomes != null) {
            columnsByBiome = new Object2IntOpenHashMap<>();
            for(Map.Entry<Identifier, Integer> entry : settings.biomes.entrySet()) {
                Biome b = Registry.BIOME.get(entry.getKey());
                if(b != null) {
                    columnsByBiome.put(b, entry.getValue().intValue());
                }
            }
        } else {
            columnsByBiome = null;
        }
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

    /**
     * Returns, for the grid format, which column of the colormap the given
     * biome should use. Returns -1 if the biome is not assigned to a column.
     * If the format is not grid, or biome is null, returns 0.
     */
    public int getColumn(Biome biome) {
        if(format == Format.GRID && biome != null) {
            if(columnsByBiome != null) {
                return columnsByBiome.getOrDefault(biome, -1);
            } else {
                return Registry.BIOME.getRawId(biome);
            }
        }
        return 0;
    }

    public Set<Biome> getApplicableBiomes() {
        Set<Biome> res = new HashSet<>();
        if(columnsByBiome != null) {
            res.addAll(columnsByBiome.keySet());
        }
        return res;
    }

    public Set<Block> getApplicableBlocks() {
        Set<Block> res = new HashSet<>();
        for(ApplicableBlockStates a : blocks) {
            if(a.states.isEmpty()) {
                res.add(a.block);
            }
        }
        return res;
    }

    public Set<BlockState> getApplicableBlockStates() {
        Set<BlockState> res = new HashSet<>();
        for(ApplicableBlockStates a : blocks) {
            res.addAll(a.states);
        }
        return res;
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
            try(Reader r = PropertyUtil.getJsonReader(in, id, k -> k, "blocks"::equals)) {
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
            log.error("Error parsing {}: {}", id, e.getMessage());
            settings = new Settings();
        }
        if(settings.blocks == null) {
            String blockId = id.getPath();
            blockId = blockId.substring(blockId.lastIndexOf('/') + 1, blockId.lastIndexOf('.'));
            settings.blocks = new ArrayList<>();
            try {
                settings.blocks.add(PropertyUtil.PROPERTY_GSON.fromJson(blockId, ApplicableBlockStates.class));
            } catch(JsonSyntaxException e) {
                log.error("Error parsing {}: {}", id, e.getMessage());
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
        Collection<ApplicableBlockStates> blocks;
        String source;
        HexColor color = HexColor.WHITE;
        int yVariance = 0;
        int yOffset = 0;
        Map<Identifier, Integer> biomes = null;
    }
}
