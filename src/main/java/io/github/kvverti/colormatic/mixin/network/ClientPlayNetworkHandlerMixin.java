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
package io.github.kvverti.colormatic.mixin.network;

import io.github.kvverti.colormatic.Colormatic;
import io.github.kvverti.colormatic.iface.DimensionTypeEquals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private DynamicRegistryManager.Immutable registryManager;

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
    private RegistryEntry<DimensionType> fixDimensionTypeOnPlayerRespawn(RegistryEntry<DimensionType> targetEntry) {
        var registry = this.registryManager.get(Registry.DIMENSION_TYPE_KEY);
        var target = (DimensionTypeEquals)Colormatic.getRegistryValue(registry, targetEntry);
        for(DimensionType dimType : registry) {
            if(target.colormatic_equals(dimType)) {
                // packet deserializer expects a direct entry
                return RegistryEntry.of(dimType);
            }
        }
        return targetEntry;
    }
}
