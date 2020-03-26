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

import io.github.kvverti.colormatic.Colormatic;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.AbstractProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

/**
 * Biome colors that are not for blocks, such as sky and fog colors, are
 * represented by pseudo-block states. These are BlockState objects that are
 * not registered as blocks.
 */
public final class PseudoBlockStates {

    public static final Block SKY;
    public static final Block SKY_FOG;
    public static final Block FLUID_FOG;

    public static final IdentifierProperty<DimensionType> DIMENSION =
        IdentifierProperty.create("dimension", Registry.DIMENSION);
    public static final IdentifierProperty<Fluid> FLUID =
        IdentifierProperty.create("fluid", Registry.FLUID);

    private static final Map<Identifier, Block> pseudoBlocks = new HashMap<>();

    private PseudoBlockStates() {}

    /**
     * Returns the pseudo-block associated with the given ID. All pseudo-blocks
     * have IDs under the colormatic namespace.
     */
    public static Block getPseudoBlock(Identifier id) {
        return pseudoBlocks.get(id);
    }

    /**
     * A Property whose values are the elements of a game registry.
     */
    public static class IdentifierProperty<T> extends AbstractProperty<Identifier> {

        private final Registry<T> registry;

        private IdentifierProperty(String name, Registry<T> registry) {
            super(name, Identifier.class);
            this.registry = registry;
        }

        public static <T> IdentifierProperty<T> create(String name, Registry<T> registry) {
            return new IdentifierProperty<>(name, registry);
        }

        @Override
        public Collection<Identifier> getValues() {
            return registry.getIds();
        }

        /**
         * Unlike getName, this method expects the namespace separator to be
         * a slash.
         */
        @Override
        public Optional<Identifier> parse(String str) {
            return Optional.ofNullable(
                registry.get(new Identifier(str.replaceFirst("/", ":"))))
                .map(registry::getId);
        }

        /**
         * The namespace separator is not allowed in property value names,
         * so we replace it with an underscore, which is allowed. Since
         * pseudo-blocks are never deserialized, this poses no issue.
         */
        @Override
        public String name(Identifier id) {
            return id.toString().replace(":", "_");
        }
    }

    static {
        class DimensionPseudoBlock extends Block {

            DimensionPseudoBlock() {
                super(Block.Settings.of(Material.AIR));
            }

            @Override
            protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(DIMENSION);
            }
        }
        SKY = new DimensionPseudoBlock();
        SKY_FOG = new DimensionPseudoBlock();
        FLUID_FOG = new Block(Block.Settings.of(Material.AIR)) {
            @Override
            protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(FLUID);
            }
        };
        pseudoBlocks.put(new Identifier(Colormatic.MODID, "sky"), SKY);
        pseudoBlocks.put(new Identifier(Colormatic.MODID, "sky_fog"), SKY_FOG);
        pseudoBlocks.put(new Identifier(Colormatic.MODID, "fluid_fog"), FLUID_FOG);
    }
}
