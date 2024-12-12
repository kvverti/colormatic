/*
 * Colormatic
 * Copyright (C) 2021-2022  Thalia Nero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As an additional permission, when conveying the Corresponding Source of an
 * object code form of this work, you may exclude the Corresponding Source for
 * "Minecraft" by Mojang Studios, AB.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.kvverti.colormatic.mixin.model;

import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.iface.ModelIdContext;
import io.github.kvverti.colormatic.mixin.color.BlockColorsAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

@Mixin(targets = "net.minecraft.client.render.model.ModelLoader$BakerImpl")
public abstract class ModelLoaderMixin {

    /**
     * Manually parse a block state given its variant representation. This mixin is too early to rely on
     * the dynamic registry manager.
     * @param blockId Block ID
     * @param variant String of the form prop1=value1,prop2=value2
     * @return The block state represented by the ID and variant, or a default block state if anything is invalid.
     */
    @Unique
    private static BlockState getBlockState(Identifier blockId, String variant) {
        var block = Registries.BLOCK.get(blockId);
        var stateManager = block.getStateManager();
        var blockState = block.getDefaultState();
        var propValues = variant.split(",");
        for(var propValue : propValues) {
            var propAndValue = propValue.split("=");
            var prop = stateManager.getProperty(propAndValue[0]);
            if(prop == null) {
                continue;
            }
            blockState = withPropertyValue(blockState, prop, propAndValue[1]);
        }
        return blockState;
    }

    @Unique
    private static <T extends Comparable<T>> BlockState withPropertyValue(BlockState state, Property<T> property, String valueStr) {
        var value = property.parse(valueStr).orElse(null);
        return value == null ? state : state.with(property, value);
    }

    /**
     * Partially determines whether Colormatic should replace the tint on a model.
     * We parse the block state from the model ID. It is important that custom biome colormaps are
     * reloaded <em>before</em> this callback is run.
     */
    @Inject(
        method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/model/UnbakedModel;bake(Lnet/minecraft/client/render/model/Baker;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedModel;"
        )
    )
    private void setModelIdContext(Identifier id, ModelBakeSettings settings, CallbackInfoReturnable<BakedModel> info) {
        if(id instanceof ModelIdentifier modelId) {
            ModelIdContext.customTintCurrentModel = false;

            BlockState blockState;
            var blockId = new Identifier(modelId.getNamespace(), modelId.getPath());
            if(modelId.getVariant().equals("inventory")) {
                // we're using the block color providers for detecting non-custom item tinting for now
                blockState = Registries.BLOCK.get(blockId).getDefaultState();
            } else {
                blockState = getBlockState(blockId, modelId.getVariant());
            }
            // test first two criteria
            //  - Colormatic has custom colors for the block state
            //  - the block does not already have a color provider
            // note: we're calling a custom biome colors method. Re-evaluate if we combine custom biome colors
            // with provided biome colors.
            if(BiomeColormaps.isCustomColored(blockState)) {
                var colorProviders = ((BlockColorsAccessor)MinecraftClient.getInstance().getBlockColors()).getProviders();
                if(!colorProviders.containsKey(Registries.BLOCK.getRawId(blockState.getBlock()))) {
                    // tentatively set to true - further checking in JsonUnbakedModelMixin
                    ModelIdContext.customTintCurrentModel = true;
                }
            }
        }
    }
}
