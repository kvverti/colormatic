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
package io.github.kvverti.colormatic.mixin.xp;

import io.github.kvverti.colormatic.Colormatic;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls experience orb color pulses.
 */
@Mixin(ExperienceOrbEntityRenderer.class)
public abstract class ExperienceOrbEntityRendererMixin extends EntityRenderer<ExperienceOrbEntity> {

    private ExperienceOrbEntityRendererMixin() {
        super(null);
    }

    @Unique
    private boolean custom;

    @Unique
    private int customRed;

    @Unique
    private int customGreen;

    @Unique
    private int customBlue;

    @Inject(method = "method_3966", at = @At("HEAD"))
    private void onRenderSetColor(ExperienceOrbEntity entity, double x, double y, double z, float eh, float partialTicks, CallbackInfo info) {
        if(Colormatic.EXPERIENCE_ORB_COLORS.hasCustomColormap()) {
            custom = true;
            float ticksPerCycle = Colormatic.COLOR_PROPS.getProperties().getXpOrbTime() / 50.0f;
            float frac = (1 - MathHelper.cos((entity.renderTicks + partialTicks) * (float)(2 * Math.PI) / ticksPerCycle)) / 2;
            int color = Colormatic.EXPERIENCE_ORB_COLORS.getColorFraction(frac);
            customRed = (color >> 16) & 0xff;
            customGreen = (color >> 8) & 0xff;
            customBlue = color & 0xff;
        } else {
            custom = false;
        }
    }

    @Redirect(
        method = "method_3966",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/BufferBuilder;color(IIII)Lnet/minecraft/client/render/BufferBuilder;"
        )
    )
    private BufferBuilder proxyColor(BufferBuilder self, int r, int g, int b, int a) {
        if(custom) {
            r = customRed;
            g = customGreen;
            b = customBlue;
        }
        return self.color(r, g, b, a);
    }
}
