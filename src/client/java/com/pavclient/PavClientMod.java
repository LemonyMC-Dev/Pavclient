package com.pavclient;

import com.pavclient.config.PavConfig;
import com.pavclient.emote.EmoteManager;
import com.pavclient.hud.HudRenderer;
import com.pavclient.network.FriendsChannel;
import com.pavclient.network.PavClientChannel;
import com.pavclient.screen.FriendsMenuScreen;
import com.pavclient.social.FriendManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * PavClient - Client-side mod initializer.
 * Handles mod downloading, config loading, and HUD registration.
 */
public class PavClientMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME);

    /** Flag set to true if new mods were downloaded and restart is needed */
    public static boolean needsRestart = false;
    public static volatile boolean installInProgress = false;
    public static volatile boolean installFinished = false;
    private static KeyBinding openFriendsKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] v{} Client initializing...", PavClient.MOD_NAME, PavClient.CLIENT_VERSION);

        // Load config
        PavConfig.load();
        FriendManager.load();

        // Download required mods without blocking startup.
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        ModDownloader downloader = new ModDownloader(modsDir);

        if (downloader.hasRequiredModsLocally()) {
            LOGGER.info("[{}] Required mods already present locally. Skipping startup download.", PavClient.MOD_NAME);
            installFinished = true;
        } else {
            installInProgress = true;
            Thread installerThread = Thread.ofVirtual().name("pavclient-mod-installer").start(() -> {
                try {
                    boolean newMods = downloader.downloadAllMods();
                    if (newMods) {
                        LOGGER.info("[{}] New mods downloaded. Restart required.", PavClient.MOD_NAME);
                        needsRestart = true;
                    }
                } finally {
                    installInProgress = false;
                    installFinished = true;
                }
            });
            LOGGER.info("[{}] Started background installer thread: {}", PavClient.MOD_NAME, installerThread.getName());
        }

        // PavClient network channel (PM | tag tespiti)
        PavClientChannel.register();
        FriendsChannel.register();

        // Friends menu keybind (H)
        openFriendsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pavclient.friends_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.pavclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openFriendsKey.wasPressed()) {
                client.setScreen(new FriendsMenuScreen(client.currentScreen));
            }
        });

        // Emote/dans sistemi
        EmoteManager.init();

        // Register HUD renderer (RGB text, armor HUD, custom crosshair, emote indicator)
        HudRenderCallback.EVENT.register(new HudRenderer());

        LOGGER.info("[{}] Client initialization complete. Target: {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
    }
}
