/*
 * Colormatic
 * Copyright (C) 2021  Thalia Nero
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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.kvverti.colormatic.colormap.BiomeColormaps;
import io.github.kvverti.colormatic.iface.ModelIdContext;
import io.github.kvverti.colormatic.mixin.color.BlockColorsAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Wrap block state models that are Colormatic custom colored in a baked model that enables the tint index
 * if the block does not already have a color provider.
 */
@Mixin(BakedQuadFactory.class)
public abstract class BakedQuadFactoryMixin {

    @Unique
    private static final BlockStateArgumentType BLOCK_STATE_PARSER = BlockStateArgumentType.blockState();

    /**
     * Replaces non-tinted quads with tinted quads when baked if the block has no color provider.
     * We parse the block state from the model ID. It is important that custom biome colormaps are
     * reloaded <em>before</em> this callback is run.
     *
     * @param self The model element face that defines the tint index.
     */
    @Redirect(
        method = "bake",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/model/json/ModelElementFace;tintIndex:I"
        )
    )
    private int addTintToCustomColoredModel(ModelElementFace self) {
        ModelIdentifier modelId = ModelIdContext.currentModelId;
        if(modelId != null) {
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
                    return self.tintIndex;
                }
            }
            // set up tint index replacement if
            //  - Colormatic has custom colors for the block state
            //  - the block does not already have a color provider
            // note: we're calling a custom biome colors method. Re-evaluate if we combine custom biome colors
            // with provided biome colors.
            if(self.tintIndex == -1 && BiomeColormaps.isCustomColored(blockState)) {
                var colorProviders = ((BlockColorsAccessor)MinecraftClient.getInstance().getBlockColors()).getProviders();
                if(!colorProviders.containsKey(Registry.BLOCK.getRawId(blockState.getBlock()))) {
                    return 0;
                }
            }
        }
        return self.tintIndex;
    }
}
