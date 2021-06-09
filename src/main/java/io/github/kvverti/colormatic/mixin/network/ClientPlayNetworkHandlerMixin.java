/*
 * Colormatic
 * Copyright (C) 2020  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private DynamicRegistryManager registryManager;

    /**
     * We loop through and make the instances identical since we want to be able to get the ID for a given dimension
     * type.
     */
    @ModifyVariable(
        method = "onPlayerRespawn",
        ordinal = 0,
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    private DimensionType fixDimensionTypeOnPlayerRespawn(DimensionType target) {
        Registry<DimensionType> registry = this.registryManager.get(Registry.DIMENSION_TYPE_KEY);
        for(DimensionType dimType : registry) {
            if(dimType.equals(target)) {
                return dimType;
            }
        }
        return target;
    }
}
