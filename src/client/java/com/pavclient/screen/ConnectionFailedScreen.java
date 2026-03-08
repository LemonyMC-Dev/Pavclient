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

/**
 * Custom connection failed screen for PavClient.
 * Only two options:
 * 1. "Yeniden Baglan" (Retry) - green button
 * 2. "Oyunu Kapat" (Close Game) - red button, with confirmation
 */
public class ConnectionFailedScreen extends Screen {

    private final Text disconnectReason;

    public ConnectionFailedScreen(Text reason) {
        super(Text.literal("PavClient - Baglanti Basarisiz"));
        this.disconnectReason = reason;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // "Yeniden Baglan" (Retry) - success green
        this.addDrawableChild(ModernButtonWidget.success(
                centerX - 100, centerY + 15, 200, 25,
                Text.literal("Yeniden Baglan"),
                button -> reconnect()
        ));

        // "Oyunu Kapat" (Close) - danger red with confirmation
        this.addDrawableChild(ModernButtonWidget.danger(
                centerX - 100, centerY + 48, 200, 25,
                Text.literal("Oyunu Kapat"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new ConfirmQuitScreen(this));
                    }
                }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawDarkOverlay(context, this.width, this.height);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        GuiHelper.drawPanel(context, centerX - 160, centerY - 65, 320, 150);

        long time = System.currentTimeMillis();
        int rgbColor = GuiHelper.getRainbowColorSafe(time, 1.5f);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("PavClient"), centerX, centerY - 50, rgbColor);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Sunucuya baglanilamadi!"), centerX, centerY - 32, 0xFFFF4444);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Sunucu: " + PavClient.TARGET_SERVER), centerX, centerY - 18, 0xFFAAAAAA);

        if (disconnectReason != null) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    disconnectReason, centerX, centerY - 2, 0xFFFFAA00);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void reconnect() {
        if (this.client == null) return;

        ServerInfo serverInfo = new ServerInfo(PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT, ServerInfo.ServerType.OTHER);
        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT);

        ConnectScreen.connect(this, this.client, serverAddress, serverInfo, false, null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
