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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.potion.PotionUtil;

/**
 * Provides custom water bottle coloring.
 */
@Mixin(PotionUtil.class)
public abstract class PotionUtilMixin {

    @ModifyExpressionValue(
        method = "getColor(Ljava/util/Collection;)I",
        at = @At(value = "CONSTANT", args = "intValue=3694022")
    )
    private static int modifyWaterColor(int waterColor) {
        int color = Colormatic.COLOR_PROPS.getProperties().getPotion(null);
        return color != 0 ? color : waterColor;
    }
}
