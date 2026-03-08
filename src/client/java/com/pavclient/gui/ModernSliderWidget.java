package com.pavclient.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Draggable modern slider helper.
 */
public class ModernSliderWidget extends SliderWidget {
    private final String label;
    private final double min;
    private final double max;
    private final double step;
    private final Consumer<Double> onValueChanged;
    private final Function<Double, String> valueFormatter;

    public ModernSliderWidget(int x, int y, int width, int height,
                              String label,
                              double min, double max, double step,
                              double current,
                              Function<Double, String> valueFormatter,
                              Consumer<Double> onValueChanged) {
        super(x, y, width, height, Text.empty(), normalize(current, min, max));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.valueFormatter = valueFormatter;
        this.onValueChanged = onValueChanged;
        this.value = normalize(current, min, max);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double actual = snap(denormalize(this.value, min, max));
        this.setMessage(Text.literal(label + ": " + valueFormatter.apply(actual)));
    }

    @Override
    protected void applyValue() {
        double actual = snap(denormalize(this.value, min, max));
        this.value = normalize(actual, min, max);
        onValueChanged.accept(actual);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) return;

        boolean hovered = this.isMouseOver(mouseX, mouseY);
        GuiHelper.drawModernButton(context, this.getX(), this.getY(), this.width, this.height, hovered, this.active);

        int pad = 8;
        int trackY = this.getY() + this.height / 2;
        int trackX1 = this.getX() + pad;
        int trackX2 = this.getX() + this.width - pad;

        // Track
        context.fill(trackX1, trackY - 1, trackX2, trackY + 1, 0x66FFFFFF);

        // Filled part
        int knobX = trackX1 + (int) ((trackX2 - trackX1) * this.value);
        context.fill(trackX1, trackY - 1, knobX, trackY + 1, 0xFF9E97D4);

        // Knob
        context.fill(knobX - 2, this.getY() + 4, knobX + 2, this.getY() + this.height - 4, hovered ? 0xFFFFFFFF : 0xFFDDDDDD);

        // Label
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(),
                this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFFFF);
    }

    private static double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }

    private static double denormalize(double value, double min, double max) {
        return min + (max - min) * value;
    }

    private double snap(double value) {
        if (step <= 0) return Math.max(min, Math.min(max, value));
        double snapped = Math.round((value - min) / step) * step + min;
        return Math.max(min, Math.min(max, snapped));
    }
}
