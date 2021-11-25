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
package io.github.kvverti.colormatic.mixin.model;

import io.github.kvverti.colormatic.iface.ModelIdContext;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
abstract class ModelLoaderMixin {

    /**
     * Sets model ID context for the baked quad factory. Must be set before UnbakedModel#back is called.
     */
    @Dynamic("Model baking lambda in upload()")
    @Inject(
        method = "method_4733",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/model/ModelLoader;bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;"
        )
    )
    private void setModelIdContext(Identifier id, CallbackInfo info) {
        if(id instanceof ModelIdentifier modelId) {
            ModelIdContext.currentModelId = modelId;
        }
    }
}
