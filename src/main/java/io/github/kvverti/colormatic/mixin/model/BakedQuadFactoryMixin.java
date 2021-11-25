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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.render.model.BakedQuadFactory;

/**
 * Wrap block state models that are Colormatic custom colored in a baked model that enables the tint index
 * if the block does not already have a color provider.
 */
@Mixin(BakedQuadFactory.class)
public abstract class BakedQuadFactoryMixin {

    /**
     * Replaces non-tinted quads with tinted quads when baked if the block has no color provider.
     */
    @ModifyArg(
        method = "bake",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/model/BakedQuad;<init>([IILnet/minecraft/util/math/Direction;Lnet/minecraft/client/texture/Sprite;Z)V"
        ),
        index = 1
    )
    private int addTintToCustomColoredModel(int tintIndex) {
        // customTintCurrentModel implies that all quads are untinted
        return ModelIdContext.customTintCurrentModel ? 0 : tintIndex;
    }
}
