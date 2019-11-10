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

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.LayeredTexture;
import net.minecraft.util.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies custom colors to banner textures. This requires colors.json
 * to be reloaded beforehand. See TextureManagerMixin.
 */
@Mixin(LayeredTexture.class)
public abstract class BannerTextureMixin extends AbstractTexture {

    // @Redirect(
    //     method = "load",
    //     at = @At(
    //         value = "INVOKE",
    //         target = "Lnet/minecraft/util/DyeColor;getColorSwapped()I"
    //     )
    // )
    // private int proxyColorSwapped(DyeColor self) {
    //     int color = Colormatic.COLOR_PROPS.getProperties().getBanner(self);
    //     if(color != 0) {
    //         return (color & 0x00ff00) |
    //             ((color & 0xff0000) >> 16) |
    //             ((color & 0x0000ff) << 16);
    //     }
    //     return ((DyeColorAccessor)(Object)self).colormatic_getColorSwapped();
    // }
}
