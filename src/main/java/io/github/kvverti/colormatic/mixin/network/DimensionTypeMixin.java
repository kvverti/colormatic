/*
 * Colormatic
 * Copyright (C) 2022  Thalia Nero
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

import java.util.Arrays;
import java.util.OptionalLong;

import io.github.kvverti.colormatic.iface.DimensionTypeEquals;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeEquals {
    @Shadow
    @Final
    private OptionalLong fixedTime;

    @Shadow
    @Final
    private boolean hasSkyLight;

    @Shadow
    @Final
    private boolean hasCeiling;

    @Shadow
    @Final
    private boolean ultrawarm;

    @Shadow
    @Final
    private boolean natural;

    @Shadow
    @Final
    private double coordinateScale;

    @Shadow
    @Final
    private boolean hasEnderDragonFight;

    @Shadow
    @Final
    private boolean piglinSafe;

    @Shadow
    @Final
    private boolean bedWorks;

    @Shadow
    @Final
    private boolean respawnAnchorWorks;

    @Shadow
    @Final
    private boolean hasRaids;

    @Shadow
    @Final
    private int minimumY;

    @Shadow
    @Final
    private int height;

    @Shadow
    @Final
    private int logicalHeight;

    @Shadow
    @Final
    private TagKey<Block> infiniburn;

    @Shadow
    @Final
    private Identifier effects;

    @Shadow
    @Final
    private float ambientLight;

    @Shadow
    @Final
    private transient float[] brightnessByLightLevel;

    @Override
    public boolean colormatic_equals(@Nullable DimensionType other) {
        if(other == null) {
            return false;
        }
        // no-op cast
        var that = (DimensionTypeMixin)(Object)other;
        return this.fixedTime.equals(that.fixedTime)
            && this.hasSkyLight == that.hasSkyLight
            && this.hasCeiling == that.hasCeiling
            && this.ultrawarm == that.ultrawarm
            && this.natural == that.natural
            && this.coordinateScale == that.coordinateScale
            && this.hasEnderDragonFight == that.hasEnderDragonFight
            && this.piglinSafe == that.piglinSafe
            && this.bedWorks == that.bedWorks
            && this.respawnAnchorWorks == that.respawnAnchorWorks
            && this.hasRaids == that.hasRaids
            && this.minimumY == that.minimumY
            && this.height == that.height
            && this.logicalHeight == that.logicalHeight
            && this.infiniburn.equals(that.infiniburn)
            && this.effects.equals(that.effects)
            && this.ambientLight == that.ambientLight
            && Arrays.equals(this.brightnessByLightLevel, that.brightnessByLightLevel);
    }
}
