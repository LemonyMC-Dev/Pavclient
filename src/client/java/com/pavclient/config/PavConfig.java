package com.pavclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pavclient.PavClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PavClient configuration system.
 * Saves/loads settings to a JSON file.
 */
public class PavConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pavclient.json");

    private static PavConfig INSTANCE;

    // === Settings ===
    public boolean rgbTextEnabled = true;
    public boolean armorHudEnabled = true;
    public boolean customCrosshairEnabled = false;
    public int crosshairStyle = 0; // 0=default, 1=dot, 2=circle, 3=cross
    public boolean optimizationEnabled = true;

    // Armor HUD position
    public int armorHudX = 5;
    public int armorHudY = 5;
    public boolean armorHudAnchorBottom = true; // true=bottom-left, false=top-left

    // RGB text speed
    public float rgbSpeed = 2.0f;

    private PavConfig() {}

    public static PavConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, PavConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new PavConfig();
                }
                PavClient.LOGGER.info("[{}] Config loaded.", PavClient.MOD_NAME);
            } catch (Exception e) {
                PavClient.LOGGER.error("[{}] Failed to load config, using defaults.", PavClient.MOD_NAME, e);
                INSTANCE = new PavConfig();
            }
        } else {
            INSTANCE = new PavConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(get()));
            PavClient.LOGGER.info("[{}] Config saved.", PavClient.MOD_NAME);
        } catch (IOException e) {
            PavClient.LOGGER.error("[{}] Failed to save config.", PavClient.MOD_NAME, e);
        }
    }
}
