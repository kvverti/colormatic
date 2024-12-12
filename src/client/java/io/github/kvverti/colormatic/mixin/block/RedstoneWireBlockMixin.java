/*
 * Colormatic
 * Copyright (C) 2021-2024  Thalia Nero
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
package io.github.kvverti.colormatic.mixin.block;

import io.github.kvverti.colormatic.Colormatic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Provides redstone wire color customization capability.
 */
@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin extends Block {

    private RedstoneWireBlockMixin() {
        super(null);
    }

    /**
     * Modifies any call to the public interface.
     * @param power redstone power level
     * @param info callback info
     */
    @Inject(method = "getWireColor", at = @At("HEAD"), cancellable = true)
    private static void onWireColor(int power, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.REDSTONE_COLORS.getColorBounded(power));
        }
    }

    /**
     * Modify every call to change the color of redstone particles.
     */
    @ModifyArg(
        method = "randomDisplayTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/RedstoneWireBlock;addPoweredParticles(Lnet/minecraft/world/World;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/Direction;FF)V"
        )
    )
    private Vec3d onRandomDisplayTick(World world, Random rand, BlockPos pos, Vec3d originalColor, Direction direction, Direction direction2, float f1, float f2) {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            int power = world.getBlockState(pos).get(RedstoneWireBlock.POWER);
            int color = Colormatic.REDSTONE_COLORS.getColorBounded(power);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >> 8) & 0xff) / 255.0f;
            float b = (color & 0xff) / 255.0f;
            return new Vec3d(r, g, b);
        }
        return originalColor;
    }
}
