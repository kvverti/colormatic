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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.kvverti.colormatic.Colormatic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * Keeps track of renamed and removed biomes so outdated resource packs can be warned and converted.
 */
public final class BiomeRenaming {

    private static final Logger log = LogManager.getLogger(Colormatic.MODID);

    private static final Set<Identifier> REMOVED_BIOMES = createRemovedBiomeSet();

    private static final Map<Identifier, Identifier> RENAMED_BIOMES = createRenamedBiomeMap();

    /**
     * Updates the given biome ID taking biome renames and removals into account.
     *
     * @param biomeId The possibly outdated biome ID.
     * @return The updated biome ID, or null if the biome was removed.
     */
    @Nullable
    public static Identifier updateName(Identifier biomeId, Identifier context) {
        if(REMOVED_BIOMES.contains(biomeId)) {
            log.warn("{}: Biome ID '{}' no longer exists and should be removed", context, biomeId);
            return null;
        }
        var renamed = RENAMED_BIOMES.get(biomeId);
        if(renamed != null) {
            log.warn("{}: Biome ID '{}' has been renamed to '{}' and should be updated", context, biomeId, renamed);
            return renamed;
        }
        return biomeId;
    }


    private static Set<Identifier> createRemovedBiomeSet() {
        var set = new HashSet<Identifier>();
        set.add(new Identifier("snowy_taiga_mountains"));
        set.add(new Identifier("giant_tree_taiga_hills"));
        set.add(new Identifier("taiga_hills"));
        set.add(new Identifier("modified_gravelly_mountains"));
        set.add(new Identifier("desert_hills"));
        set.add(new Identifier("snowy_taiga_hills"));
        set.add(new Identifier("jungle_hills"));
        set.add(new Identifier("mushroom_field_shore"));
        set.add(new Identifier("shattered_savanna_plateau"));
        set.add(new Identifier("mountain_edge"));
        set.add(new Identifier("wooded_hills"));
        set.add(new Identifier("bamboo_jungle_hills"));
        set.add(new Identifier("modified_wooded_badlands_plateau"));
        set.add(new Identifier("desert_lakes"));
        set.add(new Identifier("dark_forest_hills"));
        set.add(new Identifier("birch_forest_hills"));
        set.add(new Identifier("modified_badlands_plateau"));
        set.add(new Identifier("badlands_plateau"));
        set.add(new Identifier("modified_jungle_edge"));
        set.add(new Identifier("giant_spruce_taiga_hills"));
        set.add(new Identifier("swamp_hills"));
        set.add(new Identifier("modified_jungle"));
        set.add(new Identifier("tall_birch_hills"));
        set.add(new Identifier("snowy_mountains"));
        set.add(new Identifier("deep_warm_ocean"));
        return set;
    }

    private static Map<Identifier, Identifier> createRenamedBiomeMap() {
        var map = new HashMap<Identifier, Identifier>();
        map.put(new Identifier("mountains"), new Identifier("windswept_hills"));
        map.put(new Identifier("snowy_tundra"), new Identifier("snowy_plains"));
        map.put(new Identifier("jungle_edge"), new Identifier("sparse_jungle"));
        map.put(new Identifier("stone_shore"), new Identifier("stony_shore"));
        map.put(new Identifier("giant_tree_taiga"), new Identifier("old_growth_pine_taiga"));
        map.put(new Identifier("wooded_mountains"), new Identifier("windswept_forest"));
        map.put(new Identifier("wooded_badlands_plateau"), new Identifier("wooded_badlands"));
        map.put(new Identifier("gravelly_mountains"), new Identifier("windswept_gravelly_hills"));
        map.put(new Identifier("tall_birch_forest"), new Identifier("old_growth_birch_forest"));
        map.put(new Identifier("giant_spruce_taiga"), new Identifier("old_growth_spruce_taiga"));
        map.put(new Identifier("shattered_savanna"), new Identifier("windswept_savanna"));
        return map;
    }
}
