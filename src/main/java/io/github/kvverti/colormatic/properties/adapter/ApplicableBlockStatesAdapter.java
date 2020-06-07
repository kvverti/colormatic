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
package io.github.kvverti.colormatic.properties.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.properties.ApplicableBlockStates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

public class ApplicableBlockStatesAdapter extends TypeAdapter<ApplicableBlockStates> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void write(JsonWriter out, ApplicableBlockStates value) throws IOException {
        throw new UnsupportedOperationException("write");
    }

    @Override
    public ApplicableBlockStates read(JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            throw new JsonSyntaxException("required nonnull");
        }
        String s = in.nextString();
        return from(s);
    }

    /**
     * Tests a single element of a block state predicate list. For example,
     * these elements may be `stone`, `minecraft:grass_block`, `lever:attach=wall:facing=east,west`
     *
     * @throws JsonSyntaxException if the input is malformed
     */
    private static ApplicableBlockStates from(String blockDesc) {
        ApplicableBlockStates res = new ApplicableBlockStates();
        Block b;
        String[] parts = blockDesc.split(":");
        int bgnIdx;
        try {
            if(parts.length > 1 && parts[1].indexOf('=') < 0) {
                // a qualified name like `minecraft:grass_block:snowy=false`
                Identifier id = new Identifier(parts[0], parts[1]);
                if(parts[0].equals(Colormatic.MODID)) {
                    initSpecialBlockStates(res, id, parts);
                    return res;
                } else {
                    b = Registry.BLOCK.get(id);
                }
                bgnIdx = 2;
            } else {
                // an unqualified name like `grass_block:snowy=false`
                b = Registry.BLOCK.get(new Identifier(parts[0]));
                bgnIdx = 1;
            }
        } catch(InvalidIdentifierException e) {
            throw new JsonSyntaxException("Invalid block identifier: " + blockDesc, e);
        }
        res.block = b;
        BlockStatePredicate pred = BlockStatePredicate.forBlock(b);
        for(int i = bgnIdx; i < parts.length; i++) {
            int split = parts[i].indexOf('=');
            if(split < 0) {
                throw new JsonSyntaxException("Invalid property syntax: " + parts[i]);
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
                throw new JsonSyntaxException("Invalid property: " + propStr);
            }
            String[] propValues = parts[i].substring(split + 1).split(",");
            List<Comparable<?>> ls = new ArrayList<>();
            for(String s : propValues) {
                putPropValue(prop, s, ls);
            }
            pred = pred.with(prop, ls::contains);
        }
        // if this applies to all states, the states list is empty
        res.states = new ArrayList<>();
        boolean excluded = false;
        for(BlockState state : b.getStateManager().getStates()) {
            if(pred.test(state)) {
                res.states.add(state);
            } else {
                excluded = true;
            }
        }
        if(!excluded) {
            res.states.clear();
        }
        return res;
    }

    private static void initSpecialBlockStates(ApplicableBlockStates res, Identifier id, String[] parts) {
        res.specialKey = id;
        // parts[0] == id namespace, parts[1] == id path
        if(parts.length != 3) {
            logger.warn("Special identifier does not specify sole property: {}", Arrays.toString(parts));
        } else {
            for(int i = 2; i < parts.length; i++) {
                int split = parts[i].indexOf('=');
                if(split < 0) {
                    throw new JsonSyntaxException("Invalid property syntax: " + parts[i]);
                }
                String[] propValues = parts[i].substring(split + 1).split(",");
                for(String value : propValues) {
                    Identifier val = Identifier.tryParse(value.replaceFirst("/", ":"));
                    if(val == null) {
                        throw new JsonSyntaxException("Invalid identifier value: " + value);
                    }
                    res.specialIds.add(val);
                }
            }
        }
    }

    private static <T extends Comparable<T>> void putPropValue(Property<T> prop, String s, List<? super T> values) {
        Optional<T> value = prop.parse(s);
        if(value.isPresent()) {
            values.add(value.get());
        } else {
            throw new JsonSyntaxException("Invalid property value: " + s);
        }
    }
}
