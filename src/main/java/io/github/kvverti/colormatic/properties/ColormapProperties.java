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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonSyntaxException;
import io.github.kvverti.colormatic.Colormatic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

/**
 * A colormap properties structure, specified by a `.properties` file.
 */
public class ColormapProperties {

    private static final Logger log = LogManager.getLogger();

    /**
     * The file these properties came from.
     */
    private final Identifier id;

    /**
     * Whether these colormap properties came from an Optifine colormap file.
     */
    private transient final boolean optifine;

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
     * block states. Null if not specified.
     */
    private final HexColor color;

    /**
     * For format = grid only, the way in which to lay out biome columns by default.
     */
    private final ColumnLayout layout;

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
    private final Map<Identifier, ColumnBounds> columnsByBiome;

    /**
     * Represents a column or columns assigned to a partcular biome.
     */
    public static class ColumnBounds {

        public final int column;
        public final int count;

        public ColumnBounds(int c, int n) {
            column = c;
            count = n;
        }
    }

    private ColormapProperties(Identifier id, Settings settings) {
        this.id = id;
        this.optifine = this.id.getPath().endsWith(".properties");
        this.format = settings.format;
        this.blocks = settings.blocks;
        Identifier source = Identifier.tryParse(settings.source);
        if(source == null) {
            log.error("{}: Invalid source location '{}', using file name as fallback", id, settings.source);
            source = new Identifier(makeSourceFromFileName(id));
        }
        this.source = source;
        this.color = settings.color;
        this.layout = Objects.requireNonNullElse(settings.layout, this.optifine ? ColumnLayout.OPTIFINE : ColumnLayout.DEFAULT);
        this.yVariance = settings.yVariance;
        this.yOffset = settings.yOffset;
        if(settings.grid != null) {
            this.columnsByBiome = new HashMap<>();
            int nextColumn = 0;
            for(GridEntry entry : settings.grid) {
                if(entry.column >= 0) {
                    nextColumn = entry.column;
                }
                ColumnBounds bounds = new ColumnBounds(nextColumn, entry.width);
                nextColumn += entry.width;
                for(Identifier biomeId : entry.biomes) {
                    Identifier updated = BiomeRenaming.updateName(biomeId, this.id);
                    if(updated != null) {
                        columnsByBiome.put(updated, bounds);
                    }
                }
            }
        } else if(settings.biomes != null) {
            this.columnsByBiome = new HashMap<>();
            for(Map.Entry<Identifier, Integer> entry : settings.biomes.entrySet()) {
                Identifier updated = BiomeRenaming.updateName(entry.getKey(), this.id);
                if(updated != null) {
                    columnsByBiome.put(updated, new ColumnBounds(entry.getValue(), 1));
                }
            }
        } else {
            this.columnsByBiome = null;
        }
    }

    public Identifier getId() {
        return id;
    }

    public Format getFormat() {
        return format;
    }

