package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.screen.ClientSettingsScreen;
import com.pavclient.screen.ConfirmQuitScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin: Modifies the ESC (Game Menu) screen.
 * Removes:
 * - "Disconnect" / "Baglantıyı Kes" button
 * - "Open to LAN" / "LAN" buttons
 * - "Advancements" / "Biz Kimiz" unnecessary buttons
 *
 * Adds:
 * - "Yeniden Baglan" (reconnect - green)
 * - "Oyunu Kapat" (quit game - red, with confirmation)
 * - "Client Ayarlari" (client settings - modern)
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$modifyGameMenu(CallbackInfo ci) {
        // Collect widgets to remove
        List<ClickableWidget> toRemove = new ArrayList<>();

        for (var child : this.children()) {
            if (child instanceof ButtonWidget btn) {
                String msg = btn.getMessage().getString().toLowerCase();

                // Remove disconnect, LAN, advancements, stats buttons
                if (msg.contains("disconnect") || msg.contains("baglanti")
                        || msg.contains("lan") || msg.contains("aga ac")
                        || msg.contains("advancements") || msg.contains("gelismeler")
                        || msg.contains("stats") || msg.contains("istatistik")
                        || msg.contains("player reporting") || msg.contains("oyuncu raporla")) {
                    toRemove.add(btn);
                }
            }
        }

        // Remove the unwanted buttons
        for (ClickableWidget w : toRemove) {
            this.remove(w);
        }

        // Add our custom buttons at the bottom
        int centerX = this.width / 2;
        int bottomY = this.height - 85;

        // "Client Ayarlari" button
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - 100, bottomY, 200, 22,
                Text.literal("Client Ayarlari"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new ClientSettingsScreen(this));
                    }
                }
        ));

        // "Yeniden Baglan" button (green)
        this.addDrawableChild(ModernButtonWidget.success(
                centerX - 100, bottomY + 26, 200, 22,
                Text.literal("Yeniden Baglan"),
                button -> pavclient$reconnect()
        ));

        // "Oyunu Kapat" button (red, with confirmation)
        this.addDrawableChild(ModernButtonWidget.danger(
                centerX - 100, bottomY + 52, 200, 22,
                Text.literal("Oyunu Kapat"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new ConfirmQuitScreen(this));
                    }
                }
        ));
    }

    private void pavclient$reconnect() {
        if (this.client == null) return;

        // Disconnect from current server first
        this.client.world.disconnect();
        this.client.disconnect();

        PavClient.LOGGER.info("[{}] Reconnecting to {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);

        ServerInfo serverInfo = new ServerInfo(PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT, ServerInfo.ServerType.OTHER);
        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT);

        ConnectScreen.connect(this, this.client, serverAddress, serverInfo, false, null);
    }
}
