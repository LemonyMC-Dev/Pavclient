package com.pavclient;

import com.pavclient.config.PavConfig;
import com.pavclient.emote.EmoteManager;
import com.pavclient.hud.HudRenderer;
import com.pavclient.network.PavClientChannel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
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

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] v{} Client initializing...", PavClient.MOD_NAME, PavClient.CLIENT_VERSION);

        // Load config
        PavConfig.load();

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

        // Emote/dans sistemi
        EmoteManager.init();

        // Register HUD renderer (RGB text, armor HUD, custom crosshair, emote indicator)
        HudRenderCallback.EVENT.register(new HudRenderer());

        // Discord RPC config olustur (Simple Discord RPC modu icin)
        setupDiscordRpc();

        LOGGER.info("[{}] Client initialization complete. Target: {}:{}",
                PavClient.MOD_NAME, PavClient.TARGET_SERVER, PavClient.TARGET_PORT);
    }

    /**
     * Simple Discord RPC modunun config dosyasini olusturur.
     * oyna.pavmc.com olarak ayarlar.
     */
    private void setupDiscordRpc() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path rpcConfig = configDir.resolve("simple-discord-rpc.toml");

        // Sadece yoksa olustur (kullanici degistirmis olabilir)
        if (!Files.exists(rpcConfig)) {
            String config = """
                    [general]
                    applicationID = 1103952058914476133
                    
                    [text]
                    line1 = "PavClient v%s"
                    line2 = "oyna.pavmc.com"
                    
                    [image]
                    largeImageKey = "minecraft"
                    largeImageText = "PavClient"
                    smallImageKey = ""
                    smallImageText = ""
                    
                    [buttons]
                    enabled = true
                    label1 = "PavMC Sunucusu"
                    url1 = "https://discord.gg/pavmc"
                    label2 = ""
                    url2 = ""
                    """.formatted(PavClient.CLIENT_VERSION);
            try {
                Files.createDirectories(configDir);
                Files.writeString(rpcConfig, config);
                LOGGER.info("[{}] Discord RPC config created", PavClient.MOD_NAME);
            } catch (IOException e) {
                LOGGER.warn("[{}] Failed to create Discord RPC config", PavClient.MOD_NAME, e);
            }
        }
    }
}
