package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.PavClientMod;
import com.pavclient.screen.ConnectionFailedScreen;
import com.pavclient.screen.InstallCompleteScreen;
import com.pavclient.screen.InstallProgressScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TitleScreen init'te:
 * - Ilk acilista mod kurulumu kontrol et, sonra auto-connect.
 * - Disconnect sonrasi ConnectionFailedScreen goster.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$firstLaunchHandled = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!pavclient$firstLaunchHandled) {
            pavclient$firstLaunchHandled = true;

            // Mod indiriliyor -> progress ekrani
            if (PavClientMod.installInProgress) {
                client.setScreen(new InstallProgressScreen());
                ci.cancel();
                return;
            }

            // Yeni mod indirildi -> restart ekrani
            if (PavClientMod.needsRestart) {
                PavClient.LOGGER.info("[{}] Mods installed. Restart required.", PavClient.MOD_NAME);
                client.setScreen(new InstallCompleteScreen());
                ci.cancel();
                return;
            }

            // Auto-connect
            PavClient.LOGGER.info("[{}] Connecting to {}:{}", PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
            pavclient$autoConnect(client);
            ci.cancel();
            return;
        }

        // Disconnect sonrasi tekrar TitleScreen acildi -> reconnect ekrani
        PavClient.LOGGER.info("[{}] Disconnected. Showing reconnect screen.", PavClient.MOD_NAME);
        client.setScreen(new ConnectionFailedScreen(Text.literal("Sunucu ba\u011flant\u0131s\u0131 kesildi.")));
        ci.cancel();
    }

    @Unique
    private static void pavclient$autoConnect(MinecraftClient client) {
        ServerInfo serverInfo = new ServerInfo(
                PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT,
                ServerInfo.ServerType.OTHER
        );
        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT
        );
        ConnectScreen.connect(
                new ConnectionFailedScreen(Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                client, serverAddress, serverInfo, false, null
        );
    }
}
