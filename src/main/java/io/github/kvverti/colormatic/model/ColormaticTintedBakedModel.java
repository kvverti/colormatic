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
package io.github.kvverti.colormatic.model;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

/**
 * A baked model that enables tint index for all quads, regardless of what was specified in the model.
 */
public final class ColormaticTintedBakedModel extends ForwardingBakedModel {

    public ColormaticTintedBakedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        context.pushTransform(ColormaticTintedBakedModel::applyTint);
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.pushTransform(ColormaticTintedBakedModel::applyTint);
        super.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();
    }

    private static boolean applyTint(MutableQuadView quad) {
        var tintIndex = quad.colorIndex();
        if(tintIndex == -1) {
            quad.colorIndex(0);
        }
        return true;
    }
}
