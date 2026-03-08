package com.pavclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * PavClient - Client-side mod initializer.
 * Handles automatic mod downloading on startup.
 * Auto-connect and connection failure screens are handled via Mixins.
 */
public class PavClientMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Client initializing...", PavClient.MOD_NAME);

        // Get the mods directory
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");

        // Download required mods automatically
        ModDownloader downloader = new ModDownloader(modsDir);
        downloader.downloadAllMods();

        LOGGER.info("[{}] Client initialization complete. Target server: {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
    }
}
