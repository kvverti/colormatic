package io.github.kvverti.colormatic.mixinsodium.world;

import io.github.kvverti.colormatic.colormap.ColormaticBlockRenderView;
import io.github.kvverti.colormatic.colormap.ColormaticResolver;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.common.util.pool.ReusableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

@Mixin(WorldSlice.class)
public abstract class WorldSliceMixin extends ReusableObject implements ColormaticBlockRenderView {

    @Shadow(remap = false)
    public native Biome getCachedBiome(int x, int z);

    /**
     * Copied from {@link io.github.kvverti.colormatic.mixin.world.ClientWorldMixin} for efficiency. The
     * implementations must be kept in sync.
     */
    @Override
    public int colormatic_getColor(BlockPos pos, ColormaticResolver resolver) {
        int r = 0;
        int g = 0;
        int b = 0;
        int radius = MinecraftClient.getInstance().options.biomeBlendRadius;
        Iterable<BlockPos> coll = BlockPos.iterate(
            pos.getX() - radius, pos.getY(), pos.getZ() - radius,
            pos.getX() + radius, pos.getY(), pos.getZ() + radius);
        for(BlockPos curpos : coll) {
            Biome biome = this.getCachedBiome(curpos.getX(), curpos.getZ());
            int color = resolver.getColor(biome, curpos);
            r += (color & 0xff0000) >> 16;
            g += (color & 0x00ff00) >> 8;
            b += (color & 0x0000ff);
        }
        int posCount = (radius * 2 + 1) * (radius * 2 + 1);
        return ((r / posCount & 255) << 16) | ((g / posCount & 255) << 8) | (b / posCount & 255);
    }
}
