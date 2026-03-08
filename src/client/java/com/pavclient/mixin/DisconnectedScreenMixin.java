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
 * Mixin: Replaces the default Disconnected Screen with PavClient's
 * custom ConnectionFailedScreen (Yeniden Baglan / Oyunu Kapat).
 */
@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void pavclient$replaceDisconnectedScreen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = (Screen) (Object) this;

        Text reason = currentScreen.getTitle();

        PavClient.LOGGER.info("[{}] Connection lost. Showing PavClient reconnect screen.", PavClient.MOD_NAME);

        client.setScreen(new ConnectionFailedScreen(reason));
        ci.cancel();
    }
}
