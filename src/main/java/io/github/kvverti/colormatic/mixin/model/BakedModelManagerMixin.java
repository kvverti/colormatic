package io.github.kvverti.colormatic.mixin.model;

import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

/**
 * Reload the custom block colors before block models reload, because block models depend on the custom
 * color status of block states.
 */
@Mixin(BakedModelManager.class)
abstract class BakedModelManagerMixin {

    @Inject(
        method = "prepare",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/profiler/Profiler;startTick()V",
            shift = At.Shift.AFTER
        )
    )
    private void reloadColormaticCustomBiomeColors(ResourceManager manager, Profiler profiler, CallbackInfoReturnable<ModelLoader> info) {
        Colormatic.CUSTOM_BLOCK_COLORS.reload(manager);
    }
}
