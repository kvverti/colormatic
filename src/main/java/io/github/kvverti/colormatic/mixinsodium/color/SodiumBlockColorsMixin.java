package io.github.kvverti.colormatic.mixinsodium.color;

import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.iface.SodiumColorProviderCompat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;

@Mixin(value = BlockColors.class, priority = 1001)
@Implements(@Interface(iface = SodiumColorProviderCompat.class, prefix = "i$", remap = Interface.Remap.NONE))
public abstract class SodiumBlockColorsMixin implements SodiumColorProviderCompat {

    @Unique
    private static final BlockColorProvider COLORMATIC_PROVIDER =
        (state, world, pos, tintIndex) -> BiomeColormaps.getBiomeColor(state, world, pos);

    /**
     * Displace Sodium's implementation to first check Colormatic's custom block colors.
     */
    @Intrinsic(displace = true)
    public BlockColorProvider i$getColorProvider(BlockState state) {
        if(BiomeColormaps.isCustomColored(state)) {
            return COLORMATIC_PROVIDER;
        }
        return this.getColorProvider(state);
    }
}
