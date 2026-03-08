package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.screen.ConnectionFailedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that intercepts the Disconnected Screen to replace it with
 * PavClient's custom ConnectionFailedScreen.
 *
 * Instead of showing the default "Disconnected" screen with a "Back to Menu" button,
 * this shows only two options: "Tekrar Dene" (Retry) and "Oyunu Kapat" (Close Game).
 */
@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$replaceDisconnectedScreen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = (Screen) (Object) this;

        // Get the disconnect reason from the screen title
        Text reason = currentScreen.getTitle();

        PavClient.LOGGER.info("[{}] Connection lost/failed. Showing PavClient reconnect screen.",
                PavClient.MOD_NAME);

        // Replace with our custom screen
        client.setScreen(new ConnectionFailedScreen(reason));

        // Cancel the original init to prevent the default screen from rendering
        ci.cancel();
    }
}
