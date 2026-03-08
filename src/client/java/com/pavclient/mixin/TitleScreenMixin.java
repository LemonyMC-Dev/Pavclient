package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.PavClientMod;
import com.pavclient.SplashState;
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
 * Ilk acilista splash bitmesini bekler, mod kurulumu kontrol eder,
 * sonra auto-connect yapar. Fail durumunda ConnectionFailedScreen gosterir.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$firstLaunchHandled = false;

    @Unique
    private static boolean pavclient$waitingForSplash = false;

    @Unique
    private static boolean pavclient$connectStarted = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!pavclient$firstLaunchHandled) {
            pavclient$firstLaunchHandled = true;

            // Mod indirme devam ediyorsa progress ekrani goster
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

            // Splash hala aktifse bekle
            if (SplashState.splashActive || client.getOverlay() != null) {
                PavClient.LOGGER.info("[{}] Waiting for splash to finish...", PavClient.MOD_NAME);
                pavclient$waitingForSplash = true;
                pavclient$connectStarted = false;
                return;
            }

            // Splash bitmis, direkt connect
            PavClient.LOGGER.info("[{}] Connecting to {}:{}", PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
            pavclient$autoConnect(client);
            ci.cancel();
            return;
        }

        // Sonraki TitleScreen acilislari = disconnect sonrasi
        PavClient.LOGGER.info("[{}] TitleScreen after disconnect. Showing reconnect screen.", PavClient.MOD_NAME);
        client.setScreen(new ConnectionFailedScreen(Text.literal("Sunucu ba\u011flant\u0131s\u0131 kesildi.")));
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void pavclient$onTick(CallbackInfo ci) {
        if (pavclient$waitingForSplash && !pavclient$connectStarted) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.getOverlay() == null && !SplashState.splashActive) {
                pavclient$waitingForSplash = false;
                pavclient$connectStarted = true;

                if (PavClientMod.installInProgress) {
                    client.setScreen(new InstallProgressScreen());
                    return;
                }
                if (PavClientMod.needsRestart) {
                    client.setScreen(new InstallCompleteScreen());
                    return;
                }

                PavClient.LOGGER.info("[{}] Splash done. Connecting to {}:{}", PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
                pavclient$autoConnect(client);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void pavclient$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (pavclient$waitingForSplash && !pavclient$connectStarted) {
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
        ConnectScreen.connect(
                new ConnectionFailedScreen(Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                client, serverAddress, serverInfo, false, null
        );
    }
}
