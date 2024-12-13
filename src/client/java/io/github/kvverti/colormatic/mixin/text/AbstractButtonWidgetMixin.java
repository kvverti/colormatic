/*
 * Colormatic
 * Copyright (C) 2021-2024  Thalia Nero
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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

@Mixin(PressableWidget.class)
public abstract class AbstractButtonWidgetMixin extends ClickableWidget {

    public AbstractButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @ModifyExpressionValue(method = "renderButton", at = @At(value = "CONSTANT", args = "intValue=16777215"))
    private int proxyButtonHoverColor(int original) {
        int col = Colormatic.COLOR_PROPS.getProperties().getButtonTextHovered();
        return col != 0 && this.isHovered() ? col : original;
    }

    @ModifyExpressionValue(method = "renderButton", at = @At(value = "CONSTANT", args = "intValue=10526880"))
    private int proxyButtonDisabledColor(int original) {
        int col = Colormatic.COLOR_PROPS.getProperties().getButtonTextDisabled();
        return col != 0 ? col : original;
    }
}
