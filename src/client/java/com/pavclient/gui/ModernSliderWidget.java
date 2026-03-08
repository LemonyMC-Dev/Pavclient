package com.pavclient.gui;

import net.minecraft.client.gui.widget.SliderWidget;
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
