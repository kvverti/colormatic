/*
 * Colormatic
 * Copyright (C) 2021  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.particle;

import io.github.kvverti.colormatic.particle.CustomColoredRedDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;

/**
 * Replace the redstone dust particle with one that takes its color
 * from the resource pack.
 */
@Mixin(DustParticleEffect.class)
public abstract class DustParticleEffectMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "NEW",
            target = "net/minecraft/particle/DustParticleEffect",
            ordinal = 0
        )
    )
    private static DustParticleEffect proxyRedDust(Vector3f color, float a) {
        return new CustomColoredRedDustParticle(color, a);
    }
}
