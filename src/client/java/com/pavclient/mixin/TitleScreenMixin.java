package com.pavclient.mixin;

import com.pavclient.PavClient;
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
 * Mixin that intercepts the Title Screen to automatically connect
 * to oyna.pavmc.com without showing the main menu.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean pavclient$autoConnected = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void pavclient$onInit(CallbackInfo ci) {
        if (pavclient$autoConnected) {
            return;
        }

        pavclient$autoConnected = true;

        MinecraftClient client = MinecraftClient.getInstance();

        PavClient.LOGGER.info("[{}] Bypassing title screen - auto-connecting to {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);

        // Create server info for the target server
        ServerInfo serverInfo = new ServerInfo(
                PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT,
                ServerInfo.ServerType.OTHER
        );

        // Parse the server address
        ServerAddress serverAddress = ServerAddress.parse(
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT
        );

        // Connect to the server, bypassing the title screen entirely
        // Parameters: (parentScreen, client, serverAddress, serverInfo, quickPlay, cookieStorage)
        ConnectScreen.connect(
                (TitleScreen) (Object) this,
                client,
                serverAddress,
                serverInfo,
                false,   // quickPlay
                null     // cookieStorage
        );
    }
}
