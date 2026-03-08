package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.PavClientMod;
import com.pavclient.screen.InstallCompleteScreen;
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
 * Mixin: Completely bypasses the Title Screen.
 * - If new mods were just downloaded -> show InstallCompleteScreen
 * - Otherwise -> auto-connect to oyna.pavmc.com
 * Title screen is NEVER shown to the user.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$handled = false;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$onInit(CallbackInfo ci) {
        if (pavclient$handled) {
            // If we get here again (e.g. from disconnect), just auto-connect
            pavclient$autoConnect();
            ci.cancel();
            return;
        }

        pavclient$handled = true;
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if restart is needed (new mods downloaded)
        if (PavClientMod.needsRestart) {
            PavClient.LOGGER.info("[{}] New mods installed. Showing install complete screen.", PavClient.MOD_NAME);
            client.setScreen(new InstallCompleteScreen());
            ci.cancel();
            return;
        }

        // Auto-connect to PavMC
        PavClient.LOGGER.info("[{}] Bypassing title screen -> connecting to {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);

        pavclient$autoConnect();
        ci.cancel();
    }

    @Unique
    private static void pavclient$autoConnect() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo serverInfo = new ServerInfo(
                PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT,
                ServerInfo.ServerType.OTHER
        );

        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT
        );

        ConnectScreen.connect(
                new TitleScreen(),
                client,
                serverAddress,
                serverInfo,
                false,
                null
        );
    }
}
