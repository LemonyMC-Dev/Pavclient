package com.pavclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Modern styled button for PavClient.
 * Features gradient background, glow on hover, smooth animations.
 */
public class ModernButtonWidget extends ButtonWidget {

    public enum Style { DEFAULT, DANGER, SUCCESS }

    private final Style style;
    private float hoverAnimation = 0f; // 0.0 to 1.0

    public ModernButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Style style) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.style = style;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Smooth hover animation
        float target = this.isHovered() ? 1.0f : 0.0f;
        hoverAnimation += (target - hoverAnimation) * 0.3f;
        boolean hovered = hoverAnimation > 0.3f;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();

        switch (style) {
            case DANGER -> GuiHelper.drawDangerButton(context, x, y, w, h, hovered);
            case SUCCESS -> GuiHelper.drawSuccessButton(context, x, y, w, h, hovered);
            default -> GuiHelper.drawModernButton(context, x, y, w, h, hovered, this.active);
        }

        // Text with shadow
        int textColor = this.active ? (hovered ? 0xFFFFFFFF : 0xFFE0E0E0) : 0xFF666666;
        MinecraftClient mc = MinecraftClient.getInstance();
        context.drawCenteredTextWithShadow(
                mc.textRenderer,
                this.getMessage(),
                x + w / 2,
                y + (h - 8) / 2,
                textColor
        );
    }

    // === Factory methods ===

    public static ModernButtonWidget create(int x, int y, int w, int h, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, w, h, text, onPress, Style.DEFAULT);
    }

    public static ModernButtonWidget danger(int x, int y, int w, int h, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, w, h, text, onPress, Style.DANGER);
    }

    public static ModernButtonWidget success(int x, int y, int w, int h, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, w, h, text, onPress, Style.SUCCESS);
    }
}
