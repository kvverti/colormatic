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
package io.github.kvverti.colormatic.mixin.render;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Provides tool durability bar color customization capability.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow private void renderGuiQuad(BufferBuilder builder, int x, int y, int w, int h, int r, int g, int b, int a) {}

    @Redirect(
        method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiQuad(Lnet/minecraft/client/render/BufferBuilder;IIIIIIII)V",
            ordinal = 1
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/item/ItemStack;isDamaged()Z"
            )
        )
    )
    private void proxyRenderGuiQuad(ItemRenderer self, BufferBuilder buffer, int x, int y, int w, int h, int r, int g, int b, int a,
            TextRenderer whoCares0, ItemStack stack, int whoCares1, int whoCares2, String whoCares3) {
        if(Colormatic.DURABILITY_COLORS.hasCustomColormap()) {
            float damage = stack.getDamage();
            float maxDamage = stack.getMaxDamage();
            float durability = Math.max(0.0f, (maxDamage - damage) / maxDamage);
            int color = Colormatic.DURABILITY_COLORS.getColorFraction(durability);
            r = (color >> 16) & 0xff;
            g = (color >>  8) & 0xff;
            b = (color >>  0) & 0xff;
        }
        this.renderGuiQuad(buffer, x, y, w, h, r, g, b, a);
    }
}
