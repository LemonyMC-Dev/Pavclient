package com.pavclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * A modern styled button widget for PavClient GUI.
 */
public class ModernButtonWidget extends ButtonWidget {

    public enum Style {
        DEFAULT,
        DANGER,
        SUCCESS
    }

    private final Style style;

    public ModernButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Style style) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.style = style;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();

        switch (style) {
            case DANGER -> GuiHelper.drawDangerButton(context, getX(), getY(), getWidth(), getHeight(), hovered);
            case SUCCESS -> GuiHelper.drawSuccessButton(context, getX(), getY(), getWidth(), getHeight(), hovered);
            default -> GuiHelper.drawButtonBackground(context, getX(), getY(), getWidth(), getHeight(), hovered);
        }

        // Draw centered text
        int textColor = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        context.drawCenteredTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                this.getMessage(),
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                textColor
        );
    }

    public static ModernButtonWidget create(int x, int y, int width, int height, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, width, height, text, onPress, Style.DEFAULT);
    }

    public static ModernButtonWidget danger(int x, int y, int width, int height, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, width, height, text, onPress, Style.DANGER);
    }

    public static ModernButtonWidget success(int x, int y, int width, int height, Text text, PressAction onPress) {
        return new ModernButtonWidget(x, y, width, height, text, onPress, Style.SUCCESS);
    }
}
