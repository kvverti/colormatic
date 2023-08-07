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
