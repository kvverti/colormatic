/*
 * Colormatic
 * Copyright (C) 2022
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
package io.github.kvverti.colormatic.mixinsodium.world;

import me.jellysquid.mods.sodium.client.world.BiomeSeedProvider;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

/**
 * Sodium expects {@link BiomeSeedProvider} to be implemented on the target class, but the mixin that
 * adds it is disabled by Colormatic.
 */
@Mixin(value = ClientWorld.class)
public abstract class SodiumClientWorldMixin extends World implements BiomeSeedProvider {

    private SodiumClientWorldMixin() {
        super(null, null, null, null, null, false, false, 0, 0);
    }

    @Override
    public long sodium$getBiomeSeed() {
        return ((SodiumBiomeAccessAccessor)this.getBiomeAccess()).getSeed();
    }
}
