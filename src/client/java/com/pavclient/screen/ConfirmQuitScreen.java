package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfirmQuitScreen extends Screen {

    private final Screen parent;

    public ConfirmQuitScreen(Screen parent) {
        super(Text.literal("Onay"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addDrawableChild(ModernButtonWidget.danger(cx - 110, cy + 12, 220, 28,
                Text.literal("Evet, Kapat"),
                btn -> { if (this.client != null) this.client.scheduleStop(); }));

        this.addDrawableChild(ModernButtonWidget.create(cx - 110, cy + 48, 220, 28,
                Text.literal("\u0130ptal"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        int cy = this.height / 2;

        GuiHelper.drawPanel(context, cx - 150, cy - 45, 300, 130);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u26A0"), cx, cy - 35, 0xFFFFD740);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Oyunu kapatmak istedi\u011finize"), cx, cy - 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("emin misiniz?"), cx, cy - 8, 0xFFFF8A80);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
