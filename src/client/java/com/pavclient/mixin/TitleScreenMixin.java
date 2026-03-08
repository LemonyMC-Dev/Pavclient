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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin: Ilk acilista Title Screen'i bypass eder ve auto-connect yapar.
 * Baglanti basarisiz olursa ConnectionFailedScreen gosterilir (sonsuz dongu yok).
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$firstLaunchHandled = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Ilk acilis: mod kurulumu kontrol et, sonra auto-connect
        if (!pavclient$firstLaunchHandled) {
            pavclient$firstLaunchHandled = true;

            // Hala mod indiriliyor
            if (PavClientMod.installInProgress) {
                client.setScreen(new InstallProgressScreen());
                ci.cancel();
                return;
            }

            // Yeni mod indirildi, restart gerekli
            if (PavClientMod.needsRestart) {
                PavClient.LOGGER.info("[{}] New mods installed. Showing install complete screen.", PavClient.MOD_NAME);
                client.setScreen(new InstallCompleteScreen());
                ci.cancel();
                return;
            }

            // Ilk auto-connect
            PavClient.LOGGER.info("[{}] Bypassing title screen -> connecting to {}:{}",
                    PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
            pavclient$autoConnect(client);
            ci.cancel();
            return;
        }

        // Sonraki TitleScreen acilislari: auto-connect yapma, ConnectionFailedScreen goster.
        // Bu noktaya disconnect/fail sonrasi gelinir. Sonsuz donguyu onlemek icin
        // direkt ConnectionFailedScreen'e yonlendir.
        PavClient.LOGGER.info("[{}] TitleScreen reached after disconnect. Showing reconnect screen.", PavClient.MOD_NAME);
        client.setScreen(new ConnectionFailedScreen(
                net.minecraft.text.Text.literal("Sunucu ba\u011flant\u0131s\u0131 kesildi.")));
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

        // Parent olarak ConnectionFailedScreen ver, TitleScreen degil
        // Boylece fail durumunda TitleScreen.init dongusu tetiklenmez
        ConnectScreen.connect(
                new ConnectionFailedScreen(
                        net.minecraft.text.Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                client,
                serverAddress,
                serverInfo,
                false,
                null
        );
    }
}
