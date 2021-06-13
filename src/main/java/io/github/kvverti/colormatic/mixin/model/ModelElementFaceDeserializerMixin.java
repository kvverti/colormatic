package io.github.kvverti.colormatic.mixin.model;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.render.model.json.ModelElementFace$Deserializer")
public class ModelElementFaceDeserializerMixin {

    private int modifyTintIndex(int tintIndex) {
        if(tintIndex == -1) {
            // change to index 0 if a custom colormap for this block applies
        }
        return tintIndex;
    }
}
