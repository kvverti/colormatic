package io.github.kvverti.colormatic.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.Identifier;

@Mixin(Identifier.class)
public abstract class IdentifierMixin {

    @Shadow
    public abstract String getNamespace();

    @Shadow
    public abstract String getPath();

    /**
     * Allow arbitrary characters in path names for Optifine files
     */
    @ModifyArg(
        method = "<init>([Ljava/lang/String;)V",
        index = 0,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/Identifier;isPathValid(Ljava/lang/String;)Z"
        )
    )
    private String skipValidationForColormatic(String path) {
        if(this.getNamespace().equals("minecraft") && this.getPath().startsWith("optifine/")) {
            path = "safe_id_for_allowing_invalid_chars";
        }
        return path;
    }
}
