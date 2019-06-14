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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.kvverti.colormatic.properties.adapter.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

/**
 * Utility class for dealing with properties files.
 */
public class PropertyUtil {

    public static final Gson PROPERTY_GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new StringIdentifiableTypeAdapterFactory())
        .registerTypeAdapter(Identifier.class, new IdentifierAdapter())
        .registerTypeAdapter(BlockStatePredicate.class, new BlockStatePredicateAdapter())
        .registerTypeAdapter(DimensionType.class, new DimensionTypeAdapter())
        .registerTypeAdapter(StatusEffect.class, new StatusEffectAdapter())
        .registerTypeAdapter(HexColor.class, new HexColorAdapter())
        .create();

    /**
     * Tests a single element of a block state predicate list. For example,
     * these elements may be `stone`, `minecraft:grass_block`, `lever:attach=wall:facing=east,west`
     *
     * @throws InvalidPredicateException if the input is malformed
     */
    public static BlockStatePredicate createBlockPredicate(String blockDesc) {
        Block b;
        String[] parts = blockDesc.split(":");
        int bgnIdx;
        try {
            if(parts.length > 1 && parts[1].indexOf('=') < 0) {
                // a qualified name like `minecraft:grass_block:snowy=false`
                b = Registry.BLOCK.get(new Identifier(parts[0], parts[1]));
                bgnIdx = 2;
            } else {
                // an unqualified name like `grass_block:snowy=false`
                b = Registry.BLOCK.get(new Identifier(parts[0]));
                bgnIdx = 1;
            }
        } catch(InvalidIdentifierException e) {
            throw new InvalidPredicateException("Invalid block identifier: " + blockDesc, e);
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
        }
        return path;
    }

    /**
     * Creates a reader suitable for deserializing json from either a json file
     * or a properties file.
     */
    public static Reader getJsonReader(InputStream in, Identifier id) throws IOException {
        Reader jsonInput;
        if(id.getPath().endsWith(".properties")) {
            // properties file
            Properties data = new Properties();
            jsonInput = new StringReader(PropertyUtil.toJson(data));
        } else {
            // json file
            jsonInput = new InputStreamReader(in);
        }
        return jsonInput;
    }

    /**
     * Converts the argument to an equivalent JSON string.
     */
    public static String toJson(Properties properties) {
        // split lists of data on whitespace
        Map<String, Object> props = new HashMap<>();
        for(String prop : properties.stringPropertyNames()) {
            String[] vals = properties.getProperty(prop).split("\\s+");
            if(vals.length == 1) {
                props.put(prop, vals[0]);
            } else {
                props.put(prop, vals);
            }
        }
        return PROPERTY_GSON.toJson(props);
    }

    /**
     * Loads the given colormap properties and the image (if any) associated with
     * them.
     *
     * @throws InvalidColormapException if no colormap properties exist for the given id
     *     or if the colormap exists, but is malformed.
     */
    public static PropertyImage loadColormap(ResourceManager manager, Identifier id) {
        ColormapProperties props = ColormapProperties.load(manager, id);
        if(props.getFormat() == ColormapProperties.Format.FIXED) {
            // fixed format does not have a corresponding image
            return new PropertyImage(props, null);
        }
        try(Resource rsc = manager.getResource(props.getSource()); InputStream in = rsc.getInputStream()) {
            NativeImage image = NativeImage.fromInputStream(in);
            // swap the red and blue channels of every pixel, because the biome
            // colormap expects ARGB, but NativeImage is ABGR
            for(int x = 0; x < image.getWidth(); x++) {
                for(int y = 0; y < image.getHeight(); y++) {
                    int pix = image.getPixelRGBA(x, y);
                    int tmp = (pix & 0xff0000) >> 16;
                    tmp |= (pix & 0x0000ff) << 16;
                    pix &= ~(0xff0000 | 0x0000ff);
                    pix |= tmp;
                    image.setPixelRGBA(x, y, pix);
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
