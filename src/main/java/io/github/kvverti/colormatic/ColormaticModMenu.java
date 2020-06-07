///*
// * Colormatic
// * Copyright (C) 2019  Thalia Nero
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//package io.github.kvverti.colormatic;
//
//import io.github.prospector.modmenu.api.ModMenuApi;
//
//import java.util.function.Function;
//
//import net.minecraft.client.gui.screen.Screen;
//
//public class ColormaticModMenu implements ModMenuApi {
//
//    @Override
//    public String getModId() {
//        return Colormatic.MODID;
//    }
//
//    @Override
//    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
//        return parent -> ColormaticConfigController.getConfigScreen(Colormatic.config(), parent);
//    }
//}
