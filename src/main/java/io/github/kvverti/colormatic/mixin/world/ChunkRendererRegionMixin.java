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
package io.github.kvverti.colormatic.mixin.world;

import io.github.kvverti.colormatic.colormap.ColormaticBlockRenderView;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkRendererRegion.class)
public abstract class ChunkRendererRegionMixin implements ColormaticBlockRenderView {

    @Shadow @Final protected World world;

    @Override
    public int colormatic_getColor(BlockPos pos, ColormaticResolver resolver) {
        return ((ColormaticBlockRenderView)this.world).colormatic_getColor(pos, resolver);
    }
}
