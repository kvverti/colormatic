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
package io.github.kvverti.colormatic.mixin.particle;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.client.particle.SpriteBillboardParticle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Provides (dripping) lava drop particle color customization capability.
 */
@Mixin(targets = "net.minecraft.client.particle.BlockLeakParticle$DrippingLavaParticle")
public abstract class DrippingLavaParticleMixin extends SpriteBillboardParticle {

    private DrippingLavaParticleMixin() {
        super(null, 0.0, 0.0, 0.0);
    }

    // field injections
    private int colormatic_age;

    @Inject(
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V",
        at = @At("RETURN")
    )
    private void onConstruct(CallbackInfo info) {
        colormatic_age = 0;
        if(Colormatic.LAVA_DROP_COLORS.hasCustomColormap()) {
            int color = Colormatic.LAVA_DROP_COLORS.getColorBounded(0);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >>  8) & 0xff) / 255.0f;
            float b = ((color >>  0) & 0xff) / 255.0f;
            this.setColor(r, g, b);
        }
    }

    /*
     * Relevant bytecode:
     *  35: iadd
     *  36: i2f
     *  37: fdiv
     *  38: putfield      #50                 // Field colorBlue:F
     *  <injection point>
     *  41: aload_0
     *  42: invokespecial #52                 // Method net/minecraft/client/particle/BlockLeakParticle$DrippingParticle.updateAge:()V
     *  45: return
     */
    @Inject(
        method = "updateAge()V",
        at = @At(
            value = "RETURN",
            shift = At.Shift.BY,
            by = -2
        )
    )
    private void onUpdateAge(CallbackInfo info) {
        if(Colormatic.LAVA_DROP_COLORS.hasCustomColormap()) {
            int color = Colormatic.LAVA_DROP_COLORS.getColorBounded(++colormatic_age);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >>  8) & 0xff) / 255.0f;
            float b = ((color >>  0) & 0xff) / 255.0f;
            this.setColor(r, g, b);
        }
    }
}
