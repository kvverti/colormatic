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
package io.github.kvverti.colormatic.mixin.particle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;

/**
 * Provides (falling) lava drop particle color customization capability.
 */
@Mixin(BlockLeakParticle.class)
public abstract class FallingLavaParticleFactoryMixin {

    @ModifyReturnValue(method = "createFallingLava", at = @At("RETURN"))
    private static SpriteBillboardParticle onCreateParticle(SpriteBillboardParticle original) {
        if(Colormatic.LAVA_DROP_COLORS.hasCustomColormap()) {
            int color = Colormatic.LAVA_DROP_COLORS.getColorBounded(Integer.MAX_VALUE);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >>  8) & 0xff) / 255.0f;
            float b = ((color >>  0) & 0xff) / 255.0f;
            original.setColor(r, g, b);
        }
        return original;
    }
}
