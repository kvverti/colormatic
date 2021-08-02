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
package io.github.kvverti.colormatic.mixin.text;

import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

@Mixin(Style.class)
public abstract class ChatFormatMixin {

    /**
     * Swap out the text color of any style whenever it is requested.
     */
    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void switchToCustomColor(CallbackInfoReturnable<TextColor> info) {
        if(info.getReturnValue() != null) {
            String name = info.getReturnValue().getName();
            if(name != null) {
                Formatting formatting = Formatting.byName(name);
                if(formatting != null) {
                    TextColor color = Colormatic.COLOR_PROPS.getProperties().getText(formatting);
                    if(color != null) {
                        info.setReturnValue(color);
                    }
                }
            }
        }
    }
}
