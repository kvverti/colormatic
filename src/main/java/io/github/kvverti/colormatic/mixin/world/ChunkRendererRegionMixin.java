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
