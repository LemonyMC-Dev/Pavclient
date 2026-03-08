package com.pavclient;

import com.pavclient.config.PavConfig;
import com.pavclient.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
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

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] v{} Client initializing...", PavClient.MOD_NAME, PavClient.CLIENT_VERSION);

        // Load config
        PavConfig.load();

        // Download required mods
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        ModDownloader downloader = new ModDownloader(modsDir);
        boolean newMods = downloader.downloadAllMods();

        if (newMods) {
            LOGGER.info("[{}] New mods downloaded! Restart required.", PavClient.MOD_NAME);
            needsRestart = true;
        }

        // Register HUD renderer (RGB text, armor HUD, custom crosshair)
        HudRenderCallback.EVENT.register(new HudRenderer());

        LOGGER.info("[{}] Client initialization complete. Target: {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
    }
}
