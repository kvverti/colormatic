/*
 * Colormatic
 * Copyright (C) 2020  Thalia Nero
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
package io.github.kvverti.colormatic.mixinsodium.world;

import java.util.Map;

import io.github.kvverti.colormatic.colormap.ColormaticBlockRenderView;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.biome.BiomeCache;
import me.jellysquid.mods.sodium.common.util.pool.ReusableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

@Mixin(WorldSlice.class)
public abstract class WorldSliceMixin extends ReusableObject implements ColormaticBlockRenderView {

    @Shadow(remap = false)
    public native Biome getCachedBiome(int x, int z);

    /**
     * Color cache map for custom biome colormaps.
     */
    @Unique
    private final Map<ColormaticResolver, BiomeColorCache> customColorCache = new Reference2ObjectOpenHashMap<>();

    /**
     * Copied from {@link io.github.kvverti.colormatic.mixin.world.ClientWorldMixin} for efficiency. The
     * implementations must be kept in sync.
     */
    @Override
    public int colormatic_getColor(BlockPos pos, ColormaticResolver resolver) {
        BiomeColorCache cache = customColorCache.computeIfAbsent(resolver, k -> new BiomeColorCache());
        return cache.getBiomeColor(pos, () -> computeBiomeColor(pos, resolver));
    }

    @Unique
    private int computeBiomeColor(BlockPos pos, ColormaticResolver resolver) {
        int r = 0;
        int g = 0;
        int b = 0;
        int radius = MinecraftClient.getInstance().options.biomeBlendRadius;
        Iterable<BlockPos> coll = BlockPos.iterate(
            pos.getX() - radius, pos.getY(), pos.getZ() - radius,
            pos.getX() + radius, pos.getY(), pos.getZ() + radius);
        for(BlockPos curpos : coll) {
            Biome biome = this.getCachedBiome(curpos.getX(), curpos.getZ());
            int color = resolver.getColor(biome, curpos);
            r += (color & 0xff0000) >> 16;
            g += (color & 0x00ff00) >> 8;
            b += (color & 0x0000ff);
        }
        int posCount = (radius * 2 + 1) * (radius * 2 + 1);
        return ((r / posCount & 255) << 16) | ((g / posCount & 255) << 8) | (b / posCount & 255);
    }

    // so we don't create an object
    @ModifyVariable(method = "reset", ordinal = 0, at = @At(value = "LOAD", ordinal = 0), remap = false)
    private void resetColormaticColor(BiomeCache[] unused) {
        customColorCache.clear();
    }
}
