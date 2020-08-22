/*
 * Colormatic
 * Copyright (C) 2019-2020  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.world;

import java.util.Map;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.colormap.ColormaticBlockRenderView;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

/**
 * Provides global sky color customization capability.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements ColormaticBlockRenderView {

    private ClientWorldMixin() {
        super(null, null, null, null, false, false, 0L);
    }

    @Redirect(
        method = "method_23777",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getSkyColor()I"
        )
    )
    private int proxySkyColor(Biome self, BlockPos pos, float partialTicks) {
        DimensionType type = this.getDimension();
        if(BiomeColormaps.isSkyCustomColored(type)) {
            return BiomeColormaps.getSkyColor(type, this, pos);
        } else if(Colormatic.SKY_COLORS.hasCustomColormap() && Colormatic.getDimId(type) == DimensionType.OVERWORLD_ID) {
            BiomeColormap colormap = Colormatic.SKY_COLORS.getColormap();
            return BiomeColormap.getBiomeColor(this, pos, colormap);
        } else {
            int color = Colormatic.COLOR_PROPS.getProperties().getDimensionSky(type);
            if(color != 0) {
                return color;
            }
        }
        return self.getSkyColor();
    }

    /**
     * Color cache map for custom biome colormaps.
     */
    @Unique
    private final Map<ColormaticResolver, BiomeColorCache> customColorCache = new Reference2ObjectOpenHashMap<>();

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
            Biome biome = this.getBiomeAccess().getBiome(curpos);
            int color = resolver.getColor(biome, curpos);
            r += (color & 0xff0000) >> 16;
            g += (color & 0x00ff00) >> 8;
            b += (color & 0x0000ff);
        }
        int posCount = (radius * 2 + 1) * (radius * 2 + 1);
        return ((r / posCount & 255) << 16) | ((g / posCount & 255) << 8) | (b / posCount & 255);
    }

    /**
     * Reset custom colors for a chunk.
     */
    @Inject(method = "resetChunkColor", at = @At("RETURN"))
    private void resetColormaticChunkColor(int x, int z, CallbackInfo info) {
        customColorCache.forEach((resolver, cache) -> cache.reset(x, z));
    }

    /**
     * Reset custom colors for the entire world.
     */
    @Inject(method = "reloadColor", at = @At("RETURN"))
    private void reloadColormaticColor(CallbackInfo info) {
        customColorCache.clear();
    }
}
