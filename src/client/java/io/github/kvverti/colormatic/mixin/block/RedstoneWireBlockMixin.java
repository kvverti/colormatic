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
package io.github.kvverti.colormatic.mixin.block;

import io.github.kvverti.colormatic.Colormatic;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
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

    @Inject(method = "getWireColor", at = @At("HEAD"), cancellable = true)
    private static void onWireColor(int power, CallbackInfoReturnable<Integer> info) {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            info.setReturnValue(Colormatic.REDSTONE_COLORS.getColorBounded(power));
        }
    }

    /*
     * Relevant bytecode:
     * L1
     *  LINENUMBER 447 L1
     *  ILOAD 5
     *  IFNE L2                  // if(i != 0)
     * L3
     *  LINENUMBER 448 L3
     *  RETURN
     * L2
     *  LINENUMBER 450 L2
     *  FRAME APPEND [I]
     *  <injection point>
     *  GETSTATIC net/minecraft/util/math/Direction$Type.HORIZONTAL : Lnet/minecraft/util/math/Direction$Type;
     *  INVOKEVIRTUAL net/minecraft/util/math/Direction$Type.iterator ()Ljava/util/Iterator;
     *  ASTORE 6
     */
    @Inject(
        method = "randomDisplayTick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/util/math/Direction$Type;HORIZONTAL:Lnet/minecraft/util/math/Direction$Type;",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void onRandomDisplayTick(BlockState state, World world, BlockPos pos, Random rand, CallbackInfo info, int power) {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            double x = pos.getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            double y = ((float)pos.getY() + 0.0625F);
            double z = pos.getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            int color = Colormatic.REDSTONE_COLORS.getColorBounded(power);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >> 8) & 0xff) / 255.0f;
            float b = (color & 0xff) / 255.0f;
            world.addParticle(new DustParticleEffect(new Vector3f(r, g, b), 1.0f), x, y, z, 0.0, 0.0, 0.0);
            info.cancel();
        }
    }
}
