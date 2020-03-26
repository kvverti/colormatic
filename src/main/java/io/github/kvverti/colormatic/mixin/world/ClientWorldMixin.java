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
package io.github.kvverti.colormatic.mixin.world;

import net.minecraft.client.MinecraftClient;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.colormap.ColormaticBlockRenderView;
import io.github.kvverti.colormatic.properties.PseudoBlockStates;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Provides global sky color customization capability.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements ColormaticBlockRenderView {

    private ClientWorldMixin() {
        super(null, null, null, null, false);
    }

    @Redirect(
        method = "method_23777",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getSkyColor()I"
        )
    )
    private int proxySkyColor(Biome self, BlockPos pos, float partialTicks) {
        DimensionType type = this.dimension.getType();
        BlockState state = PseudoBlockStates.SKY.getDefaultState()
            .with(PseudoBlockStates.DIMENSION, Registry.DIMENSION.getId(type));
        if(BiomeColormaps.isCustomColored(state)) {
            return BiomeColormaps.getBiomeColor(state, this, pos);
        } else if(type == DimensionType.OVERWORLD && Colormatic.SKY_COLORS.hasCustomColormap()) {
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

    @Override
    public int colormatic_getColor(BlockPos pos, ColormaticResolver resolver) {
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
}
