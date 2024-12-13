/*
 * Colormatic
 * Copyright (C) 2021-2024  Thalia Nero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As an additional permission, when conveying the Corresponding Source of an
 * object code form of this work, you may exclude the Corresponding Source for
 * "Minecraft" by Mojang Studios, AB.
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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.effect.StatusEffect;

/**
 * Provides custom potion and effect coloring.
 */
@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {

    @ModifyReturnValue(method = "getColor", at = @At("RETURN"))
    private int modifyColor(int originalColor) {
        StatusEffect self = (StatusEffect)(Object)this;
        int color = Colormatic.COLOR_PROPS.getProperties().getPotion(self);
        if(color != 0) {
            return color;
        }
        return originalColor;
    }
}
