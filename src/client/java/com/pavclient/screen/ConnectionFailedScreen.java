package com.pavclient.screen;

import com.pavclient.PavClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Custom connection failed screen for PavClient.
 *
 * Displays only two options when connection to oyna.pavmc.com fails:
 * 1. "Tekrar Dene" (Retry) - Attempts to reconnect to the server
 * 2. "Oyunu Kapat" (Close Game) - Immediately shuts down the game
 *
 * No main menu or other navigation is available.
 */
public class ConnectionFailedScreen extends Screen {

    private final Text disconnectReason;

    public ConnectionFailedScreen(Text reason) {
        super(Text.literal("PavClient - Baglanti Basarisiz"));
        this.disconnectReason = reason;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int centerY = this.height / 2;

        // "Tekrar Dene" (Retry) button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tekrar Dene").formatted(Formatting.GREEN),
                button -> reconnect()
        ).dimensions(centerX, centerY + 10, buttonWidth, buttonHeight).build());

        // "Oyunu Kapat" (Close Game) button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Oyunu Kapat").formatted(Formatting.RED),
                button -> {
                    PavClient.LOGGER.info("[{}] User chose to close the game.", PavClient.MOD_NAME);
                    if (this.client != null) {
                        this.client.scheduleStop();
                    }
                }
        ).dimensions(centerX, centerY + 40, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("PavClient").formatted(Formatting.GOLD, Formatting.BOLD),
                this.width / 2,
                this.height / 2 - 60,
                0xFFFFFF
        );

        // Subtitle - connection failed
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Sunucuya baglanilamadi!").formatted(Formatting.RED),
                this.width / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );

        // Server address
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Sunucu: " + PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT)
                        .formatted(Formatting.GRAY),
                this.width / 2,
                this.height / 2 - 25,
                0xFFFFFF
        );

        // Disconnect reason (if available)
        if (disconnectReason != null) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("Sebep: ").formatted(Formatting.YELLOW)
                            .append(disconnectReason.copy().formatted(Formatting.WHITE)),
                    this.width / 2,
                    this.height / 2 - 8,
                    0xFFFFFF
            );
        }
    }

    /**
     * Attempts to reconnect to the PavMC server.
     */
    private void reconnect() {
        if (this.client == null) return;

        PavClient.LOGGER.info("[{}] Retrying connection to {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);

        ServerInfo serverInfo = new ServerInfo(
                PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT,
                ServerInfo.ServerType.OTHER
        );

        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT
        );

        // Use this screen as the parent (not TitleScreen, to avoid re-triggering auto-connect mixin)
        // Parameters: (parentScreen, client, serverAddress, serverInfo, quickPlay, cookieStorage)
        ConnectScreen.connect(
                this,
                this.client,
                serverAddress,
                serverInfo,
                false,   // quickPlay
                null     // cookieStorage
        );
    }

    /**
     * Prevent closing this screen with ESC key.
     * The user must choose one of the two buttons.
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
