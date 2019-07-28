package io.github.kvverti.colormatic;

import me.sargunvohra.mcmods.autoconfig1.ConfigData;
import me.sargunvohra.mcmods.autoconfig1.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1.shadowed.blue.endless.jankson.Comment;

@Config(name = Colormatic.MODID)
public final class ColormaticConfig implements ConfigData {

    @ConfigEntry.Category("fog")
    @Comment("Toggle fog rendering")
    public final boolean enableFog = true;

    @ConfigEntry.Category("fog")
    @Comment("Use sky colors for fog")
    public final boolean clearSky = false;

    @ConfigEntry.Category("light")
    @Comment("Toggle sunrise/sunset blending")
    public final boolean blendSkyLight = true;

    @ConfigEntry.Category("light")
    @Comment("Toggle block light flicker")
    public final boolean flickerBlockLight = true;
}
