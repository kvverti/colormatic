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

import io.github.kvverti.colormatic.properties.predicate.InvalidPredicateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PropertyUtil {

    /**
     * Tests a single element of a block state predicate list. For example,
     * these elements may be `stone`, `minecraft:grass_block`, `lever:attach=wall:facing=east,west`
     */
    public static BlockStatePredicate createBlockPredicate(String blockDesc) {
        Block b;
        String[] parts = blockDesc.split(":");
        int bgnIdx;
        if(parts.length > 1 && parts[1].indexOf('=') < 0) {
            // a qualified name like `minecraft:grass_block:snowy=false`
            b = Registry.BLOCK.get(new Identifier(parts[0], parts[1]));
            bgnIdx = 2;
        } else {
            // an unqualified name like `grass_block:snowy=false`
            b = Registry.BLOCK.get(new Identifier(parts[0]));
            bgnIdx = 1;
        }
        if(b == null) {
            throw new InvalidPredicateException("Block not found: " + blockDesc);
        }
        BlockStatePredicate res = BlockStatePredicate.forBlock(b);
        for(int i = bgnIdx; i < parts.length; i++) {
            int split = parts[i].indexOf('=');
            if(split < 0) {
                throw new InvalidPredicateException("Invalid property syntax: " + parts[i]);
            }
            String propStr = parts[i].substring(0, split);
            Property<?> prop = null;
            for(Property<?> p : b.getDefaultState().getProperties()) {
                if(p.getName().equals(propStr)) {
                    prop = p;
                    break;
                }
            }
            if(prop == null) {
                throw new InvalidPredicateException("Invalid property: " + propStr);
            }
            String[] propValues = parts[i].substring(split + 1).split(",");
            List<Comparable<?>> ls = new ArrayList<>();
            for(String s : propValues) {
                putPropValue(prop, s, ls);
            }
            res = res.with(prop, ls::contains);
        }
        return res;
    }

    private static <T extends Comparable<T>> void putPropValue(Property<T> prop, String s, List<? super T> values) {
        Optional<T> value = prop.getValue(s);
        if(value.isPresent()) {
            values.add(value.get());
        } else {
            throw new InvalidPredicateException("Invalid property value: " + s);
        }
    }
}
