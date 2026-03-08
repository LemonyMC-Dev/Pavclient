package com.pavclient.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Modern GUI rendering utilities for PavClient.
 * Provides rounded rectangles, gradients, and modern styling.
 */
public final class GuiHelper {

    private GuiHelper() {}

    /**
     * Draws a modern semi-transparent dark panel background.
     */
    public static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        // Dark background with slight transparency
        context.fill(x, y, x + width, y + height, 0xCC1A1A2E);
        // Top accent line (gradient purple)
        context.fill(x, y, x + width, y + 2, 0xFF6C63FF);
        // Border
        drawBorder(context, x, y, width, height, 0x55FFFFFF);
    }

    /**
     * Draws a modern button background (idle state).
     */
    public static void drawButtonBackground(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        if (hovered) {
            context.fill(x, y, x + width, y + height, 0xDD6C63FF);
        } else {
            context.fill(x, y, x + width, y + height, 0xBB2D2B55);
        }
        drawBorder(context, x, y, width, height, hovered ? 0xFFAA99FF : 0x66FFFFFF);
    }

    /**
     * Draws a danger button (red theme).
     */
    public static void drawDangerButton(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        if (hovered) {
            context.fill(x, y, x + width, y + height, 0xDDFF4444);
        } else {
            context.fill(x, y, x + width, y + height, 0xBB551111);
        }
        drawBorder(context, x, y, width, height, hovered ? 0xFFFF8888 : 0x66FF4444);
    }

    /**
     * Draws a success button (green theme).
     */
    public static void drawSuccessButton(DrawContext context, int x, int y, int width, int height, boolean hovered) {
        if (hovered) {
            context.fill(x, y, x + width, y + height, 0xDD44CC44);
        } else {
            context.fill(x, y, x + width, y + height, 0xBB115511);
        }
        drawBorder(context, x, y, width, height, hovered ? 0xFF88FF88 : 0x6644CC44);
    }

    /**
     * Draws a thin border around a rectangle.
     */
    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Top
        context.fill(x, y, x + width, y + 1, color);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        context.fill(x, y, x + 1, y + height, color);
        // Right
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * Draws a fullscreen dark overlay.
     */
    public static void drawDarkOverlay(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, 0xDD0D0D1A);
    }

    /**
     * Generates an RGB color that cycles over time.
     * @param timeMs current time in milliseconds
     * @param speed cycle speed multiplier
     * @return ARGB color
     */
    public static int getRainbowColor(long timeMs, float speed) {
        float hue = ((timeMs * speed) % 10000) / 10000.0f;
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
    }

    /**
     * Generates RGB color without java.awt dependency (for Android/Pojav).
     */
    public static int getRainbowColorSafe(long timeMs, float speed) {
        float hue = ((timeMs * speed) % 10000) / 10000.0f;
        return 0xFF000000 | hsbToRgb(hue, 0.8f, 1.0f);
    }

    /**
     * HSB to RGB conversion without java.awt.
     */
    public static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> { r = (int) (brightness * 255 + 0.5f); g = (int) (t * 255 + 0.5f); b = (int) (p * 255 + 0.5f); }
                case 1 -> { r = (int) (q * 255 + 0.5f); g = (int) (brightness * 255 + 0.5f); b = (int) (p * 255 + 0.5f); }
                case 2 -> { r = (int) (p * 255 + 0.5f); g = (int) (brightness * 255 + 0.5f); b = (int) (t * 255 + 0.5f); }
                case 3 -> { r = (int) (p * 255 + 0.5f); g = (int) (q * 255 + 0.5f); b = (int) (brightness * 255 + 0.5f); }
                case 4 -> { r = (int) (t * 255 + 0.5f); g = (int) (p * 255 + 0.5f); b = (int) (brightness * 255 + 0.5f); }
                case 5 -> { r = (int) (brightness * 255 + 0.5f); g = (int) (p * 255 + 0.5f); b = (int) (q * 255 + 0.5f); }
            }
        }
        return (r << 16) | (g << 8) | b;
    }
}