    public HexColor getColor() {
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

    private static final ColumnBounds DEFAULT_BOUNDS = new ColumnBounds(0, 1);

    /**
     * Returns, for the grid format, which column of the colormap the given
     * biome should use. If this colormap applies to all biomes, then the columns
     * are based on the biome's raw ID.
     *
     * @throws IllegalArgumentException if the colormap does not apply to the
     *                                  given biome
     * @throws IllegalStateException    if the format is not grid format
     */
    public ColumnBounds getColumn(RegistryKey<Biome> biomeKey, Registry<Biome> biomeRegistry) {
        if(format == Format.GRID) {
            if(biomeKey != null) {
                Identifier id = biomeKey.getValue();
                if(columnsByBiome != null) {
                    ColumnBounds cb = columnsByBiome.get(id);
                    if(cb == null) {
                        throw new IllegalArgumentException(id.toString());
                    }
                    return cb;
                } else {
                    return switch(layout) {
                        case DEFAULT -> DefaultColumns.getDefaultBounds(biomeKey);
                        case OPTIFINE -> DefaultColumns.getOptifineBounds(biomeKey, biomeRegistry);
                        case LEGACY -> DefaultColumns.getLegacyBounds(biomeKey, biomeRegistry, this.optifine);
                        case STABLE -> DefaultColumns.getStableBounds(biomeKey);
                    };
                }
            } else {
                return DEFAULT_BOUNDS;
            }
        } else {
            throw new IllegalStateException(format.toString());
        }
    }

    /**
     * Returns the set of biomes this colormap applies to, or the empty set
     * if this colormap applies to all biomes.
     */
    public Set<Identifier> getApplicableBiomes() {
        Set<Identifier> res = new HashSet<>();
        if(columnsByBiome != null) {
            res.addAll(columnsByBiome.keySet());
        }
        return res;
    }

    public Set<Block> getApplicableBlocks() {
        Set<Block> res = new HashSet<>();
        for(ApplicableBlockStates a : blocks) {
            if(a.specialKey == null && a.states.isEmpty()) {
                res.add(a.block);
            }
        }
        return res;
    }

    public Set<BlockState> getApplicableBlockStates() {
        Set<BlockState> res = new HashSet<>();
        for(ApplicableBlockStates a : blocks) {
            if(a.specialKey == null) {
                res.addAll(a.states);
            }
        }
        return res;
    }

    public Map<Identifier, Collection<Identifier>> getApplicableSpecialIds() {
        Map<Identifier, Collection<Identifier>> res = new HashMap<>();
        for(ApplicableBlockStates a : blocks) {
            if(a.specialKey != null) {
                res.put(a.specialKey, a.specialIds);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return String.format("ColormapProperties { format=%s, blocks=%s, source=%s, color=%x, yVariance=%x, yOffset=%x }",
            format,
            blocks,
            source,
            color == null ? 0 : color.rgb(),
            yVariance,
            yOffset);
    }

    public enum Format implements StringIdentifiable {
        FIXED("fixed"),
        VANILLA("vanilla"),
        GRID("grid");

        private final String name;

        Format(String s) {
            name = s;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public enum ColumnLayout implements StringIdentifiable {
        DEFAULT("default"),
        OPTIFINE("optifine"),
        LEGACY("legacy"),
        STABLE("stable");

        private final String name;

        ColumnLayout(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    /**
     * Loads the colormap properties defined by the given identifier. The properties
     * should be in JSON format. If not present, returns a default properties taken
     * from the identifier name.
     */
    public static ColormapProperties load(ResourceManager manager, Identifier id, boolean custom) {
        try(InputStream in = manager.getResourceOrThrow(id).getInputStream()) {
            try(Reader r = PropertyUtil.getJsonReader(in, id, k -> k, "blocks"::equals)) {
                return loadFromJson(r, id, custom);
            }
        } catch(IOException e) {
            return loadFromJson(new StringReader("{}"), id, custom);
        }
    }

    private static ColormapProperties loadFromJson(Reader json, Identifier id, boolean custom) {
        Settings settings;
        try {
            settings = PropertyUtil.PROPERTY_GSON.fromJson(json, Settings.class);
            if(settings == null) {
                settings = new Settings();
            }
        } catch(Exception e) {
            // any one of a number of exceptions could have been thrown during deserialization
            log.error("Error loading {}: {}", id, e.getMessage());
            settings = new Settings();
        }
        if(settings.format == null) {
            settings.format = Colormatic.COLOR_PROPS.getProperties().getDefaultFormat();
        }
        if(settings.layout == null) {
            settings.layout = Colormatic.COLOR_PROPS.getProperties().getDefaultLayout();
        }
        if(custom) {
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
        } else {
            // disable `blocks`, `grid`, and `biomes` for non-custom colormaps, warn if they are present
            if(settings.biomes != null || settings.grid != null || settings.blocks != null) {
                log.warn("{}: found `biomes`, `grid`, or `blocks` properties in a provided colormap; these will be ignored", id);
            }
            settings.biomes = null;
            settings.grid = null;
            settings.blocks = Collections.emptyList();
        }
        if(settings.source == null) {
            settings.source = makeSourceFromFileName(id);
        }
        settings.source = PropertyUtil.resolve(settings.source, id);
        return new ColormapProperties(id, settings);
    }

    private static String makeSourceFromFileName(Identifier id) {
        String path = id.toString();
        path = path.substring(0, path.lastIndexOf('.')) + ".png";
        return path;
    }

    private static class Settings {

        Format format = null;
        Collection<ApplicableBlockStates> blocks = null;
        String source = null;
        HexColor color = null;
        ColumnLayout layout = null;
        int yVariance = 0;
        int yOffset = 0;
        Map<Identifier, Integer> biomes = null;
        List<GridEntry> grid = null;
    }
}
