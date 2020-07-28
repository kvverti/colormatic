package io.github.kvverti.colormatic.iface;

import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;

/**
 * Compatibility interface for mixing into Sodium. This interface effectively "forward declares" the methods
 * declared in {@link BlockColorsExtended} in order to prevent a non-fatal exception from being thrown within
 * mixin when Sodium is not installed.
 */
public interface SodiumColorProviderCompat {
    BlockColorProvider getColorProvider(BlockState state);
}

@SuppressWarnings("unused")
class EnsureMethodSignaturesAreCorrect implements SodiumColorProviderCompat, BlockColorsExtended {

    @Override
    public BlockColorProvider getColorProvider(BlockState state) {
        throw new AssertionError();
    }
}
