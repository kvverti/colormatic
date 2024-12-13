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
package io.github.kvverti.colormatic.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    /**
     * When clear void is enabled, prevent the void from rendering.
     */
    @ModifyExpressionValue(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "CONSTANT", args = "doubleValue=0", ordinal = 0),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/world/ClientWorld$Properties;getSkyDarknessHeight(Lnet/minecraft/world/HeightLimitView;)D"
            )
        )
    )
    private double modifyVoidBackgroundCondition(double zero) {
        if(Colormatic.config().clearVoid) {
            zero = Double.NEGATIVE_INFINITY;
        }
        return zero;
    }
}
