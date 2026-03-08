package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Shown after first-time mod installation.
 * Modern branded PavMC screen.
 */
public class InstallCompleteScreen extends Screen {

    public InstallCompleteScreen() {
        super(Text.literal("PavClient - Kurulum Ba\u015far\u0131l\u0131"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addDrawableChild(ModernButtonWidget.danger(
                cx - 110, cy + 45, 220, 28,
                Text.literal("Oyunu Kapat"),
                btn -> { if (this.client != null) this.client.scheduleStop(); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Custom PavMC branded background
        GuiHelper.drawClientBackground(context, this.width, this.height);

        int cx = this.width / 2;
        int cy = this.height / 2;

        // Panel
        GuiHelper.drawPanel(context, cx - 170, cy - 80, 340, 160);

        // Rainbow title
        long time = System.currentTimeMillis();
        int rgb = GuiHelper.getRainbowColor(time, 1.5f);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavClient \u25C6"), cx, cy - 65, rgb);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Gerekli modlar ba\u015far\u0131yla indirildi!"), cx, cy - 38, 0xFF69F0AE);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Oyunu yeniden ba\u015flat\u0131n"), cx, cy - 22, 0xFFE0E0E0);

        // Badge
        int badgeW = this.textRenderer.getWidth("[ Kurulum Ba\u015far\u0131l\u0131 ]") + 12;
        GuiHelper.drawBorder(context, cx - badgeW / 2, cy - 2, badgeW, 14, 0xFF7C4DFF);
        context.fill(cx - badgeW / 2, cy - 2, cx + badgeW / 2, cy + 12, 0x447C4DFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("[ Kurulum Ba\u015far\u0131l\u0131 ]"), cx, cy + 1, 0xFFB388FF);

        // Version
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION), cx, cy + 25, 0x66FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
