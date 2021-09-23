package io.github.kvverti.colormatic.mixin.color;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.collection.IdList;

@Mixin(BlockColors.class)
public interface BlockColorsAccessor {
    @Accessor
    IdList<BlockColorProvider> getProviders();
}
