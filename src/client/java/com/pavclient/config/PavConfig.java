package com.pavclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pavclient.PavClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PavConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pavclient.json");
    private static PavConfig INSTANCE;

    // === HUD: RGB Yazi ===
    public boolean rgbTextEnabled = true;
    public float rgbScale = 2.0f;
    public int rgbX = 6;
    public int rgbY = 22;

    // === HUD: Zirh ===
    public boolean armorHudEnabled = true;
    public float armorHudScale = 1.0f;
    public int armorHudX = 5;
    public int armorHudY = 5;
    public boolean armorHudAnchorBottom = true;

    // === Crosshair ===
    public boolean customCrosshairEnabled = false;
    public int crosshairStyle = 0;

    // === Gorunum ===
    public boolean showOwnName = false;
    public boolean blockHighlight = true;
    public boolean realisticAnimations = false;

    // === Dans/Emote ===
    public int lastEmote = 0;

    private PavConfig() {}

    public static PavConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                INSTANCE = GSON.fromJson(Files.readString(CONFIG_PATH), PavConfig.class);
                if (INSTANCE == null) INSTANCE = new PavConfig();
            } catch (Exception e) {
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
        } catch (IOException e) {
            PavClient.LOGGER.error("Config save failed", e);
        }
    }
}
