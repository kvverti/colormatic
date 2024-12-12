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
package io.github.kvverti.colormatic;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class ColormaticConfigScreen extends Screen {

    private static final int STANDARD_WIDTH = 150;
    private static final int STANDARD_HEIGHT = 20;
    private static final int STANDARD_MARGIN = 5;
    private static final int TOOLTIP_WIDTH = 200;

    private static final int TOP_MARGIN = 20;

    private final ColormaticConfig config;
    private final Screen parent;

    private int fogSectionBottom;

    protected ColormaticConfigScreen(Text title, Screen parent, ColormaticConfig config) {
        super(title);
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();
        var centerX = this.width / 2;
        // sky and fog settings
        var clearSkyBtn = this.addDrawableChild(CyclingButtonWidget
            .onOffBuilder()
            .initially(config.clearSky)
            .tooltip(value -> Tooltip.of(Text.translatable("colormatic.config.option.clearSky.desc")))
            .build(
                centerX - STANDARD_MARGIN - STANDARD_WIDTH,
                TOP_MARGIN + 2 * STANDARD_HEIGHT,
                STANDARD_WIDTH,
                STANDARD_HEIGHT,
                Text.translatable("colormatic.config.option.clearSky"),
                (button, value) -> config.clearSky = value));
        var clearVoidBtn = this.addDrawableChild(CyclingButtonWidget
            .onOffBuilder()
            .initially(config.clearVoid)
            .tooltip(value -> Tooltip.of(Text.translatable("colormatic.config.option.clearVoid.desc")))
            .build(
                centerX + STANDARD_MARGIN,
                TOP_MARGIN + 2 * STANDARD_HEIGHT,
                STANDARD_WIDTH,
                STANDARD_HEIGHT,
                Text.translatable("colormatic.config.option.clearVoid"),
                (button, value) -> config.clearVoid = value));
        this.fogSectionBottom = clearVoidBtn.getY() + clearVoidBtn.getHeight() + STANDARD_MARGIN;
        // lighting settings
        var blendSkyLightBtn = this.addDrawableChild(CyclingButtonWidget
            .onOffBuilder()
            .initially(config.blendSkyLight)
            .tooltip(value -> Tooltip.of(Text.translatable("colormatic.config.option.blendSkyLight.desc")))
            .build(
                clearSkyBtn.getX(),
                this.fogSectionBottom + 2 * STANDARD_HEIGHT,
                STANDARD_WIDTH,
                STANDARD_HEIGHT,
                Text.translatable("colormatic.config.option.blendSkyLight"),
                (button, value) -> config.blendSkyLight = value
            ));
        this.addDrawableChild(CyclingButtonWidget
            .onOffBuilder()
            .initially(config.flickerBlockLight)
            .tooltip(value -> Tooltip.of(Text.translatable("colormatic.config.option.flickerBlockLight.desc")))
            .build(
                clearVoidBtn.getX(),
                this.fogSectionBottom + 2 * STANDARD_HEIGHT,
                STANDARD_WIDTH,
                STANDARD_HEIGHT,
                Text.translatable("colormatic.config.option.flickerBlockLight"),
                (button, value) -> config.flickerBlockLight = value
            ));
        this.addDrawableChild(new BlockLightIntensitySlider(
            blendSkyLightBtn.getX(),
            blendSkyLightBtn.getY() + blendSkyLightBtn.getHeight() + 2 * ColormaticConfigScreen.STANDARD_MARGIN
        ));
        // done button
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .position(centerX - (STANDARD_WIDTH / 2), this.height - 2 * STANDARD_HEIGHT)
                .width(STANDARD_WIDTH)
                .build()
        );
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        // draw category labels
        /*this.renderBackground(matrices);
        var title = Text.translatable("colormatic.config.title");
        var width = this.textRenderer.getWidth(title);
        this.textRenderer.drawWithShadow(matrices, title, (this.width - width) / 2.0f, TOP_MARGIN, -1);
        var fogSettingsTitle = Text.translatable("colormatic.config.category.fog");
        width = this.textRenderer.getWidth(fogSettingsTitle);
        this.textRenderer.drawWithShadow(matrices, fogSettingsTitle, (this.width - width) / 2.0f, TOP_MARGIN + STANDARD_HEIGHT, -1);
        var lightSettingsTitle = Text.translatable("colormatic.config.category.light");
        width = this.textRenderer.getWidth(lightSettingsTitle);
        this.textRenderer.drawWithShadow(matrices, lightSettingsTitle, (this.width - width) / 2.0f, this.fogSectionBottom + STANDARD_HEIGHT, -1);
        var tooltip = this.getTooltip(mouseX, mouseY);
        if(tooltip != null) {
            this.renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
        }*/
        super.render(matrices, mouseX, mouseY, delta);
    }

    /*private @Nullable List<OrderedText> getTooltip(int mouseX, int mouseY) {
        for(var element : this.children()) {
            if(element.isMouseOver(mouseX, mouseY)) {
                if(element instanceof OrderableTooltip tooltipElement) {
                    return tooltipElement.getOrderedTooltip();
                }
                break;
            }
        }
        return null;
    }*/

    @Override
    public void close() {
        ColormaticConfigController.persist(config);
        this.client.setScreen(parent);
    }

    /**
     * Slider widget for relative block light intensity.
     */
    private class BlockLightIntensitySlider extends SliderWidget {

        public BlockLightIntensitySlider(int x, int y) {
            super(
                x,
                y,
                ColormaticConfigScreen.STANDARD_WIDTH,
                ColormaticConfigScreen.STANDARD_HEIGHT,
                Text.empty(),
                1.0 - ColormaticConfigScreen.this.config.relativeBlockLightIntensityExponent / -16.0);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.translatable("colormatic.config.option.relativeBlockLightIntensity")
                .append(": ")
                .append(Text.literal(String.valueOf((int)(100 * Math.exp(ColormaticConfig.scaled(this.configValue()))))).append("%")));
        }

        @Override
        protected void applyValue() {
            config.relativeBlockLightIntensityExponent = this.configValue();
        }

        private double configValue() {
            return (1.0 - this.value) * -16.0;
        }

        //@Override
        public List<OrderedText> getOrderedTooltip() {
            return ColormaticConfigScreen.this.textRenderer.wrapLines(Text.translatable("colormatic.config.option.relativeBlockLightIntensity.desc"), TOOLTIP_WIDTH);
        }
    }
}
