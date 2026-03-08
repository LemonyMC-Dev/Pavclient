package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class InstallCompleteScreen extends Screen {

    public InstallCompleteScreen() {
        super(Text.literal("PavClient Kurulum"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.addDrawableChild(ModernButtonWidget.danger(
                cx - 110, cy + 50, 220, 28,
                Text.literal("Oyunu Kapat ve Yeniden Ba\u015flat"),
                btn -> { if (this.client != null) this.client.scheduleStop(); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        int cy = this.height / 2;

        GuiHelper.drawPanel(context, cx - 180, cy - 85, 360, 175);
        context.fill(cx - 150, cy - 54, cx + 150, cy - 10, 0xAA000000);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavClient \u25C6"), cx, cy - 72, 0xFFFFFFFF);

        // Mesajlar - net g\u00f6r\u00fcn\u00fcr renkler
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u2714 Kurulum Ba\u015far\u0131l\u0131!"), cx, cy - 48, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Gerekli modlar ba\u015far\u0131yla indirildi."), cx, cy - 32, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Oyunu yeniden ba\u015flat\u0131n ve tekrar girin."), cx, cy - 18, 0xFFFFFFFF);

        // Badge
        String badge = "[ \u0130ndirme Tamamland\u0131 ]";
        int bw = this.textRenderer.getWidth(badge) + 16;
        context.fill(cx - bw / 2, cy + 4, cx + bw / 2, cy + 18, 0xAA000000);
        GuiHelper.drawBorder(context, cx - bw / 2, cy + 4, bw, 14, 0xAAB388FF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(badge), cx, cy + 7, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION), cx, cy + 30, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
