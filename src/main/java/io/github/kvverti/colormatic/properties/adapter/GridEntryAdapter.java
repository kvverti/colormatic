/*
 * Colormatic
 * Copyright (C) 2022  Thalia Nero
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
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.kvverti.colormatic.properties.GridEntry;

public class GridEntryAdapter extends TypeAdapter<GridEntry> {

    private final IdentifierAdapter idAdapter = new IdentifierAdapter();

    @Override
    public void write(JsonWriter jsonWriter, GridEntry gridEntry) {
        throw new UnsupportedOperationException("write");
    }

    @Override
    public GridEntry read(JsonReader in) throws IOException {
        switch(in.peek()) {
            case NULL -> {
                in.nextNull();
                throw new JsonSyntaxException("required nonnull");
            }
            case STRING -> {
                var biomeId = this.idAdapter.read(in);
                var gridEntry = new GridEntry();
                gridEntry.biomes = ImmutableList.of(biomeId);
                return gridEntry;
            }
            default -> {
                var gridEntry = new GridEntry();
                in.beginObject();
                while(in.hasNext()) {
                    switch(in.nextName()) {
                        case "biomes" -> {
                            gridEntry.biomes = new ArrayList<>();
                            in.beginArray();
                            while(in.hasNext()) {
                                gridEntry.biomes.add(this.idAdapter.read(in));
                            }
                            in.endArray();
                        }
                        case "column" -> gridEntry.column = in.nextInt();
                        case "width" -> gridEntry.width = in.nextInt();
                        default -> in.skipValue();
                    }
                }
                in.endObject();
                return gridEntry;
            }
        }
    }
}
