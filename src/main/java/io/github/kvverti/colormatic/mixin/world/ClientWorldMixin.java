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

import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.properties.PseudoBlockStates;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormap;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import net.minecraft.class_4700;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ColorResolver;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Provides global sky color customization capability.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

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
            return BiomeColormaps.getBiomeColor(state, (ClientWorld)(Object)this, pos);
        } else if(type == DimensionType.OVERWORLD && Colormatic.SKY_COLORS.hasCustomColormap()) {
            BiomeColormap colormap = Colormatic.SKY_COLORS.getColormap();
            return BiomeColormap.getBiomeColor((ClientWorld)(Object)this, pos, colormap);
        } else {
            int color = Colormatic.COLOR_PROPS.getProperties().getDimensionSky(type);
            if(color != 0) {
                return color;
            }
        }
        return self.getSkyColor();
    }

    /**
     * Add Colormatic's ColorResolvers to ClientWorld's map
     */
    @Dynamic("ColorResolver addition in ClientWorld constructor")
    @Inject(method = "method_23778", at = @At("RETURN"))
    private static void onColorResolverRegistration(Object2ObjectArrayMap<ColorResolver, class_4700> map, CallbackInfo info) {
        map.put(BiomeColormap.colormaticResolver, new class_4700());
        map.put(BiomeColormaps.colormaticResolver, new class_4700());
    }
}
