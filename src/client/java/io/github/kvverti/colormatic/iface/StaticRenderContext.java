/*
 * Colormatic
 * Copyright (C) 2024  Thalia Nero
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
package io.github.kvverti.colormatic.iface;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.world.ClientWorld;

/**
 * Static state for global rendering (such as the background and world renderer).
 */
public final class StaticRenderContext {

    /**
     * Context for static sky tinting.
     */
    public static final ThreadLocal<SkyData> SKY_CONTEXT = ThreadLocal.withInitial(SkyData::new);

    /**
     * Context for static fog tinting.
     */
    public static final ThreadLocal<SkyData> FOG_CONTEXT = ThreadLocal.withInitial(SkyData::new);

    /**
     * Temporary data class for sky and fog context.
     */
    public static final class SkyData {

        @Nullable
        public ClientWorld world;
        public int posX;
        public int posY;
        public int posZ;
    }
}
