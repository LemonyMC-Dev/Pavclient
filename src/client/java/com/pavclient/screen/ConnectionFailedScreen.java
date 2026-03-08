package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class ConnectionFailedScreen extends Screen {

    private final Text disconnectReason;

    public ConnectionFailedScreen(Text reason) {
        super(Text.literal("PavClient - Ba\u011flant\u0131 Ba\u015far\u0131s\u0131z"));
        this.disconnectReason = reason;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addDrawableChild(ModernButtonWidget.success(
                cx - 110, cy + 18, 220, 28,
                Text.literal("\u21BB Yeniden Ba\u011flan"),
                btn -> reconnect()
        ));

        this.addDrawableChild(ModernButtonWidget.danger(
                cx - 110, cy + 54, 220, 28,
                Text.literal("\u2716 Oyunu Kapat"),
                btn -> { if (this.client != null) this.client.setScreen(new ConfirmQuitScreen(this)); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        int cy = this.height / 2;

        GuiHelper.drawPanel(context, cx - 170, cy - 70, 340, 160);

        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavMC \u25C6"), cx, cy - 55, rgb);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Sunucuya ba\u011flan\u0131lamad\u0131!"), cx, cy - 35, 0xFFFF5252);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(PavClient.TARGET_SERVER), cx, cy - 20, 0xFF9E9E9E);

        if (disconnectReason != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, disconnectReason, cx, cy - 4, 0xFFFFD740);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void reconnect() {
        if (this.client == null) return;
        ServerInfo info = new ServerInfo(PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT, ServerInfo.ServerType.OTHER);
        ServerAddress addr = ServerAddress.parse(PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT);
        // Parent olarak kendimizi ver, TitleScreen degil (sonsuz dongu onlemi)
        ConnectScreen.connect(
                new ConnectionFailedScreen(Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                this.client, addr, info, false, null);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
