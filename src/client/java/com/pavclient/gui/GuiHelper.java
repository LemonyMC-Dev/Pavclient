package com.pavclient.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Modern GUI rendering utilities for PavClient.
 * Gradient backgrounds, glow effects, smooth modern styling.
 */
public final class GuiHelper {

    private GuiHelper() {}

    // === COLOR PALETTE - Soft / Pastel ===
    public static final int COLOR_BG_DARK = 0xEE0D0D18;
    public static final int COLOR_BG_PANEL = 0xDD141420;
    public static final int COLOR_ACCENT = 0xFF6C63AC;
    public static final int COLOR_ACCENT_LIGHT = 0xFF9E97D4;
    public static final int COLOR_DANGER = 0xFFE57373;
    public static final int COLOR_DANGER_DARK = 0xFFAF4448;
    public static final int COLOR_SUCCESS = 0xFF81C784;
    public static final int COLOR_SUCCESS_DARK = 0xFF519657;
    public static final int COLOR_TEXT = 0xFFE0E0E0;
    public static final int COLOR_TEXT_DIM = 0xFF8E8E8E;
    public static final int COLOR_BORDER = 0x33FFFFFF;

    /**
     * Draws a fullscreen dark gradient background (PavClient branded).
     */
    public static void drawClientBackground(DrawContext context, int width, int height) {
        // Soft dark gradient
        drawVerticalGradient(context, 0, 0, width, height, 0xFF101018, 0xFF08080E);
        // Subtle soft glow at top center
        int glowX = width / 2 - 150;
        drawVerticalGradient(context, glowX, 0, glowX + 300, 60, 0x226C63AC, 0x00000000);
    }

    /**
     * Draws a modern panel with gradient background and accent line.
     */
    public static void drawPanel(DrawContext context, int x, int y, int w, int h) {
        // Soft panel background
        drawVerticalGradient(context, x, y, x + w, y + h, 0xDD161622, 0xDD0E0E18);
        // Subtle top accent line (soft purple, no pink)
        drawHorizontalGradient(context, x, y, x + w, y + 2, 0xFF6C63AC, 0xFF5A5490);
        // Soft border
        drawBorder(context, x, y, w, h, 0x22FFFFFF);
    }

    /**
     * Draws a modern button with gradient, glow on hover, smooth feel.
     */
    public static void drawModernButton(DrawContext context, int x, int y, int w, int h, boolean hovered, boolean active) {
        if (!active) {
            context.fill(x, y, x + w, y + h, 0x44222222);
            drawBorder(context, x, y, w, h, 0x18FFFFFF);
            return;
        }
        if (hovered) {
            // Soft glow
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x226C63AC);
            drawVerticalGradient(context, x, y, x + w, y + h, 0xDD5A5490, 0xDD484280);
            drawBorder(context, x, y, w, h, 0x889E97D4);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC1C1C2E, 0xCC151526);
            drawBorder(context, x, y, w, h, 0x33FFFFFF);
        }
    }

    /**
     * Draws a danger button (red theme) with gradient.
     */
    public static void drawDangerButton(DrawContext context, int x, int y, int w, int h, boolean hovered) {
        if (hovered) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x22E57373);
            drawVerticalGradient(context, x, y, x + w, y + h, 0xDDB05050, 0xDD8B3A3A);
            drawBorder(context, x, y, w, h, 0x88EF9A9A);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC2E1515, 0xCC220E0E);
            drawBorder(context, x, y, w, h, 0x44E57373);
        }
    }

    /**
     * Draws a success button (soft green theme).
     */
    public static void drawSuccessButton(DrawContext context, int x, int y, int w, int h, boolean hovered) {
        if (hovered) {
            context.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x2281C784);
            drawVerticalGradient(context, x, y, x + w, y + h, 0xDD4E8C52, 0xDD3A6E3E);
            drawBorder(context, x, y, w, h, 0x88A5D6A7);
        } else {
            drawVerticalGradient(context, x, y, x + w, y + h, 0xCC15301A, 0xCC0E2212);
            drawBorder(context, x, y, w, h, 0x4481C784);
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
