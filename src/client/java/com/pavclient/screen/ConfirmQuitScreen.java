package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Confirmation dialog: "Oyunu kapatmak istediginize emin misiniz?"
 * - Evet, Kapat (red)
 * - Iptal (default)
 */
public class ConfirmQuitScreen extends Screen {

    private final Screen parent;

    public ConfirmQuitScreen(Screen parent) {
        super(Text.literal("Onay"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // "Evet, Kapat"
        this.addDrawableChild(ModernButtonWidget.danger(
                centerX - 100, centerY + 10, 200, 25,
                Text.literal("Evet, Kapat"),
                button -> {
                    if (this.client != null) {
                        this.client.scheduleStop();
                    }
                }
        ));

        // "Iptal"
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - 100, centerY + 42, 200, 25,
                Text.literal("Iptal"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawDarkOverlay(context, this.width, this.height);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Small panel
        GuiHelper.drawPanel(context, centerX - 140, centerY - 40, 280, 120);

        // Warning icon / text
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Oyunu kapatmak istediginize"),
                centerX, centerY - 25,
                0xFFFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("emin misiniz?"),
                centerX, centerY - 12,
                0xFFFFAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
