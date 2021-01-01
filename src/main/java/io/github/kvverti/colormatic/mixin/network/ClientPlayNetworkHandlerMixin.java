package io.github.kvverti.colormatic.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private DynamicRegistryManager registryManager;

    /**
     * We loop through and make the instances identical since we want to be able to get the ID for a given dimension
     * type.
     */
    @ModifyVariable(
        method = "onPlayerRespawn",
        ordinal = 0,
        at = @At(
            value = "STORE",
            ordinal = 0
        )
    )
    private DimensionType fixDimensionTypeOnPlayerRespawn(DimensionType target) {
        Registry<DimensionType> registry = this.registryManager.getDimensionTypes();
        for(DimensionType dimType : registry) {
            if(dimType.equals(target)) {
                return dimType;
            }
        }
        return target;
    }
}
