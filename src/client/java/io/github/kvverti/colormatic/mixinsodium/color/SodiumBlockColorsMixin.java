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
package io.github.kvverti.colormatic.mixinsodium.color;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.Block;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IdList;

@Mixin(BlockColors.class)
public abstract class SodiumBlockColorsMixin {

    @Shadow @Final private IdList<BlockColorProvider> providers;

    @Unique
    private static final BlockColorProvider COLORMATIC_PROVIDER =
        (state, world, pos, tintIndex) -> BiomeColormaps.getBiomeColor(state, world, pos);

    /**
     * Displace Sodium's implementation to first check Colormatic's custom block colors.
     */
    @ModifyReturnValue(method = "sodium$getProviders", at = @At("RETURN"))
    private Reference2ReferenceMap<Block, BlockColorProvider> addColormaticProviders(Reference2ReferenceMap<Block, BlockColorProvider> original) {
        var map = new Reference2ReferenceOpenHashMap<>(original);
        for(var block : Registries.BLOCK) {
            if(BiomeColormaps.isBlockCustomColored(block)) {
                map.put(block, COLORMATIC_PROVIDER);
            }
        }
        return map;
    }

    @ModifyReturnValue(method = "sodium$getOverridenVanillaBlocks", at = @At("RETURN"))
    private ReferenceSet<Block> addColormaticOverriddenBlocks(ReferenceSet<Block> original) {
        var set = new ReferenceOpenHashSet<>(original);
        for(var block : Registries.BLOCK) {
            if(this.providers.containsKey(Registries.BLOCK.getRawId(block)) && BiomeColormaps.isBlockCustomColored(block)) {
                set.add(block);
            }
        }
        return set;
    }
}
