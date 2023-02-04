/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.kvverti.colormatic.properties.adapter.ApplicableBlockStatesAdapter;
import io.github.kvverti.colormatic.properties.adapter.ChatFormatAdapter;
import io.github.kvverti.colormatic.properties.adapter.GridEntryAdapter;
import io.github.kvverti.colormatic.properties.adapter.HexColorAdapter;
import io.github.kvverti.colormatic.properties.adapter.IdentifierAdapter;
import io.github.kvverti.colormatic.properties.adapter.MaterialColorAdapter;
import io.github.kvverti.colormatic.properties.adapter.StringIdentifiableTypeAdapterFactory;

import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Utility class for dealing with properties files.
 */
public class PropertyUtil {

    public static final Gson PROPERTY_GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new StringIdentifiableTypeAdapterFactory())
        .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
        .registerTypeAdapter(ApplicableBlockStates.class, new ApplicableBlockStatesAdapter())
        .registerTypeAdapter(HexColor.class, new HexColorAdapter())
        .registerTypeAdapter(MapColor.class, new MaterialColorAdapter())
        .registerTypeAdapter(Formatting.class, new ChatFormatAdapter())
        .registerTypeAdapter(GridEntry.class, new GridEntryAdapter())
        .create();

    /**
     * Resolves a possibly relative identifier with the given identifier.
     */
    public static String resolve(String path, Identifier id) {
        if(path.startsWith("./")) {
            // relative path
            String thisPath = id.toString();
            path = thisPath.substring(0, thisPath.lastIndexOf('/')) + path.substring(1);
        } else if(path.startsWith("~/")) {
            // ~ is the optifine directory
            path = "optifine" + path.substring(1);
        } else if(!path.contains("/") && !path.contains(":")) {
            // relative path - not a file path or a namespaced path
            String thisPath = id.toString();
            path = thisPath.substring(0, thisPath.lastIndexOf('/') + 1) + path;
        }
        return path;
    }

    /**
     * Creates a reader suitable for deserializing json from either a json file
     * or a properties file. Since Optifine properties files may use old names
     * for property keys, the keyMapper parameter transforms these into sensible
     * names. As well, for properties that must be arrays in json, there is the
     * predicate arrayValue.
     */
    public static Reader getJsonReader(
        InputStream in,
        Identifier id,
        Function<String, String> keyMapper,
        Predicate<String> arrayValue) throws IOException {
        Reader jsonInput;
        if(id.getPath().endsWith(".properties")) {
            // properties file
            Properties data = new Properties();
            data.load(in);
            jsonInput = new StringReader(PropertyUtil.toJson(data, keyMapper, arrayValue));
        } else {
            // json file
            jsonInput = new InputStreamReader(in);
        }
        return jsonInput;
    }

    /**
     * Converts the argument to an equivalent JSON string. Property keys with
     * dot separators are rendered as nested objects in the JSON string.
     * Property keys are also mapped according to the keyMapper.
     */
    private static String toJson(Properties properties, Function<String, String> keyMapper, Predicate<String> arrayValue) {
        // split lists of data on whitespace
        Map<String, Object> props = new HashMap<>();
        for(String prop : properties.stringPropertyNames()) {
            String[] keys = prop.split("\\.");
            Map<String, Object> nest = props;
            int i;
            for(i = 0; i < keys.length - 1; i++) {
                String key = keyMapper.apply(keys[i]);
                Object tmp = nest.computeIfAbsent(key, k -> new HashMap<>());
                // similar to mergeCompound() below, but the existing key is
                // the non-map object rather than the map object.
                if(tmp instanceof Map<?, ?>) {
                    // noinspection unchecked
                    nest = (Map<String, Object>)tmp;
                } else {
                    Map<String, Object> newNest = new HashMap<>();
                    newNest.put("", tmp);
                    nest.put(key, newNest);
                    nest = newNest;
                }
            }
            String key = keyMapper.apply(keys[i]);
            String propVal = properties.getProperty(prop);
            Object val = arrayValue.test(key) ? propVal.split("\\s+") : propVal;
            nest.merge(key, val, PropertyUtil::mergeCompound);
        }
        return PROPERTY_GSON.toJson(props);
    }

    /**
     * Merge compound keys like so:
     * key = value
     * key.nest = value2
     * ---------------------
     * "key": {
     * "": value,
     * "nest": value2
     * }
     */
    private static Object mergeCompound(Object existingValue, Object newValue) {
        if(existingValue instanceof Map<?, ?>) {
            // existing value is a compound, so we add the new value to it
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)existingValue;
            map.put("", newValue);
            return existingValue;
        } else {
            return newValue;
        }
    }

    /**
     * Loads the given colormap properties and the image (if any) associated with
     * them.
     *
     * @throws InvalidColormapException if no colormap properties exist for the given id
     *                                  or if the colormap exists, but is malformed.
     */
    public static PropertyImage loadColormap(ResourceManager manager, Identifier id, boolean custom) {
        ColormapProperties props = ColormapProperties.load(manager, id, custom);
        if(props.getFormat() == ColormapProperties.Format.FIXED) {
            // fixed format does not have a corresponding image
            return new PropertyImage(props, null);
        }
        try(InputStream in = manager.getResourceOrThrow(props.getSource()).getInputStream()) {
            NativeImage image = NativeImage.read(in);
            if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                // swap the red and blue channels of every pixel, because the biome
                // colormap expects ARGB, but NativeImage is ABGR
                for(int x = 0; x < image.getWidth(); x++) {
                    for(int y = 0; y < image.getHeight(); y++) {
                        int pix = image.getColor(x, y);
                        int tmp = (pix & 0xff0000) >> 16;
                        tmp |= (pix & 0x0000ff) << 16;
                        pix &= ~(0xff0000 | 0x0000ff);
                        pix |= tmp;
                        image.setColor(x, y, pix);
                    }
                }
            }
            // cross-reference image dimensions with colormap format
            if(props.getFormat() == ColormapProperties.Format.VANILLA) {
                if(image.getWidth() != 256 || image.getHeight() != 256) {
                    throw new InvalidColormapException("Vanilla colormap dimensions must be 256x256");
                }
            }
            return new PropertyImage(props, image);
        } catch(IOException e) {
            throw new InvalidColormapException(e);
        }
    }
}
