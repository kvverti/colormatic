package io.github.kvverti.colormatic.iface;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;

/**
 * Compatibility interface for mixing into Sodium. This interface effectively "forward declares" the methods
 * declared in {@code BlockColorsExtended} in order to prevent a non-fatal exception from being thrown within
 * mixin when Sodium is not installed.
 */
public interface SodiumColorProviderCompat {
    BlockColorProvider getColorProvider(BlockState state);
}
