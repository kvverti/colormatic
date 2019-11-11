package io.github.kvverti.colormatic.colormap;

import net.minecraft.util.math.BlockPos;

public interface ColormaticBlockRenderView {

    int colormatic_getColor(BlockPos pos, ColormaticResolver resolver);
}
