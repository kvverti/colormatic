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
package io.github.kvverti.colormatic.properties.adapter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.MapColor;

public class MaterialColorAdapter extends TypeAdapter<MapColor> {

    // MaterialColors do not store their colors, so we store them here
    private static final Map<String, MapColor> materialColors;

    @Override
    public MapColor read(JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            throw new JsonSyntaxException(new NullPointerException("Required nonnull"));
        }
        String s = in.nextString();
        return materialColors.get(s);
    }

    @Override
    public void write(JsonWriter out, MapColor value) throws IOException {
        throw new UnsupportedOperationException("write");
    }

    static {
        materialColors = new HashMap<>();
        materialColors.put("air", MapColor.CLEAR);
        materialColors.put("grass", MapColor.PALE_GREEN);
        materialColors.put("sand", MapColor.PALE_YELLOW);
        materialColors.put("cloth", MapColor.WHITE_GRAY);
        materialColors.put("tnt", MapColor.BRIGHT_RED);
        materialColors.put("ice", MapColor.PALE_PURPLE);
        materialColors.put("iron", MapColor.IRON_GRAY);
        materialColors.put("foliage", MapColor.DARK_GREEN);
        materialColors.put("snow", MapColor.WHITE);
        materialColors.put("white", MapColor.WHITE);
        materialColors.put("clay", MapColor.LIGHT_BLUE_GRAY);
        materialColors.put("dirt", MapColor.DIRT_BROWN);
        materialColors.put("stone", MapColor.STONE_GRAY);
        materialColors.put("water", MapColor.WATER_BLUE);
        materialColors.put("wood", MapColor.OAK_TAN);
        materialColors.put("quartz", MapColor.OFF_WHITE);
        materialColors.put("adobe", MapColor.ORANGE);
        materialColors.put("orange", MapColor.ORANGE);
        materialColors.put("magenta", MapColor.MAGENTA);
        materialColors.put("light_blue", MapColor.LIGHT_BLUE);
        materialColors.put("yellow", MapColor.YELLOW);
        materialColors.put("lime", MapColor.LIME);
        materialColors.put("pink", MapColor.PINK);
        materialColors.put("gray", MapColor.GRAY);
        materialColors.put("light_gray", MapColor.LIGHT_GRAY);
        materialColors.put("cyan", MapColor.CYAN);
        materialColors.put("purple", MapColor.PURPLE);
        materialColors.put("blue", MapColor.BLUE);
        materialColors.put("brown", MapColor.BROWN);
        materialColors.put("green", MapColor.GREEN);
        materialColors.put("red", MapColor.RED);
        materialColors.put("black", MapColor.BLACK);
        materialColors.put("gold", MapColor.GOLD);
        materialColors.put("diamond", MapColor.DIAMOND_BLUE);
        materialColors.put("lapis", MapColor.LAPIS_BLUE);
        materialColors.put("emerald", MapColor.EMERALD_GREEN);
        materialColors.put("podzol", MapColor.SPRUCE_BROWN);
        materialColors.put("netherrack", MapColor.DARK_RED);
        materialColors.put("white_terracotta", MapColor.TERRACOTTA_WHITE);
        materialColors.put("orange_terracotta", MapColor.TERRACOTTA_ORANGE);
        materialColors.put("magenta_terracotta", MapColor.TERRACOTTA_MAGENTA);
        materialColors.put("light_blue_terracotta", MapColor.TERRACOTTA_LIGHT_BLUE);
        materialColors.put("yellow_terracotta", MapColor.TERRACOTTA_YELLOW);
        materialColors.put("lime_terracotta", MapColor.TERRACOTTA_LIME);
        materialColors.put("pink_terracotta", MapColor.TERRACOTTA_PINK);
        materialColors.put("gray_terracotta", MapColor.TERRACOTTA_GRAY);
        materialColors.put("light_gray_terracotta", MapColor.TERRACOTTA_LIGHT_GRAY);
        materialColors.put("cyan_terracotta", MapColor.TERRACOTTA_CYAN);
        materialColors.put("purple_terracotta", MapColor.TERRACOTTA_PURPLE);
        materialColors.put("blue_terracotta", MapColor.TERRACOTTA_BLUE);
        materialColors.put("brown_terracotta", MapColor.TERRACOTTA_BROWN);
        materialColors.put("green_terracotta", MapColor.TERRACOTTA_GREEN);
        materialColors.put("red_terracotta", MapColor.TERRACOTTA_RED);
        materialColors.put("black_terracotta", MapColor.TERRACOTTA_BLACK);
    }
}
