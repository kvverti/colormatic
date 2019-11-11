package io.github.kvverti.colormatic.colormap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

@FunctionalInterface
public interface ColormaticResolver {

    int getColor(Biome biome, BlockPos pos);
}
