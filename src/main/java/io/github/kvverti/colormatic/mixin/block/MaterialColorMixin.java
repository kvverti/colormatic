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
package io.github.kvverti.colormatic.mixin.block;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.block.MapColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Provides redstone wire color customization capability.
 */
@Mixin(MapColor.class)
public abstract class MaterialColorMixin {

    @Inject(
        method = "getRenderColor",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/block/MapColor;color:I",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        cancellable = true
    )
    private void onRenderColor(int dontCare, CallbackInfoReturnable<Integer> info, int scalar) {
        int color = Colormatic.COLOR_PROPS.getProperties().getMap((MapColor)(Object)this);
        if(color != 0) {
            int r = ((color >> 16) & 0xff) * scalar / 255;
            int g = ((color >>  8) & 0xff) * scalar / 255;
            int b = ((color >>  0) & 0xff) * scalar / 255;
            info.setReturnValue(0xff000000 | (b << 16) | (g << 8) | r);
        }
    }
}
