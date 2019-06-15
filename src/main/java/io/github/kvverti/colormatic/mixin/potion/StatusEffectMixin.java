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
package io.github.kvverti.colormatic.mixin.potion;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.entity.effect.StatusEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void onColor(CallbackInfoReturnable<Integer> info) {
        StatusEffect self = (StatusEffect)(Object)this;
        int color = Colormatic.COLOR_PROPS.getProperties().getPotion(self);
        if(color != 0) {
            info.setReturnValue(color);
        }
    }
}
