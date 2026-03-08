package com.pavclient.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Modern GUI rendering utilities for PavClient.
 * Gradient backgrounds, glow effects, smooth modern styling.
 */
public final class GuiHelper {

    private GuiHelper() {}

    // === COLOR PALETTE ===
    public static final int COLOR_BG_DARK = 0xEE0A0A14;
    public static final int COLOR_BG_PANEL = 0xDD12121F;
    public static final int COLOR_ACCENT = 0xFF7C4DFF;
    public static final int COLOR_ACCENT_LIGHT = 0xFFB388FF;
    public static final int COLOR_DANGER = 0xFFFF5252;
    public static final int COLOR_DANGER_DARK = 0xFFCC0000;
    public static final int COLOR_SUCCESS = 0xFF69F0AE;
    public static final int COLOR_SUCCESS_DARK = 0xFF00C853;
    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_TEXT_DIM = 0xFF9E9E9E;
    public static final int COLOR_BORDER = 0x44FFFFFF;

    /**
     * Draws a fullscreen dark gradient background (PavClient branded).
     */
    public static void drawClientBackground(DrawContext context, int width, int height) {
        // Dark gradient from top to bottom
        drawVerticalGradient(context, 0, 0, width, height, 0xFF0D0D1F, 0xFF06060E);
        // Subtle purple glow at top center
        int glowX = width / 2 - 150;
        drawVerticalGradient(context, glowX, 0, glowX + 300, 80, 0x337C4DFF, 0x00000000);
    }

    /**
     * Draws a modern panel with gradient background and accent line.
     */
    public static void drawPanel(DrawContext context, int x, int y, int w, int h) {
        // Main panel background
        drawVerticalGradient(context, x, y, x + w, y + h, 0xDD161625, 0xDD0E0E1A);
        // Top accent gradient line
        drawHorizontalGradient(context, x, y, x + w, y + 2, 0xFF7C4DFF, 0xFFFF4081);
        // Border
        drawBorder(context, x, y, w, h, 0x33FFFFFF);
    }

    /**
     * Draws a modern button with gradient, glow on hover, smooth feel.
     */
    public static void drawModernButton(DrawContext context, int x, int y, int w, int h, boolean hovered, boolean active) {
        if (!active) {
            context.fill(x, y, x + w, y + h, 0x44333333);
            drawBorder(context, x, y, w, h, 0x22FFFFFF);
            return;
        }
        if (hovered) {
            // Glow effect - outer glow
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x337C4DFF);
            // Bright gradient
            drawVerticalGradient(context, x, y, x + w, y + h, 0xEE7C4DFF, 0xEE5E35CC);
            drawBorder(context, x, y, w, h, 0xAAB388FF);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC1E1E32, 0xCC16162A);
            drawBorder(context, x, y, w, h, 0x55FFFFFF);
        }
    }

    /**
     * Draws a danger button (red theme) with gradient.
     */
    public static void drawDangerButton(DrawContext context, int x, int y, int w, int h, boolean hovered) {
        if (hovered) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x33FF5252);
            drawVerticalGradient(context, x, y, x + w, y + h, 0xEEFF5252, 0xEECC0000);
            drawBorder(context, x, y, w, h, 0xAAFF8A80);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC3D1111, 0xCC2A0A0A);
            drawBorder(context, x, y, w, h, 0x66FF5252);
        }
    }

    /**
     * Draws a success button (green theme) with gradient.
     */
    public static void drawSuccessButton(DrawContext context, int x, int y, int w, int h, boolean hovered) {
        if (hovered) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x3369F0AE);
            drawVerticalGradient(context, x, y, x + w, y + h, 0xEE69F0AE, 0xEE00C853);
            drawBorder(context, x, y, w, h, 0xAAB9F6CA);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC113D22, 0xCC0A2A16);
            drawBorder(context, x, y, w, h, 0x6669F0AE);
        }
    }

    /**
     * Draws a vertical gradient fill.
     */
    public static void drawVerticalGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        context.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    /**
     * Draws a horizontal gradient fill (simulated with thin vertical strips).
     */
    public static void drawHorizontalGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        int w = x2 - x1;
        if (w <= 0) return;
        int steps = Math.min(w, 32);
        int stripW = Math.max(w / steps, 1);

        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int color = lerpColor(colorLeft, colorRight, t);
            int sx = x1 + (int) (w * t);
            int ex = Math.min(sx + stripW + 1, x2);
            context.fill(sx, y1, ex, y2, color);
        }
    }

    /**
     * Draws a thin border.
     */
    public static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    /**
     * Fullscreen dark overlay.
     */
    public static void drawDarkOverlay(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, COLOR_BG_DARK);
    }

    /**
     * Rainbow color (safe for Android/Pojav, no java.awt).
     */
    public static int getRainbowColor(long timeMs, float speed) {
        float hue = ((timeMs * speed) % 10000) / 10000.0f;
        return 0xFF000000 | hsbToRgb(hue, 0.85f, 1.0f);
    }

    /**
     * HSB to RGB without java.awt.
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
                case 0 -> { r = (int)(brightness*255+.5f); g = (int)(t*255+.5f); b = (int)(p*255+.5f); }
                case 1 -> { r = (int)(q*255+.5f); g = (int)(brightness*255+.5f); b = (int)(p*255+.5f); }
                case 2 -> { r = (int)(p*255+.5f); g = (int)(brightness*255+.5f); b = (int)(t*255+.5f); }
                case 3 -> { r = (int)(p*255+.5f); g = (int)(q*255+.5f); b = (int)(brightness*255+.5f); }
                case 4 -> { r = (int)(t*255+.5f); g = (int)(p*255+.5f); b = (int)(brightness*255+.5f); }
                case 5 -> { r = (int)(brightness*255+.5f); g = (int)(p*255+.5f); b = (int)(q*255+.5f); }
            }
        }
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Linearly interpolate between two ARGB colors.
     */
    public static int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
