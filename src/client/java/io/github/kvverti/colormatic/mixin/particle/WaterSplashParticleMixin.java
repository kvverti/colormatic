/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
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
import io.github.kvverti.colormatic.colormap.BiomeColormap;

import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.WaterSplashParticle;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Provides custom portal particle color ability
 */
@Mixin(WaterSplashParticle.class)
public abstract class WaterSplashParticleMixin extends SpriteBillboardParticle {

    @Unique
    private static final BlockPos.Mutable pos = new BlockPos.Mutable();

    private WaterSplashParticleMixin() {
        super(null, 0.0, 0.0, 0.0);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo info) {
        if(Colormatic.WATER_COLORS.hasCustomColormap()) {
            BiomeColormap colormap = Colormatic.WATER_COLORS.getColormap();
            pos.set(this.x, this.y, this.z);
            int color = BiomeColormap.getBiomeColor(this.world, pos, colormap);
            this.red = ((color >> 16) & 0xff) / 255.0f;
            this.green = ((color >> 8) & 0xff) / 255.0f;
            this.blue = ((color >> 0) & 0xff) / 255.0f;
        }
    }
}
