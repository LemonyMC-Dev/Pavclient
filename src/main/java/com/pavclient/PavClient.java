package com.pavclient;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PavClient - Main mod initializer (server-safe).
 * Client-specific logic is in PavClientMod (ClientModInitializer).
 */
public class PavClient implements ModInitializer {

    public static final String MOD_ID = "pavclient";
    public static final String MOD_NAME = "PavClient";
    public static final String TARGET_SERVER = "oyna.pavmc.com";
    public static final int TARGET_PORT = 25565;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] Mod initialized.", MOD_NAME);
    }
}
