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
package io.github.kvverti.colormatic.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.kvverti.colormatic.colormap.ExtendedColorResolver;
import io.github.kvverti.colormatic.iface.StaticRenderContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeCoords;

/**
 * Provides global sky color customization capability.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    @Shadow
    @Final
    private Object2ObjectArrayMap<ColorResolver, BiomeColorCache> colorCache;

    @Shadow
    public abstract int calculateColor(BlockPos pos, ColorResolver colorResolver);

    private ClientWorldMixin() {
        super(null, null, null, null, null, false, false, 0L, 0);
    }

    /**
     * Store this object for necessary context when computing the biome sky color.
     * Why are we doing this? Because Sodium redirects exactly the place we would have redirected,
     * and also ignores the vanilla RgbFetcher, we do this roundabout change instead. The world
     * and all positions are stored in static state so that the logic for calculating the sky color
     * can reside in {@link Biome#getSkyColor()}.
     */
    @WrapMethod(method = "getSkyColor")
    private Vec3d setWorldForSkyColor(Vec3d cameraPos, float tickDelta, Operation<Vec3d> original) {
        var ctx = StaticRenderContext.SKY_CONTEXT.get();
        ctx.world = (ClientWorld)(Object)this;
        var result = original.call(cameraPos, tickDelta);
        ctx.world = null;
        return result;
    }

    /**
     * Store the biome coordinates for use in getting the biome sky color.
     * ClientWorld normally inherits the implementation from {@link WorldView}.
     * Sodium calls this method directly instead of using the BiomeAccess for sky color.
     * todo: this is called concurrently so we need to make sure to only do stuff when we're calling fog/sky :))))))))
     */
    @Override
    public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        var skyCtx = StaticRenderContext.SKY_CONTEXT.get();
        if(skyCtx.world != null) {
            skyCtx.posX = BiomeCoords.toBlock(biomeX);
            skyCtx.posY = BiomeCoords.toBlock(biomeY);
            skyCtx.posZ = BiomeCoords.toBlock(biomeZ);
        }
        var fogCtx = StaticRenderContext.FOG_CONTEXT.get();
        if(fogCtx.world != null) {
            fogCtx.posX = BiomeCoords.toBlock(biomeX);
            fogCtx.posY = BiomeCoords.toBlock(biomeY);
            fogCtx.posZ = BiomeCoords.toBlock(biomeZ);
        }
        return super.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }

    /**
     * Vanilla doesn't check if the color cache exists before retrieving it. We fix this here.
     * This method can race with {@link #reloadColormaticColor(CallbackInfo)} so it modifies the receiver
     * directly before the method invocation.
     */
    @ModifyReceiver(
        method = "getColor",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/world/BiomeColorCache;getBiomeColor(Lnet/minecraft/util/math/BlockPos;)I"
        )
    )
    private BiomeColorCache fixVanillaColorCache(BiomeColorCache cache, BlockPos pos, BlockPos samePos, ColorResolver resolver) {
        if(cache == null) {
            cache = new BiomeColorCache(pos1 -> this.calculateColor(pos1, resolver));
            // prevent races with removing in reloadColormaticColor
            synchronized(this) {
                this.colorCache.put(resolver, cache);
            }
        }
        return cache;
    }

    /**
     * Reset custom colors for the entire world. We remove caches for Colormatic's custom resolvers, as their
     * identities are not consistent.
     */
    @Inject(method = "reloadColor", at = @At("RETURN"))
    private void reloadColormaticColor(CallbackInfo info) {
        // prevent races with adding in fixVanillaColorCache
        synchronized(this) {
            this.colorCache.entrySet().removeIf(entry -> entry.getKey() instanceof ExtendedColorResolver);
        }
    }
}
