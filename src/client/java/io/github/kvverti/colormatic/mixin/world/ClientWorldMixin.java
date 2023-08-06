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
package io.github.kvverti.colormatic.mixin.world;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.colormap.ExtendedColorResolver;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.ColorResolver;

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

    @Shadow
    public abstract DynamicRegistryManager getRegistryManager();

    private ClientWorldMixin() {
        super(null, null, null, null, false, false, 0L, 0);
    }

    @ModifyArg(
        method = "getSkyColor",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"
        ),
        index = 1
    )
    private CubicSampler.RgbFetcher proxySkyColor(CubicSampler.RgbFetcher fetcher) {
        var dimId = Colormatic.getDimId(this);
        var resolver = BiomeColormaps.getTotalSky(dimId);
        var biomeAccess = this.getBiomeAccess();
        var manager = this.getRegistryManager();
        return (x, y, z) -> {
            var biomeRegistry = manager.get(Registry.BIOME_KEY);
            var biome = Colormatic.getRegistryValue(biomeRegistry, biomeAccess.getBiomeForNoiseGen(x, y, z));
            return Vec3d.unpackRgb(resolver.getColor(manager, biome, BiomeCoords.toBlock(x), BiomeCoords.toBlock(y), BiomeCoords.toBlock(z)));
        };
    }

    /**
     * Vanilla doesn't check if the color cache exists before retrieving it. We fix this here.
     */
    @Inject(method = "getColor", at = @At("HEAD"))
    private void fixVanillaColorCache(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> info) {
        if(this.colorCache.get(resolver) == null) {
            this.colorCache.put(resolver, new BiomeColorCache(pos1 -> this.calculateColor(pos1, resolver)));
        }
    }

    /**
     * Reset custom colors for the entire world. We remove caches for Colormatic's custom resolvers, as their
     * identities are not consistent.
     */
    @Inject(method = "reloadColor", at = @At("RETURN"))
    private void reloadColormaticColor(CallbackInfo info) {
        this.colorCache.entrySet().removeIf(entry -> entry.getKey() instanceof ExtendedColorResolver);
    }
}
