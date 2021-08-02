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

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.StringIdentifiable;

/**
 * Type adapter factory for enum values that implement StringIdentifiable
 */
public class StringIdentifiableTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> cls = type.getRawType();
        if(cls.isEnum()) {
            Class<?>[] implemented = cls.getInterfaces();
            for(Class<?> iface : implemented) {
                if(iface == StringIdentifiable.class) {
                    return (TypeAdapter<T>)new StringIdentifiableTypeAdapter<>(cls);
                }
            }
        }
        return null;
    }

    private static class StringIdentifiableTypeAdapter<T extends Enum<T> & StringIdentifiable> extends TypeAdapter<T> {

        private final T[] values;

        @SuppressWarnings({ "ConstantConditions", "unchecked" })
        StringIdentifiableTypeAdapter(Class<?> cls) {
            values = (T[])cls.getEnumConstants();
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            if(value == null) {
                out.nullValue();
            } else {
                out.value(value.asString());
            }
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if(in.peek() == JsonToken.NULL) {
                in.nextNull();
                throw new JsonSyntaxException(new NullPointerException("Required nonnull"));
            }
            String name = in.nextString();
            for(T t : values) {
                if(t.asString().equals(name)) {
                    return t;
                }
            }
            throw new JsonSyntaxException(new IllegalArgumentException(name));
        }
    }
}
