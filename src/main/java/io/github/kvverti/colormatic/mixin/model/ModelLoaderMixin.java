package io.github.kvverti.colormatic.mixin.model;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.mixin.color.BlockColorsAccessor;
import io.github.kvverti.colormatic.model.ColormaticTintedBakedModel;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Wrap block state models that are Colormatic custom colored in a baked model that enables the tint index
 * if the block does not already have a color provider.
 */
@Mixin(ModelLoader.class)
abstract class ModelLoaderMixin {

    @Unique
    private static final BlockStateArgumentType BLOCK_STATE_PARSER = BlockStateArgumentType.blockState();

    // change to inject + modify variable if there are incompatibility reports
    @Dynamic("Model baking lambda in upload()")
    @ModifyArg(
        method = "method_4733",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
            remap = false
        ),
        index = 1
    )
    private Object wrapColormaticBakedModel(Object key, Object model) {
        if(key instanceof ModelIdentifier modelId) {
            BlockState blockState;
            if(modelId.getVariant().equals("inventory")) {
                // we're using the block color providers for detecting non-custom item tinting for now
                var blockId = new Identifier(modelId.getNamespace(), modelId.getPath());
                blockState = Registry.BLOCK.get(blockId).getDefaultState();
            } else {
                var blockStateDesc = modelId.getNamespace() + ":" + modelId.getPath() + "[" + modelId.getVariant() + "]";
                try {
                    blockState = BLOCK_STATE_PARSER.parse(new StringReader(blockStateDesc)).getBlockState();
                } catch(CommandSyntaxException e) {
                    return model;
                }
            }
            // set up tint index replacement if
            //  - Colormatic has custom colors for the block state
            //  - the block does not already have a color provider
            // note: we're calling a custom biome colors method. Re-evaluate if we combine custom biome colors
            // with provided biome colors.
            if(BiomeColormaps.isCustomColored(blockState)) {
                var colorProviders = ((BlockColorsAccessor)MinecraftClient.getInstance().getBlockColors()).getProviders();
                if(!colorProviders.containsKey(Registry.BLOCK.getRawId(blockState.getBlock()))) {
                    return new ColormaticTintedBakedModel((BakedModel)model);
                }
            }
        }
        return model;
    }
}
