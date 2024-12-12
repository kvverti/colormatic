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
package io.github.kvverti.colormatic.mixin.block;

import io.github.kvverti.colormatic.particle.CustomColoredRedDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.block.Block;
import net.minecraft.block.LeverBlock;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;

/**
 * For some reason, levers don't use the default dust particle, so we have to
 * redirect the constructor call here as well.
 */
@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin extends Block {

    private LeverBlockMixin() {
        super(null);
    }

    @ModifyArg(
        method = "spawnParticles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldAccess;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
        )
    )
    private static ParticleEffect proxyRedDust(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        if(parameters instanceof AbstractDustParticleEffect dustParticleEffect) {
            return new CustomColoredRedDustParticle(dustParticleEffect.getColor(), dustParticleEffect.getScale());
        }
        return parameters;
    }
}
