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

import net.minecraft.world.biome.Biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Provides sky color and underwater color customization capability.
 */
@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onSkyColor(CallbackInfoReturnable<Integer> info) {
        if(Colormatic.SKY_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.SKY_COLORS.getColormap().getColor((Biome)(Object)this));
        }
    }

    @Inject(method = "getWaterFogColor", at = @At("HEAD"), cancellable = true)
    private void onUnderwaterColor(CallbackInfoReturnable<Integer> info) {
        if(Colormatic.UNDERWATER_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.UNDERWATER_COLORS.getColormap().getColor((Biome)(Object)this));
        }
    }
}
