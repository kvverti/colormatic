package io.github.kvverti.colormatic;

public final class ColormaticConfig {

    // @ConfigEntry.Category("fog")
    // @Comment("Use sky colors for fog")
    public boolean clearSky = false;

    // @ConfigEntry.Category("fog")
    // @Comment("Use fog colors for the void")
    public boolean clearVoid = false;

    // @ConfigEntry.Category("light")
    // @Comment("Toggle sunrise/sunset blending")
    public boolean blendSkyLight = true;

    // @ConfigEntry.Category("light")
    // @Comment("Toggle block light flicker")
    public boolean flickerBlockLight = true;
}
