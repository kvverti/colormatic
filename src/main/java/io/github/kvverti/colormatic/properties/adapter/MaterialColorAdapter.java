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
package io.github.kvverti.colormatic.properties.adapter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.MaterialColor;

public class MaterialColorAdapter extends TypeAdapter<MaterialColor> {

    // MaterialColors do not store their colors, so we store them here
    private static final Map<String, MaterialColor> materialColors;

    @Override
    public MaterialColor read(JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            throw new JsonSyntaxException(new NullPointerException("Required nonnull"));
        }
        String s = in.nextString();
        return materialColors.get(s);
    }

    @Override
    public void write(JsonWriter out, MaterialColor value) throws IOException {
        throw new UnsupportedOperationException("write");
    }

    static {
        materialColors = new HashMap<>();
        materialColors.put("air", MaterialColor.AIR);
        materialColors.put("grass", MaterialColor.GRASS);
        materialColors.put("sand", MaterialColor.SAND);
        materialColors.put("cloth", MaterialColor.WEB);
        materialColors.put("tnt", MaterialColor.LAVA);
        materialColors.put("ice", MaterialColor.ICE);
        materialColors.put("iron", MaterialColor.IRON);
        materialColors.put("foliage", MaterialColor.FOLIAGE);
        materialColors.put("snow", MaterialColor.WHITE);
        materialColors.put("white", MaterialColor.WHITE);
        materialColors.put("clay", MaterialColor.CLAY);
        materialColors.put("dirt", MaterialColor.DIRT);
        materialColors.put("stone", MaterialColor.STONE);
        materialColors.put("water", MaterialColor.WATER);
        materialColors.put("wood", MaterialColor.WOOD);
        materialColors.put("quartz", MaterialColor.QUARTZ);
        materialColors.put("adobe", MaterialColor.ORANGE);
        materialColors.put("orange", MaterialColor.ORANGE);
        materialColors.put("magenta", MaterialColor.MAGENTA);
        materialColors.put("light_blue", MaterialColor.LIGHT_BLUE);
        materialColors.put("yellow", MaterialColor.YELLOW);
        materialColors.put("lime", MaterialColor.LIME);
        materialColors.put("pink", MaterialColor.PINK);
        materialColors.put("gray", MaterialColor.GRAY);
        materialColors.put("light_gray", MaterialColor.LIGHT_GRAY);
        materialColors.put("cyan", MaterialColor.CYAN);
        materialColors.put("purple", MaterialColor.PURPLE);
        materialColors.put("blue", MaterialColor.BLUE);
        materialColors.put("brown", MaterialColor.BROWN);
        materialColors.put("green", MaterialColor.GREEN);
        materialColors.put("red", MaterialColor.RED);
        materialColors.put("black", MaterialColor.BLACK);
        materialColors.put("gold", MaterialColor.GOLD);
        materialColors.put("diamond", MaterialColor.DIAMOND);
        materialColors.put("lapis", MaterialColor.LAPIS);
        materialColors.put("emerald", MaterialColor.EMERALD);
        materialColors.put("podzol", MaterialColor.SPRUCE);
        materialColors.put("netherrack", MaterialColor.NETHER);
        materialColors.put("white_terracotta", MaterialColor.WHITE_TERRACOTTA);
        materialColors.put("orange_terracotta", MaterialColor.ORANGE_TERRACOTTA);
        materialColors.put("magenta_terracotta", MaterialColor.MAGENTA_TERRACOTTA);
        materialColors.put("light_blue_terracotta", MaterialColor.LIGHT_BLUE_TERRACOTTA);
        materialColors.put("yellow_terracotta", MaterialColor.YELLOW_TERRACOTTA);
        materialColors.put("lime_terracotta", MaterialColor.LIME_TERRACOTTA);
        materialColors.put("pink_terracotta", MaterialColor.PINK_TERRACOTTA);
        materialColors.put("gray_terracotta", MaterialColor.GRAY_TERRACOTTA);
        materialColors.put("light_gray_terracotta", MaterialColor.LIGHT_GRAY_TERRACOTTA);
        materialColors.put("cyan_terracotta", MaterialColor.CYAN_TERRACOTTA);
        materialColors.put("purple_terracotta", MaterialColor.PURPLE_TERRACOTTA);
        materialColors.put("blue_terracotta", MaterialColor.BLUE_TERRACOTTA);
        materialColors.put("brown_terracotta", MaterialColor.BROWN_TERRACOTTA);
        materialColors.put("green_terracotta", MaterialColor.GREEN_TERRACOTTA);
        materialColors.put("red_terracotta", MaterialColor.RED_TERRACOTTA);
        materialColors.put("black_terracotta", MaterialColor.BLACK_TERRACOTTA);
    }
}
