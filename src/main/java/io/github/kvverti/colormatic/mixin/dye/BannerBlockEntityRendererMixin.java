/*
 * Colormatic
 * Copyright (C) 2019-2020  Thalia Nero
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

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.util.DyeColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies custom colors to banner textures. This requires colors.json
 * to be reloaded beforehand. See TextureManagerMixin.
 */
@Mixin(BannerBlockEntityRenderer.class)
public abstract class BannerBlockEntityRendererMixin extends BlockEntityRenderer<BannerBlockEntity> {

    private BannerBlockEntityRendererMixin() {
        super(null);
    }

    @Redirect(
        method = "renderCanvas",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/DyeColor;getColorComponents()[F"
        )
    )
    private static float[] proxyColorComponents(DyeColor self) {
        float[] color = Colormatic.COLOR_PROPS.getProperties().getBannerRgb(self);
        if(color != null) {
            return color;
        }
        return self.getColorComponents();
    }
}
