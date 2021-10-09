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
package io.github.kvverti.colormatic.mixin.network;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private DynamicRegistryManager registryManager;

    @Shadow
    @Final
    private MinecraftClient client;

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
        Registry<DimensionType> registry = this.registryManager.get(Registry.DIMENSION_TYPE_KEY);
        for(DimensionType dimType : registry) {
            if(dimType.equals(target)) {
                return dimType;
            }
        }
        return target;
    }

    /**
     * Whether we've displayed the warning already.
     */
    @Unique
    private static boolean notified;

    /**
     * Add a warning for players who install Sodium without Indium.
     */
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void warnColormaticIndiumOnce(CallbackInfo info) {
        if(!notified && FabricLoader.getInstance().isModLoaded("sodium") && !FabricLoader.getInstance().isModLoaded("indium")) {
            var text = new LiteralText("")
                .append(new LiteralText("Colormatic: ")
                    .setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW, Formatting.BOLD)))
                .append(new TranslatableText("colormatic.sodium_dependency_warning")
                    .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            "https://modrinth.com/mod/indium"))
                        .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new TranslatableText("colormatic.sodium_dependency_warning.hover")))));
            this.client.player.sendSystemMessage(text, Util.NIL_UUID);
        }
        notified = true;
    }
}
