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
package io.github.kvverti.colormatic.mixin.dye;

import io.github.kvverti.colormatic.Colormatic;

import java.util.Map;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin extends AnimalEntity {

    private SheepEntityMixin() {
        super(null, null);
    }

    // pending a bug in Mixin affecting cancellable injections with array returning targets
    // @Inject(method = "getRgbColor", at = @At("HEAD"), cancellable = true)
    // private static void onRgbColor(DyeColor dye, CallbackInfoReturnable<float[]> info) {
    //     float[] rgb = Colormatic.COLOR_PROPS.getProperties().getWoolRgb(dye);
    //     if(rgb != null) {
    //         info.setReturnValue(rgb);
    //     }
    // }

    // redirect the Map#get call in getRgbColor until the injection bug in
    // Mixin is fixed
    @Redirect(
        method = "getRgbColor",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;",
            remap = false
        )
    )
    private static Object proxyRgbGet(Map<DyeColor, float[]> self, Object key) {
        float[] rgb = Colormatic.COLOR_PROPS.getProperties().getWoolRgb((DyeColor)key);
        return rgb != null ? rgb : self.get(key);
    }
}
