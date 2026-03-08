package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.PavClientMod;
import com.pavclient.gui.GuiHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class InstallProgressScreen extends Screen {

    public InstallProgressScreen() {
        super(Text.literal("PavClient Kurulum"));
    }

    @Override
    public void tick() {
        if (this.client == null) {
            return;
        }
        if (!PavClientMod.installInProgress) {
            if (PavClientMod.needsRestart) {
                this.client.setScreen(new InstallCompleteScreen());
            } else {
                this.client.setScreen(new net.minecraft.client.gui.screen.TitleScreen());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);

        int cx = this.width / 2;
        int cy = this.height / 2;

        GuiHelper.drawPanel(context, cx - 180, cy - 80, 360, 160);

        long ms = System.nanoTime() / 1_000_000L;
        int rgb = 0xFFFFFFFF;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("PavMC"), cx, cy - 48, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Gerekli modlar kuruluyor..."), cx, cy - 24, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Bu ilk açılışta biraz sürebilir."), cx, cy - 10, 0xFFFFFFFF);

        float progress = ((ms % 1800) / 1800.0f);
        int barW = 180;
        int barX = cx - barW / 2;
        int barY = cy + 12;
        context.fill(barX, barY, barX + barW, barY + 4, 0x33FFFFFF);
        context.fill(barX, barY, barX + (int) (barW * progress), barY + 4, rgb);

        int dots = (int) ((ms / 400) % 4);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("İndiriliyor" + ".".repeat(dots)), cx, cy + 24, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION), cx, cy + 40, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
