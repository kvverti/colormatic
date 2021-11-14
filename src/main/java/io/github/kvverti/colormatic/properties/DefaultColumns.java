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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public final class DefaultColumns {

    private static final Logger log = LogManager.getLogger();

    /**
     * Mapping of dynamic biomes to nearest vanilla columns.
     */
    private static final Map<Identifier, ColormapProperties.ColumnBounds> dynamicColumns = new HashMap<>();

    /**
     * The default mapping of biomes to columns.
     */
    private static final Map<Identifier, ColormapProperties.ColumnBounds> vanillaColumns = createVanillaColumnBounds();

    private DefaultColumns() {
    }

    public static ColormapProperties.ColumnBounds getBounds(RegistryKey<Biome> biomeKey, Registry<Biome> biomeRegistry, boolean optifine) {
        var bounds = vanillaColumns.get(biomeKey.getValue());
        if(bounds == null) {
            if(optifine) {
                // Optifine computes grid colors using the raw ID
                int rawID = biomeRegistry.getRawId(biomeRegistry.get(biomeKey));
                return new ColormapProperties.ColumnBounds(rawID, 1);
            } else {
                // Colormatic computes grid colors using temperature-humidity distance
                bounds = dynamicColumns.get(biomeKey.getValue());
                if(bounds == null) {
                    // this exception tends to trigger a crash in the crash report generator due to it
                    // happening off-thread, so we log before bailing
                    var msg = "No column bounds for dynamic biome: " + biomeKey.getValue();
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        return bounds;
    }

    /**
     * Called when the dynamic registry manager is changed to re-generate default column bounds.
     * This keeps column bounds in sync with the available biome list.
     *
     * @param manager The dynamic registry manager.
     */
    public static void reloadDefaultColumnBounds(DynamicRegistryManager manager) {
        dynamicColumns.clear();
        if(manager != null) {
            var biomeRegistry = manager.get(Registry.BIOME_KEY);
            for(var entry : biomeRegistry.getEntries()) {
                var key = entry.getKey();
                if(!vanillaColumns.containsKey(key.getValue())) {
                    dynamicColumns.put(key.getValue(), computeClosestDefaultBiome(key, biomeRegistry));
                }
            }
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
    private static ColormapProperties.ColumnBounds computeClosestDefaultBiome(RegistryKey<Biome> biomeKey, Registry<Biome> biomeRegistry) {
        var customBiome = biomeRegistry.get(biomeKey);
        if(customBiome == null) {
            throw new IllegalStateException("Biome is not registered: " + biomeKey.getValue());
        }
        double temperature = customBiome.getTemperature();
        double humidity = MathHelper.clamp(customBiome.getDownfall(), 0.0, 1.0);
        double minDistanceSq = Double.POSITIVE_INFINITY;
        ColormapProperties.ColumnBounds minBounds = null;
        for(var entry : vanillaColumns.entrySet()) {
            var vanillaBiome = biomeRegistry.get(entry.getKey());
            if(vanillaBiome == null) {
                log.error("Vanilla biome is not registered????? : {}", entry.getKey());
                continue;
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
     * The vanilla biome column bounds, based on their raw IDs.
     */
    private static Map<Identifier, ColormapProperties.ColumnBounds> createVanillaColumnBounds() {
        // see the Minecraft Wiki (https://minecraft.gamepedia.com/Biome#Biome_IDs)
        // circa November 12, 2021
        // we keep the legacy column associations where possible, renaming biomes as appropriate
        var map = new HashMap<Identifier, ColormapProperties.ColumnBounds>();
        map.put(new Identifier("ocean"), new ColormapProperties.ColumnBounds(0, 1));
        map.put(new Identifier("plains"), new ColormapProperties.ColumnBounds(1, 1));
        map.put(new Identifier("desert"), new ColormapProperties.ColumnBounds(2, 1));
        map.put(new Identifier("windswept_hills"), new ColormapProperties.ColumnBounds(3, 1));
        map.put(new Identifier("forest"), new ColormapProperties.ColumnBounds(4, 1));
        map.put(new Identifier("taiga"), new ColormapProperties.ColumnBounds(5, 1));
        map.put(new Identifier("swamp"), new ColormapProperties.ColumnBounds(6, 1));
        map.put(new Identifier("river"), new ColormapProperties.ColumnBounds(7, 1));
        map.put(new Identifier("nether_wastes"), new ColormapProperties.ColumnBounds(8, 1));
        map.put(new Identifier("the_end"), new ColormapProperties.ColumnBounds(9, 1));
        map.put(new Identifier("frozen_ocean"), new ColormapProperties.ColumnBounds(10, 1));
        map.put(new Identifier("frozen_river"), new ColormapProperties.ColumnBounds(11, 1));
        map.put(new Identifier("snowy_plains"), new ColormapProperties.ColumnBounds(12, 1));
        map.put(new Identifier("mushroom_fields"), new ColormapProperties.ColumnBounds(14, 1));
        map.put(new Identifier("beach"), new ColormapProperties.ColumnBounds(16, 1));
        map.put(new Identifier("jungle"), new ColormapProperties.ColumnBounds(21, 1));
        map.put(new Identifier("sparse_jungle"), new ColormapProperties.ColumnBounds(23, 1));
        map.put(new Identifier("deep_ocean"), new ColormapProperties.ColumnBounds(24, 1));
        map.put(new Identifier("stony_shore"), new ColormapProperties.ColumnBounds(25, 1));
        map.put(new Identifier("snowy_beach"), new ColormapProperties.ColumnBounds(26, 1));
        map.put(new Identifier("birch_forest"), new ColormapProperties.ColumnBounds(27, 1));
        map.put(new Identifier("dark_forest"), new ColormapProperties.ColumnBounds(29, 1));
        map.put(new Identifier("snowy_taiga"), new ColormapProperties.ColumnBounds(30, 1));
        map.put(new Identifier("old_growth_pine_taiga"), new ColormapProperties.ColumnBounds(32, 1));
        map.put(new Identifier("windswept_forest"), new ColormapProperties.ColumnBounds(34, 1));
        map.put(new Identifier("savanna"), new ColormapProperties.ColumnBounds(35, 1));
        map.put(new Identifier("savanna_plateau"), new ColormapProperties.ColumnBounds(36, 1));
        map.put(new Identifier("badlands"), new ColormapProperties.ColumnBounds(37, 1));
        map.put(new Identifier("wooded_badlands"), new ColormapProperties.ColumnBounds(38, 1));
        map.put(new Identifier("small_end_islands"), new ColormapProperties.ColumnBounds(40, 1));
        map.put(new Identifier("end_midlands"), new ColormapProperties.ColumnBounds(41, 1));
        map.put(new Identifier("end_highlands"), new ColormapProperties.ColumnBounds(42, 1));
        map.put(new Identifier("end_barrens"), new ColormapProperties.ColumnBounds(43, 1));
        map.put(new Identifier("warm_ocean"), new ColormapProperties.ColumnBounds(44, 1));
        map.put(new Identifier("lukewarm_ocean"), new ColormapProperties.ColumnBounds(45, 1));
        map.put(new Identifier("cold_ocean"), new ColormapProperties.ColumnBounds(46, 1));
        map.put(new Identifier("deep_lukewarm_ocean"), new ColormapProperties.ColumnBounds(48, 1));
        map.put(new Identifier("deep_cold_ocean"), new ColormapProperties.ColumnBounds(49, 1));
        map.put(new Identifier("deep_frozen_ocean"), new ColormapProperties.ColumnBounds(50, 1));
        // formerly "mutated" variants of biomes, normal biome ID + 128, except for
        // the post-1.7 biome additions.
        map.put(new Identifier("the_void"), new ColormapProperties.ColumnBounds(127, 1));
        map.put(new Identifier("sunflower_plains"), new ColormapProperties.ColumnBounds(129, 1));
        map.put(new Identifier("windswept_gravelly_hills"), new ColormapProperties.ColumnBounds(131, 1));
        map.put(new Identifier("flower_forest"), new ColormapProperties.ColumnBounds(132, 1));
        map.put(new Identifier("ice_spikes"), new ColormapProperties.ColumnBounds(140, 1));
        map.put(new Identifier("old_growth_birch_forest"), new ColormapProperties.ColumnBounds(155, 1));
        map.put(new Identifier("old_growth_spruce_taiga"), new ColormapProperties.ColumnBounds(160, 1));
        map.put(new Identifier("windswept_savanna"), new ColormapProperties.ColumnBounds(163, 1));
        map.put(new Identifier("eroded_badlands"), new ColormapProperties.ColumnBounds(165, 1));
        map.put(new Identifier("bamboo_jungle"), new ColormapProperties.ColumnBounds(168, 1));
        // 1.16 nether biomes
        map.put(new Identifier("soul_sand_valley"), new ColormapProperties.ColumnBounds(170, 1));
        map.put(new Identifier("crimson_forest"), new ColormapProperties.ColumnBounds(171, 1));
        map.put(new Identifier("warped_forest"), new ColormapProperties.ColumnBounds(172, 1));
        map.put(new Identifier("basalt_deltas"), new ColormapProperties.ColumnBounds(173, 1));
        // 1.17 cave biomes
        map.put(new Identifier("dripstone_caves"), new ColormapProperties.ColumnBounds(174, 1));
        map.put(new Identifier("lush_caves"), new ColormapProperties.ColumnBounds(175, 1));
        // 1.18 highland biomes (1.18 raw IDs 28-33, but these were already used)
        map.put(new Identifier("meadow"), new ColormapProperties.ColumnBounds(176, 1));
        map.put(new Identifier("grove"), new ColormapProperties.ColumnBounds(177, 1));
        map.put(new Identifier("snowy_slopes"), new ColormapProperties.ColumnBounds(178, 1));
        map.put(new Identifier("frozen_peaks"), new ColormapProperties.ColumnBounds(179, 1));
        map.put(new Identifier("jagged_peaks"), new ColormapProperties.ColumnBounds(180, 1));
        map.put(new Identifier("stony_peaks"), new ColormapProperties.ColumnBounds(181, 1));
        return map;
    }
}
