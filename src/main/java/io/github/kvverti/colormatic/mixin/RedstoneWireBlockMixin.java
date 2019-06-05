/*
 * Colormatic
 * Copyright (C) 2019  Thalia Nero
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.kvverti.colormatic.mixin;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.block.Block;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.particle.DustParticleEffect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
     *  13: istore        5
     *  15: iload         5
     *  17: ifne          21    <--- if (int_1 != 0)
     *  20: return
     *  <injection point>
     *  21: aload_3
     *  22: invokevirtual #513                // Method net/minecraft/util/math/BlockPos.getX:()I
     *  25: i2d
     *  26: ldc2_w        #514                // double 0.5d
     */
    @Inject(
        method = "randomDisplayTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/BlockPos;getX()I",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        cancellable = true
    )
    private void onRandomDisplayTick(BlockState state, World world, BlockPos pos, Random rand, CallbackInfo info, int power) {
        if(Colormatic.REDSTONE_COLORS.hasCustomColormap()) {
            double x = pos.getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            double y = ((float)pos.getY() + 0.0625F);
            double z = pos.getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
            int color = Colormatic.REDSTONE_COLORS.getColorBounded(power);
            float r = ((color >> 16) & 0xff) / 255.0f;
            float g = ((color >>  8) & 0xff) / 255.0f;
            float b = ((color >>  0) & 0xff) / 255.0f;
            world.addParticle(new DustParticleEffect(r, g, b, 1.0f), x, y, z, 0.0, 0.0, 0.0);
            info.cancel();
        }
    }
}
