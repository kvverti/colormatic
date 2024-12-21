/*
 * Colormatic
 * Copyright (C) 2024  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.SculkSensorBlock;
import net.minecraft.particle.DustColorTransitionParticleEffect;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    @ModifyExpressionValue(
        method = "randomDisplayTick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/particle/DustColorTransitionParticleEffect;DEFAULT:Lnet/minecraft/particle/DustColorTransitionParticleEffect;"
        )
    )
    private DustColorTransitionParticleEffect proxyColormaticSculkGradient(DustColorTransitionParticleEffect original) {
        var props = Colormatic.COLOR_PROPS.getProperties();
        var startColor = props.getSculkStart();
        var endColor = props.getSculkEnd();
        if(startColor == null && endColor == null) {
            return original;
        }

        if(startColor == null) {
            startColor = original.getFromColor();
        }
        if(endColor == null) {
            endColor = original.getToColor();
        }
        return new DustColorTransitionParticleEffect(startColor, endColor, original.getScale());
    }
}
