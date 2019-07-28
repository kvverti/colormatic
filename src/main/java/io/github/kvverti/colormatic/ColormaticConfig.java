package io.github.kvverti.colormatic;

import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1.shadowed.blue.endless.jankson.Comment;

@Config(name = Colormatic.MODID)
public final class ColormaticConfig implements ConfigData {

    @ConfigEntry.Category("fog")
    @Comment("Use sky colors for fog")
    public boolean clearSky = false;

    @ConfigEntry.Category("fog")
    @Comment("Use fog colors for the void")
    public boolean clearVoid = false;

    @ConfigEntry.Category("light")
    @Comment("Toggle sunrise/sunset blending")
    public boolean blendSkyLight = true;

    @ConfigEntry.Category("light")
    @Comment("Toggle block light flicker")
    public boolean flickerBlockLight = true;
}
