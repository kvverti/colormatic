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
package io.github.kvverti.colormatic.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.iface.DefaultSkyColorAccess;
import io.github.kvverti.colormatic.iface.StaticRenderContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;

@Mixin(Biome.class)
public class BiomeMixin implements DefaultSkyColorAccess {

    @Shadow @Final private BiomeEffects effects;

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private int proxySkyColor(int original) {
        var ctx = StaticRenderContext.SKY_CONTEXT.get();
        if(ctx.world != null) {
            var world = ctx.world;
            var dimId = Colormatic.getDimId(world);
            var resolver = BiomeColormaps.getTotalSky(dimId);
            var manager = world.getRegistryManager();
            return resolver.getColor(manager, (Biome)(Object)this, ctx.posX, ctx.posY, ctx.posZ);
        }
        return original;
    }

    @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
    private int proxyFogColor(int original) {
        var ctx = StaticRenderContext.FOG_CONTEXT.get();
        if(ctx.world != null) {
            var world = ctx.world;
            var fogInvisible = Colormatic.config().clearSky && world.getDimension().hasSkyLight();
            if(fogInvisible) {
                return original;
            }
            var dimId = Colormatic.getDimId(world);
            var resolver = BiomeColormaps.getTotalSkyFog(dimId);
            return resolver.getColor(world.getRegistryManager(), (Biome)(Object)this, ctx.posX, ctx.posY, ctx.posZ);
        }
        return original;
    }

    @Override
    public int colormatic$getDefaultSkyColor() {
        return this.effects.getSkyColor();
    }

    @Override
    public int colormatic$getDefaultFogColor() {
        return this.effects.getFogColor();
    }
}
