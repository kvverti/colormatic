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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
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
     * The format of the corresponding colormap.
     */
    private final Format format;

    /**
     * A list of BlockStates that this colormap applies to. If not specified,
     * it is taken from the name of the properties file (minecraft namespace only).
     */
    private final Collection<ApplicableBlockStates> blocks;

    /**
     * s
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
     * The default mapping of biomes to columns.
     */
    private static final Map<Identifier, ColumnBounds> defaultColumns = new HashMap<>();

    static {
        initDefaultColumnBounds(defaultColumns);
    }

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
        this.format = settings.format;
        this.blocks = settings.blocks;
        this.source = new Identifier(settings.source);
        this.color = settings.color;
        this.yVariance = settings.yVariance;
        this.yOffset = settings.yOffset;
        if(settings.grid != null) {
            this.columnsByBiome = new HashMap<>();
            for(GridEntry entry : settings.grid) {
                ColumnBounds bounds = new ColumnBounds(entry.column, entry.width);
                for(Identifier biomeId : entry.biomes) {
                    columnsByBiome.put(biomeId, bounds);
                }
            }
        } else if(settings.biomes != null) {
            this.columnsByBiome = new HashMap<>();
            for(Map.Entry<Identifier, Integer> entry : settings.biomes.entrySet()) {
                columnsByBiome.put(entry.getKey(), new ColumnBounds(entry.getValue(), 1));
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
                    ColumnBounds defaultForBiome = defaultColumns.get(id);
                    if(defaultForBiome == null) {
                        if(this.id.getPath().endsWith(".properties")) {
                            // Optifine computes grid colors using the raw ID
                            int rawID = biomeRegistry.getRawId(biomeRegistry.get(biomeKey));
                            return new ColumnBounds(rawID, 1);
                        } else {
                            // Colormatic computes grid colors using temperature-humidity distance
                            return computeClosestDefaultBiome(biomeKey, biomeRegistry);
                        }
                    }
                    return defaultForBiome;
                }
            } else {
                return DEFAULT_BOUNDS;
            }
        } else {
            throw new IllegalStateException(format.toString());
        }
    }

    /**
     * Given a custom biome, finds the vanilla biome closest in temperature and humidity to the given
     * biome and returns its bounds.
     *
     * @param biomeKey      The key of the custom biome.
     * @param biomeRegistry The biome registry.
     * @return The bounds of the vanilla biome closest to the given biome.
     */
    private static ColumnBounds computeClosestDefaultBiome(RegistryKey<Biome> biomeKey, Registry<Biome> biomeRegistry) {
        var customBiome = biomeRegistry.get(biomeKey);
        if(customBiome == null) {
            throw new IllegalStateException("Biome is not registered: " + biomeKey.getValue());
        }
        double temperature = customBiome.getTemperature();
        double humidity = MathHelper.clamp(customBiome.getDownfall(), 0.0, 1.0);
        double minDistanceSq = Double.POSITIVE_INFINITY;
        ColumnBounds minBounds = null;
        for(var entry : defaultColumns.entrySet()) {
            var vanillaBiome = biomeRegistry.get(entry.getKey());
            if(vanillaBiome == null) {
                throw new IllegalStateException("Vanilla biome is not registered????? : " + entry.getKey());
            }
            var dTemperature = temperature - vanillaBiome.getTemperature();
            var dHumidity = humidity - MathHelper.clamp(vanillaBiome.getDownfall(), 0.0, 1.0);
            var thisDistanceSq = dTemperature * dTemperature + dHumidity * dHumidity;
            if(thisDistanceSq < minDistanceSq) {
                minDistanceSq = thisDistanceSq;
                minBounds = entry.getValue();
            }
        }
        return minBounds;
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

        public static Format byName(String name) {
            return switch(name) {
                case "fixed" -> FIXED;
                case "grid" -> GRID;
                default -> VANILLA;
            };
        }
    }

    /**
     * Loads the colormap properties defined by the given identifier. The properties
     * should be in JSON format. If not present, returns a default properties taken
     * from the identifier name.
     */
    public static ColormapProperties load(ResourceManager manager, Identifier id, boolean custom) {
        try(Resource rsc = manager.getResource(id); InputStream in = rsc.getInputStream()) {
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
        } catch(JsonSyntaxException e) {
            log.error("Error parsing {}: {}", id, e.getMessage());
            settings = new Settings();
        }
        if(settings.format == null) {
            settings.format = Colormatic.COLOR_PROPS.getProperties().getDefaultFormat();
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
            // disable `blocks`, `grid`, and `biomes` for non-custom colormaps
            settings.biomes = null;
            settings.grid = null;
            settings.blocks = Collections.emptyList();
        }
        if(settings.source == null) {
            String path = id.toString();
            path = path.substring(0, path.lastIndexOf('.')) + ".png";
            settings.source = path;
        }
        settings.source = PropertyUtil.resolve(settings.source, id);
        return new ColormapProperties(id, settings);
    }

    private static class Settings {

        Format format = null;
        Collection<ApplicableBlockStates> blocks = null;
        String source = null;
        HexColor color = null;
        int yVariance = 0;
        int yOffset = 0;
        @Deprecated
        Map<Identifier, Integer> biomes = null;
        List<GridEntry> grid = null;
    }

    private static class GridEntry {
        List<Identifier> biomes = Collections.emptyList();
        int column = 0;
        int width = 1;
    }

    private static void initDefaultColumnBounds(Map<Identifier, ColumnBounds> map) {
        // see the Minecraft Wiki (https://minecraft.gamepedia.com/Biome#Biome_IDs)
        // circa July 31, 2020
        map.put(new Identifier("ocean"), new ColumnBounds(0, 1));
        map.put(new Identifier("plains"), new ColumnBounds(1, 1));
        map.put(new Identifier("desert"), new ColumnBounds(2, 1));
        map.put(new Identifier("mountains"), new ColumnBounds(3, 1));
        map.put(new Identifier("forest"), new ColumnBounds(4, 1));
        map.put(new Identifier("taiga"), new ColumnBounds(5, 1));
        map.put(new Identifier("swamp"), new ColumnBounds(6, 1));
        map.put(new Identifier("river"), new ColumnBounds(7, 1));
        map.put(new Identifier("nether_wastes"), new ColumnBounds(8, 1));
        map.put(new Identifier("the_end"), new ColumnBounds(9, 1));
        map.put(new Identifier("frozen_ocean"), new ColumnBounds(10, 1));
        map.put(new Identifier("frozen_river"), new ColumnBounds(11, 1));
        map.put(new Identifier("snowy_tundra"), new ColumnBounds(12, 1));
        map.put(new Identifier("snowy_mountains"), new ColumnBounds(13, 1));
        map.put(new Identifier("mushroom_fields"), new ColumnBounds(14, 1));
        map.put(new Identifier("mushroom_field_shore"), new ColumnBounds(15, 1));
        map.put(new Identifier("beach"), new ColumnBounds(16, 1));
        map.put(new Identifier("desert_hills"), new ColumnBounds(17, 1));
        map.put(new Identifier("wooded_hills"), new ColumnBounds(18, 1));
        map.put(new Identifier("taiga_hills"), new ColumnBounds(19, 1));
        map.put(new Identifier("mountain_edge"), new ColumnBounds(20, 1));
        map.put(new Identifier("jungle"), new ColumnBounds(21, 1));
        map.put(new Identifier("jungle_hills"), new ColumnBounds(22, 1));
        map.put(new Identifier("jungle_edge"), new ColumnBounds(23, 1));
        map.put(new Identifier("deep_ocean"), new ColumnBounds(24, 1));
        map.put(new Identifier("stone_shore"), new ColumnBounds(25, 1));
        map.put(new Identifier("snowy_beach"), new ColumnBounds(26, 1));
        map.put(new Identifier("birch_forest"), new ColumnBounds(27, 1));
        map.put(new Identifier("birch_forest_hills"), new ColumnBounds(28, 1));
        map.put(new Identifier("dark_forest"), new ColumnBounds(29, 1));
        map.put(new Identifier("snowy_taiga"), new ColumnBounds(30, 1));
        map.put(new Identifier("snowy_taiga_hills"), new ColumnBounds(31, 1));
        map.put(new Identifier("giant_tree_taiga"), new ColumnBounds(32, 1));
        map.put(new Identifier("giant_tree_taiga_hills"), new ColumnBounds(33, 1));
        map.put(new Identifier("wooded_mountains"), new ColumnBounds(34, 1));
        map.put(new Identifier("savanna"), new ColumnBounds(35, 1));
        map.put(new Identifier("savanna_plateau"), new ColumnBounds(36, 1));
        map.put(new Identifier("badlands"), new ColumnBounds(37, 1));
        map.put(new Identifier("wooded_badlands_plateau"), new ColumnBounds(38, 1));
        map.put(new Identifier("badlands_plateau"), new ColumnBounds(39, 1));
        map.put(new Identifier("small_end_islands"), new ColumnBounds(40, 1));
        map.put(new Identifier("end_midlands"), new ColumnBounds(41, 1));
        map.put(new Identifier("end_highlands"), new ColumnBounds(42, 1));
        map.put(new Identifier("end_barrens"), new ColumnBounds(43, 1));
        map.put(new Identifier("warm_ocean"), new ColumnBounds(44, 1));
        map.put(new Identifier("lukewarm_ocean"), new ColumnBounds(45, 1));
        map.put(new Identifier("cold_ocean"), new ColumnBounds(46, 1));
        map.put(new Identifier("deep_warm_ocean"), new ColumnBounds(47, 1));
        map.put(new Identifier("deep_lukewarm_ocean"), new ColumnBounds(48, 1));
        map.put(new Identifier("deep_cold_ocean"), new ColumnBounds(49, 1));
        map.put(new Identifier("deep_frozen_ocean"), new ColumnBounds(50, 1));
        // formerly "mutated" variants of biomes, normal biome ID + 128, except for
        // the post-1.7 biome additions.
        map.put(new Identifier("the_void"), new ColumnBounds(127, 1));
        map.put(new Identifier("sunflower_plains"), new ColumnBounds(129, 1));
        map.put(new Identifier("desert_lakes"), new ColumnBounds(130, 1));
        map.put(new Identifier("gravelly_mountains"), new ColumnBounds(131, 1));
        map.put(new Identifier("flower_forest"), new ColumnBounds(132, 1));
        map.put(new Identifier("swamp_hills"), new ColumnBounds(133, 1));
        map.put(new Identifier("ice_spikes"), new ColumnBounds(140, 1));
        map.put(new Identifier("modified_jungle"), new ColumnBounds(149, 1));
        map.put(new Identifier("modified_jungle_edge"), new ColumnBounds(151, 1));
        map.put(new Identifier("tall_birch_forest"), new ColumnBounds(155, 1));
        map.put(new Identifier("tall_birch_hills"), new ColumnBounds(156, 1));
        map.put(new Identifier("dark_forest_hills"), new ColumnBounds(157, 1));
        map.put(new Identifier("snowy_taiga_mountains"), new ColumnBounds(158, 1));
        map.put(new Identifier("giant_spruce_taiga"), new ColumnBounds(160, 1));
        map.put(new Identifier("giant_spruce_taiga_hills"), new ColumnBounds(161, 1));
        map.put(new Identifier("modified_gravelly_mountains"), new ColumnBounds(162, 1));
        map.put(new Identifier("shattered_savanna"), new ColumnBounds(163, 1));
        map.put(new Identifier("shattered_savanna_plateau"), new ColumnBounds(164, 1));
        map.put(new Identifier("eroded_badlands"), new ColumnBounds(165, 1));
        map.put(new Identifier("modified_wooded_badlands_plateau"), new ColumnBounds(166, 1));
        map.put(new Identifier("modified_badlands_plateau"), new ColumnBounds(167, 1));
        map.put(new Identifier("bamboo_jungle"), new ColumnBounds(168, 1));
        map.put(new Identifier("bamboo_jungle_hills"), new ColumnBounds(169, 1));
        map.put(new Identifier("soul_sand_valley"), new ColumnBounds(170, 1));
        map.put(new Identifier("crimson_forest"), new ColumnBounds(171, 1));
        map.put(new Identifier("warped_forest"), new ColumnBounds(172, 1));
        map.put(new Identifier("basalt_deltas"), new ColumnBounds(173, 1));
    }
}
