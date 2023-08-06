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

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SuspendParticle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Provides mycelium particle color customization capability.
 */
@Mixin(SuspendParticle.MyceliumFactory.class)
public abstract class MyceliumParticleFactoryMixin {

    @Inject(method = "createParticle", at = @At("RETURN"))
    private void onCreateParticle(CallbackInfoReturnable<Particle> info) {
        if(Colormatic.MYCELIUM_PARTICLE_COLORS.hasCustomColormap()) {
            Particle particle = info.getReturnValue();
            int color = Colormatic.MYCELIUM_PARTICLE_COLORS.getRandomColor();
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >>  8) & 0xff) / 255.0f;
            float b = ((color >>  0) & 0xff) / 255.0f;
            particle.setColor(r, g, b);
        }
    }
}
