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
package io.github.kvverti.colormatic.mixin;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Provides fog color customization capability.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Redirect(
        method = "updateColorNotInWater",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getFogColor(F)Lnet/minecraft/util/math/Vec3d;"
        )
    )
    private Vec3d proxyFogColor(World self, float partialTicks, Camera camera, World self2, float partialTicks2) {
        if(Colormatic.FOG_COLORS.hasCustomColormap() && self.getDimension().getType() == DimensionType.OVERWORLD) {
            Biome biome = self.getBiome(camera.getBlockPos());
            double double_1 = (double)MathHelper.clamp(biome.getTemperature(), 0.0F, 1.0F);
            double double_2 = (double)MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
            int color = Colormatic.FOG_COLORS.getColor(double_1, double_2);
            double r = ((color >> 16) & 0xff) / 255.0;
            double g = ((color >>  8) & 0xff) / 255.0;
            double b = ((color >>  0) & 0xff) / 255.0;
            // time-of-day calculations
            float daytimeAngle = self.getSkyAngle(partialTicks);
            float float_3 = MathHelper.cos(daytimeAngle * 2.0f * (float)Math.PI) * 2.0F + 0.5F;
            float_3 = MathHelper.clamp(float_3, 0.0F, 1.0F);
            r *= float_3 * 0.94F + 0.06F;
            g *= float_3 * 0.94F + 0.06F;
            b *= float_3 * 0.91F + 0.09F;
            return new Vec3d(r, g, b);
        } else {
            return self.getFogColor(partialTicks);
        }
    }
}
