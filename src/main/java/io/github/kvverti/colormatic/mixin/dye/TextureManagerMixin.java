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
package io.github.kvverti.colormatic.mixin.dye;

import io.github.kvverti.colormatic.Colormatic;

import java.util.concurrent.Executor;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that color.json is loaded before textures are created.
 * Fixes banner textures being loaded before colors are.
 */
@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    @Dynamic("Post reload lambda method")
    @Inject(method = "method_18167", at = @At("HEAD"))
    private void onReload(ResourceManager manager, Executor exec, Void v, CallbackInfo info) {
        Colormatic.COLOR_PROPS.reload(manager);
    }
}
