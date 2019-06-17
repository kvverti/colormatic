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

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Provides global sky color customization capability.
 */
@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow @Final public Dimension dimension;

    @Redirect(
        method = "getSkyColor",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/Biome;getSkyColor(F)I"
        )
    )
    private int proxySkyColor(Biome self, float temp, BlockPos pos, float partialTicks) {
        DimensionType type = this.dimension.getType();
        if(type == DimensionType.OVERWORLD && Colormatic.SKY_COLORS.hasCustomColormap()) {
            BiomeColormap colormap = Colormatic.SKY_COLORS.getColormap();
            return BiomeColormap.getBiomeColor((World)(Object)this, pos, colormap);
        } else {
            int color = Colormatic.COLOR_PROPS.getProperties().getDimensionSky(type);
            if(color != 0) {
                return color;
            }
        }
        return self.getSkyColor(temp);
    }
}
