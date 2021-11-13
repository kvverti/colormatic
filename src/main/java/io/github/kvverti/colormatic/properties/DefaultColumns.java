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
                    throw new IllegalArgumentException("No column bounds for dynamic biome: " + biomeKey.getValue());
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
        // circa July 31, 2020
        // todo: update this to include the new biome IDs and add a migration aid
        var map = new HashMap<Identifier, ColormapProperties.ColumnBounds>();
        map.put(new Identifier("ocean"), new ColormapProperties.ColumnBounds(0, 1));
        map.put(new Identifier("plains"), new ColormapProperties.ColumnBounds(1, 1));
        map.put(new Identifier("desert"), new ColormapProperties.ColumnBounds(2, 1));
        map.put(new Identifier("mountains"), new ColormapProperties.ColumnBounds(3, 1));
        map.put(new Identifier("forest"), new ColormapProperties.ColumnBounds(4, 1));
        map.put(new Identifier("taiga"), new ColormapProperties.ColumnBounds(5, 1));
        map.put(new Identifier("swamp"), new ColormapProperties.ColumnBounds(6, 1));
        map.put(new Identifier("river"), new ColormapProperties.ColumnBounds(7, 1));
        map.put(new Identifier("nether_wastes"), new ColormapProperties.ColumnBounds(8, 1));
        map.put(new Identifier("the_end"), new ColormapProperties.ColumnBounds(9, 1));
        map.put(new Identifier("frozen_ocean"), new ColormapProperties.ColumnBounds(10, 1));
        map.put(new Identifier("frozen_river"), new ColormapProperties.ColumnBounds(11, 1));
        map.put(new Identifier("snowy_tundra"), new ColormapProperties.ColumnBounds(12, 1));
        map.put(new Identifier("snowy_mountains"), new ColormapProperties.ColumnBounds(13, 1));
        map.put(new Identifier("mushroom_fields"), new ColormapProperties.ColumnBounds(14, 1));
        map.put(new Identifier("mushroom_field_shore"), new ColormapProperties.ColumnBounds(15, 1));
        map.put(new Identifier("beach"), new ColormapProperties.ColumnBounds(16, 1));
        map.put(new Identifier("desert_hills"), new ColormapProperties.ColumnBounds(17, 1));
        map.put(new Identifier("wooded_hills"), new ColormapProperties.ColumnBounds(18, 1));
        map.put(new Identifier("taiga_hills"), new ColormapProperties.ColumnBounds(19, 1));
        map.put(new Identifier("mountain_edge"), new ColormapProperties.ColumnBounds(20, 1));
        map.put(new Identifier("jungle"), new ColormapProperties.ColumnBounds(21, 1));
        map.put(new Identifier("jungle_hills"), new ColormapProperties.ColumnBounds(22, 1));
        map.put(new Identifier("jungle_edge"), new ColormapProperties.ColumnBounds(23, 1));
        map.put(new Identifier("deep_ocean"), new ColormapProperties.ColumnBounds(24, 1));
        map.put(new Identifier("stone_shore"), new ColormapProperties.ColumnBounds(25, 1));
        map.put(new Identifier("snowy_beach"), new ColormapProperties.ColumnBounds(26, 1));
        map.put(new Identifier("birch_forest"), new ColormapProperties.ColumnBounds(27, 1));
        map.put(new Identifier("birch_forest_hills"), new ColormapProperties.ColumnBounds(28, 1));
        map.put(new Identifier("dark_forest"), new ColormapProperties.ColumnBounds(29, 1));
        map.put(new Identifier("snowy_taiga"), new ColormapProperties.ColumnBounds(30, 1));
        map.put(new Identifier("snowy_taiga_hills"), new ColormapProperties.ColumnBounds(31, 1));
        map.put(new Identifier("giant_tree_taiga"), new ColormapProperties.ColumnBounds(32, 1));
        map.put(new Identifier("giant_tree_taiga_hills"), new ColormapProperties.ColumnBounds(33, 1));
        map.put(new Identifier("wooded_mountains"), new ColormapProperties.ColumnBounds(34, 1));
        map.put(new Identifier("savanna"), new ColormapProperties.ColumnBounds(35, 1));
        map.put(new Identifier("savanna_plateau"), new ColormapProperties.ColumnBounds(36, 1));
        map.put(new Identifier("badlands"), new ColormapProperties.ColumnBounds(37, 1));
        map.put(new Identifier("wooded_badlands_plateau"), new ColormapProperties.ColumnBounds(38, 1));
        map.put(new Identifier("badlands_plateau"), new ColormapProperties.ColumnBounds(39, 1));
        map.put(new Identifier("small_end_islands"), new ColormapProperties.ColumnBounds(40, 1));
        map.put(new Identifier("end_midlands"), new ColormapProperties.ColumnBounds(41, 1));
        map.put(new Identifier("end_highlands"), new ColormapProperties.ColumnBounds(42, 1));
        map.put(new Identifier("end_barrens"), new ColormapProperties.ColumnBounds(43, 1));
        map.put(new Identifier("warm_ocean"), new ColormapProperties.ColumnBounds(44, 1));
        map.put(new Identifier("lukewarm_ocean"), new ColormapProperties.ColumnBounds(45, 1));
        map.put(new Identifier("cold_ocean"), new ColormapProperties.ColumnBounds(46, 1));
        map.put(new Identifier("deep_warm_ocean"), new ColormapProperties.ColumnBounds(47, 1));
        map.put(new Identifier("deep_lukewarm_ocean"), new ColormapProperties.ColumnBounds(48, 1));
        map.put(new Identifier("deep_cold_ocean"), new ColormapProperties.ColumnBounds(49, 1));
        map.put(new Identifier("deep_frozen_ocean"), new ColormapProperties.ColumnBounds(50, 1));
        // formerly "mutated" variants of biomes, normal biome ID + 128, except for
        // the post-1.7 biome additions.
        map.put(new Identifier("the_void"), new ColormapProperties.ColumnBounds(127, 1));
        map.put(new Identifier("sunflower_plains"), new ColormapProperties.ColumnBounds(129, 1));
        map.put(new Identifier("desert_lakes"), new ColormapProperties.ColumnBounds(130, 1));
        map.put(new Identifier("gravelly_mountains"), new ColormapProperties.ColumnBounds(131, 1));
        map.put(new Identifier("flower_forest"), new ColormapProperties.ColumnBounds(132, 1));
        map.put(new Identifier("swamp_hills"), new ColormapProperties.ColumnBounds(133, 1));
        map.put(new Identifier("ice_spikes"), new ColormapProperties.ColumnBounds(140, 1));
        map.put(new Identifier("modified_jungle"), new ColormapProperties.ColumnBounds(149, 1));
        map.put(new Identifier("modified_jungle_edge"), new ColormapProperties.ColumnBounds(151, 1));
        map.put(new Identifier("tall_birch_forest"), new ColormapProperties.ColumnBounds(155, 1));
        map.put(new Identifier("tall_birch_hills"), new ColormapProperties.ColumnBounds(156, 1));
        map.put(new Identifier("dark_forest_hills"), new ColormapProperties.ColumnBounds(157, 1));
        map.put(new Identifier("snowy_taiga_mountains"), new ColormapProperties.ColumnBounds(158, 1));
        map.put(new Identifier("giant_spruce_taiga"), new ColormapProperties.ColumnBounds(160, 1));
        map.put(new Identifier("giant_spruce_taiga_hills"), new ColormapProperties.ColumnBounds(161, 1));
        map.put(new Identifier("modified_gravelly_mountains"), new ColormapProperties.ColumnBounds(162, 1));
        map.put(new Identifier("shattered_savanna"), new ColormapProperties.ColumnBounds(163, 1));
        map.put(new Identifier("shattered_savanna_plateau"), new ColormapProperties.ColumnBounds(164, 1));
        map.put(new Identifier("eroded_badlands"), new ColormapProperties.ColumnBounds(165, 1));
        map.put(new Identifier("modified_wooded_badlands_plateau"), new ColormapProperties.ColumnBounds(166, 1));
        map.put(new Identifier("modified_badlands_plateau"), new ColormapProperties.ColumnBounds(167, 1));
        map.put(new Identifier("bamboo_jungle"), new ColormapProperties.ColumnBounds(168, 1));
        map.put(new Identifier("bamboo_jungle_hills"), new ColormapProperties.ColumnBounds(169, 1));
        map.put(new Identifier("soul_sand_valley"), new ColormapProperties.ColumnBounds(170, 1));
        map.put(new Identifier("crimson_forest"), new ColormapProperties.ColumnBounds(171, 1));
        map.put(new Identifier("warped_forest"), new ColormapProperties.ColumnBounds(172, 1));
        map.put(new Identifier("basalt_deltas"), new ColormapProperties.ColumnBounds(173, 1));
        return map;
    }
}
