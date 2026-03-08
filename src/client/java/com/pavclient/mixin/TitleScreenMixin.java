package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.PavClientMod;
import com.pavclient.screen.ConnectionFailedScreen;
import com.pavclient.screen.InstallCompleteScreen;
import com.pavclient.screen.InstallProgressScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
 * Mixin: Ilk acilista Title Screen'i bypass eder ve auto-connect yapar.
 * Splash overlay tamamen kapandiktan sonra connect baslatir.
 * Baglanti basarisiz olursa ConnectionFailedScreen gosterilir (sonsuz dongu yok).
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$firstLaunchHandled = false;

    @Unique
    private static boolean pavclient$waitingForSplash = true;

    @Unique
    private static boolean pavclient$connectStarted = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Ilk acilis
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

            // Splash hala aktifse, bekle - tick/render'da kontrol edilecek
            if (SplashOverlayMixin.pavclient$splashActive) {
                PavClient.LOGGER.info("[{}] Waiting for splash to finish before auto-connect...", PavClient.MOD_NAME);
                pavclient$waitingForSplash = true;
                pavclient$connectStarted = false;
                // TitleScreen'i iptal ETME - splash overlay onun ustunde ciziliyor
                // Normal init devam etsin ama render'da biz kendi arka planımızı cizecegiz
                return;
            }

            // Splash bitmis, direkt connect
            PavClient.LOGGER.info("[{}] Bypassing title screen -> connecting to {}:{}",
                    PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
            pavclient$autoConnect(client);
            ci.cancel();
            return;
        }

        // Sonraki TitleScreen acilislari: disconnect/fail sonrasi
        PavClient.LOGGER.info("[{}] TitleScreen reached after disconnect. Showing reconnect screen.", PavClient.MOD_NAME);
        client.setScreen(new ConnectionFailedScreen(
                Text.literal("Sunucu ba\u011flant\u0131s\u0131 kesildi.")));
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void pavclient$onTick(CallbackInfo ci) {
        // Splash bitmesini bekliyorsak, her tick'te kontrol et
        if (pavclient$waitingForSplash && !pavclient$connectStarted) {
            MinecraftClient client = MinecraftClient.getInstance();
            // Overlay null ise veya flag false ise splash bitmis demektir
            if (client.getOverlay() == null || !SplashOverlayMixin.pavclient$splashActive) {
                pavclient$waitingForSplash = false;
                pavclient$connectStarted = true;

                // Mod indirme kontrolu (splash sirasinda bitmis olabilir)
                if (PavClientMod.installInProgress) {
                    client.setScreen(new InstallProgressScreen());
                    return;
                }
                if (PavClientMod.needsRestart) {
                    client.setScreen(new InstallCompleteScreen());
                    return;
                }

                PavClient.LOGGER.info("[{}] Splash done. Auto-connecting to {}:{}",
                        PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
                pavclient$autoConnect(client);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void pavclient$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Splash bitmesini beklerken TitleScreen'in vanilla icerigini gizle
        // Splash overlay zaten ustune PavMC cizecek
        if (pavclient$waitingForSplash && !pavclient$connectStarted) {
            // Koyu arka plan ciz, vanilla title screen gozukmesin
            MinecraftClient mc = MinecraftClient.getInstance();
            int w = mc.getWindow().getScaledWidth();
            int h = mc.getWindow().getScaledHeight();
            context.fill(0, 0, w, h, 0xFF0A0A14);
            ci.cancel();
        }
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
        ConnectScreen.connect(
                new ConnectionFailedScreen(
                        Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                client,
                serverAddress,
                serverInfo,
                false,
                null
        );
    }
}
