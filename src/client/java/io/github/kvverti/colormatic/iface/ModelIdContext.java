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
package io.github.kvverti.colormatic.iface;

/**
 * Context for {@link io.github.kvverti.colormatic.mixin.model.BakedQuadFactoryMixin} so that the callback knows
 * which model is being loaded.
 */
public final class ModelIdContext {

    /**
     * Whether the current model should be custom tinted, which is the case if and only if
     * - Colormatic has custom colors for the block state, and
     * - the block does not already have a color provider, and
     * - the model does not define tint index for any faces
     */
    public static boolean customTintCurrentModel;

    private ModelIdContext() {
    }
}
