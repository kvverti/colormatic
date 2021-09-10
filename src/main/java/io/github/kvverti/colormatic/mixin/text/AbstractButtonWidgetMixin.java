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

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ClickableWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClickableWidget.class)
public abstract class AbstractButtonWidgetMixin extends DrawableHelper {

    @Shadow public abstract boolean isHovered();

    @ModifyConstant(method = "renderButton", constant = @Constant(intValue = 16777215))
    private int proxyButtonHoverColor(int original) {
        int col = Colormatic.COLOR_PROPS.getProperties().getButtonTextHovered();
        return col != 0 && this.isHovered() ? col : original;
    }

    @ModifyConstant(method = "renderButton", constant = @Constant(intValue = 10526880))
    private int proxyButtonDisabledColor(int original) {
        int col = Colormatic.COLOR_PROPS.getProperties().getButtonTextDisabled();
        return col != 0 ? col : original;
    }
}
