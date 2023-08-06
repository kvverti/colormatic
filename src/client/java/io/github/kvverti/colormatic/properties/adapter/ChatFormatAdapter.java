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

import net.minecraft.util.Formatting;

public class ChatFormatAdapter extends TypeAdapter<Formatting> {

    @Override
    public Formatting read(JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            throw new JsonSyntaxException(new NullPointerException("Required nonnull"));
        }
        String name = in.nextString();
        return Formatting.byName(name);
    }

    @Override
    public void write(JsonWriter out, Formatting value) throws IOException {
        if(value == null) {
            out.nullValue();
        } else {
            out.value(value.getName());
        }
    }
}
