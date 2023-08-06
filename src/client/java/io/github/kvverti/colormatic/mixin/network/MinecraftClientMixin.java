/*
 * Colormatic
 * Copyright (C) 2021  Thalia Nero
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

import io.github.kvverti.colormatic.colormap.ExtendedColorResolver;
import io.github.kvverti.colormatic.properties.DefaultColumns;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    /**
     * Reset the cached dynamic registry manager when the world is replaced; as this is when the DRM is regenerated.
     */
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void propagateDynamicRegistry(@Nullable ClientWorld world, CallbackInfo info) {
        var manager = world == null ? null : world.getRegistryManager();
        ExtendedColorResolver.setRegistryManager(manager);
        DefaultColumns.reloadDefaultColumnBounds(manager);
    }
}
