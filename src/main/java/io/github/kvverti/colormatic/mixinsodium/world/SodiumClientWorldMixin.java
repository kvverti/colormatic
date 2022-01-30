package io.github.kvverti.colormatic.mixinsodium.world;

import me.jellysquid.mods.sodium.client.world.BiomeSeedProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

// This for some reason fixes the whole Sodium BiomeSeedProvider issue
@Mixin(value = ClientWorld.class, priority = 2039)
public class SodiumClientWorldMixin implements BiomeSeedProvider {

    @Override
    public long getBiomeSeed() {
        return ((SodiumBiomeAccessAccessor) ((World) (Object) this).getBiomeAccess()).getSeed();
    }
}
