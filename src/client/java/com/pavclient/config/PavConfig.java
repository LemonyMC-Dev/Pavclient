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
    public boolean blockHighlightRgb = false;
    public int blockHighlightRed = 124;
    public int blockHighlightGreen = 77;
    public int blockHighlightBlue = 255;
    public float blockHighlightAlpha = 0.9f;
    public float blockHighlightSize = 0.002f;
    public boolean blockHighlightFill = false;
    public float blockHighlightFillAlpha = 0.2f;
    public boolean realisticAnimations = false;

    // === Dans/Emote ===
    public int lastEmote = 0;

    // === Mod Ayarları ===
    // Mouse Tweaks
    public boolean mouseTweaksEnabled = true;
    public String mouseTweaksKey = "Yok";

    // AppleSkin
    public boolean appleSkinEnabled = true;
    public String appleSkinKey = "Yok";

    // Chat Heads
    public boolean chatHeadsEnabled = true;
    public String chatHeadsKey = "Yok";

    // 3D Skin Layers
    public boolean skinLayers3dEnabled = true;
    public String skinLayers3dKey = "Yok";

    // Simple Voice Chat
    public boolean voiceChatEnabled = true;
    public String voiceChatKey = "V";

    // Zoomify
    public boolean zoomifyEnabled = true;
    public String zoomifyKey = "C";

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
