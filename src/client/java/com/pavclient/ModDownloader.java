package com.pavclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automatic mod downloader - gizli klasor sistemi.
 * Modlar mods/.pavclient/ altina random isimle indirilir.
 * Fabric Loader mods/ alt klasorlerini otomatik tarar.
 *
 * Kullanici goremez: dot-prefix gizli + random isimler.
 */
public class ModDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME + "/ModDownloader");

    private static final String MODRINTH_API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "LemonyMC-Dev/PavClient/2.0.0 (pavclient)";
    private static final String GAME_VERSION = "1.21.4";
    private static final String LOADER = "fabric";

    /** Gizli klasor adi - mods/.pavclient/ */
    private static final String HIDDEN_DIR = ".pavclient";

    private static final List<ModEntry> REQUIRED_MODS = List.of(
            new ModEntry("viafabricplus", "ViaFabricPlus"),
            new ModEntry("lithium", "Lithium"),
            new ModEntry("ferrite-core", "FerriteCore"),
            new ModEntry("modmenu", "Mod Menu"),
            new ModEntry("cloth-config", "Cloth Config"),
            new ModEntry("mouse-tweaks", "Mouse Tweaks"),
            new ModEntry("appleskin", "AppleSkin"),
            new ModEntry("chat-heads", "Chat Heads"),
            new ModEntry("3dskinlayers", "3D Skin Layers"),
            new ModEntry("simple-voice-chat", "Simple Voice Chat"),
            new ModEntry("fabric-language-kotlin", "Fabric Language Kotlin"),
            new ModEntry("yacl", "YetAnotherConfigLib"),
            new ModEntry("zoomify", "Zoomify"),
            new ModEntry("not-enough-animations", "Not Enough Animations"),
            new ModEntry("entityculling", "Entity Culling"),
            new ModEntry("c2me-fabric", "C2ME"),
            new ModEntry("simple-discord-rpc", "Simple Discord RPC")
    );

    private final Path modsDir;
    private final Path hiddenDir;
    private final HttpClient httpClient;
    private final AtomicBoolean newModsDownloaded = new AtomicBoolean(false);

    public ModDownloader(Path modsDir) {
        this.modsDir = modsDir;
        this.hiddenDir = modsDir.resolve(HIDDEN_DIR);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean hasNewMods() {
        return newModsDownloaded.get();
    }

    public boolean hasRequiredModsLocally() {
        try {
            Files.createDirectories(hiddenDir);
        } catch (IOException e) {
            return false;
        }

        removeSodium();

        for (ModEntry mod : REQUIRED_MODS) {
            if (!isModPresentLocally(mod)) {
                return false;
            }
        }
        return true;
    }

    public boolean downloadAllMods() {
        LOGGER.info("Starting automatic mod download to hidden dir...");

        try {
            Files.createDirectories(hiddenDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create hidden mods directory: {}", hiddenDir, e);
            return false;
        }

        removeSodium();

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(3, REQUIRED_MODS.size()));
        List<CompletableFuture<Void>> futures = REQUIRED_MODS.stream()
                .map(mod -> CompletableFuture.runAsync(() -> downloadMod(mod), executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        LOGGER.info("Mod download completed. New mods: {}", newModsDownloaded.get());
        return newModsDownloaded.get();
    }

    private void removeSodium() {
        // Hem mods/ hem de mods/.pavclient/ icinden kaldir
        removeSodiumFrom(modsDir);
        removeSodiumFrom(hiddenDir);
    }

    private void removeSodiumFrom(Path dir) {
        try {
            if (!Files.exists(dir)) return;
            Files.list(dir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.contains("sodium") && name.endsWith(".jar");
                    })
                    .forEach(p -> {
                        try {
                            LOGGER.info("Removing incompatible mod: {} (gl4es crash)", p.getFileName());
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to scan for Sodium in {}", dir, e);
        }
    }

    private void downloadMod(ModEntry mod) {
        try {
            LOGGER.info("Checking: {} ({})", mod.displayName(), mod.slug());

            JsonArray versions = fetchVersions(mod.slug());
            if (versions == null || versions.isEmpty()) {
                LOGGER.warn("No version found for {} (MC {}/{})", mod.displayName(), GAME_VERSION, LOADER);
                return;
            }

            JsonObject version = findBestVersion(versions);
            if (version == null) {
                LOGGER.warn("No release for {}", mod.displayName());
                return;
            }

            JsonArray files = version.getAsJsonArray("files");
            JsonObject primaryFile = findPrimaryFile(files);
            if (primaryFile == null) {
                LOGGER.warn("No file for {}", mod.displayName());
                return;
            }

            String downloadUrl = primaryFile.get("url").getAsString();
            String originalFileName = primaryFile.get("filename").getAsString();
            String expectedSha512 = primaryFile.getAsJsonObject("hashes").get("sha512").getAsString();

            // Zaten indirildiyse hash kontrol et (hem gizli hem normal dizinde)
            if (isModPresentWithHash(mod, expectedSha512)) {
                LOGGER.info("{} up to date", mod.displayName());
                return;
            }

            // Eski versiyonlari temizle
            cleanOldVersions(mod);

            // Random isimle gizli klasore indir
            String randomName = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ".jar";
            Path targetFile = hiddenDir.resolve(randomName);

            LOGGER.info("Downloading {} -> .pavclient/{}", mod.displayName(), randomName);
            downloadFile(downloadUrl, targetFile);

            String downloadedHash = computeSha512(targetFile);
            if (!expectedSha512.equals(downloadedHash)) {
                LOGGER.error("Hash mismatch for {}! Deleting.", mod.displayName());
                Files.deleteIfExists(targetFile);
                return;
            }

            // Slug mapping dosyasi olustur (hangi random dosya hangi mod)
            Path mappingFile = hiddenDir.resolve(randomName + ".meta");
            Files.writeString(mappingFile, mod.slug() + "\n" + originalFileName + "\n" + expectedSha512);

            LOGGER.info("Verified: {} -> .pavclient/{}", mod.displayName(), randomName);
            newModsDownloaded.set(true);

        } catch (Exception e) {
            LOGGER.error("Failed to download: {}", mod.displayName(), e);
        }
    }

    /**
     * Mod'un hem gizli hem normal dizinde var olup olmadigi + hash kontrolu.
     */
    private boolean isModPresentWithHash(ModEntry mod, String expectedHash) {
        // Gizli dizinde meta dosyalarindan kontrol
        try {
            if (Files.exists(hiddenDir)) {
                var metaFiles = Files.list(hiddenDir)
                        .filter(p -> p.toString().endsWith(".meta"))
                        .toList();
                for (Path meta : metaFiles) {
                    String content = Files.readString(meta);
                    String[] lines = content.split("\n");
                    if (lines.length >= 3 && lines[0].equals(mod.slug()) && lines[2].equals(expectedHash)) {
                        // Jar dosyasi da var mi?
                        String jarName = meta.getFileName().toString().replace(".meta", "");
                        if (Files.exists(hiddenDir.resolve(jarName))) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException ignored) {}

        // Normal mods/ dizininde eski tarzda arama
        return isModPresentLocally(mod);
    }

    private boolean isModPresentLocally(ModEntry mod) {
        // Gizli dizinde meta dosyalarindan kontrol et
        try {
            if (Files.exists(hiddenDir)) {
                var metaFiles = Files.list(hiddenDir)
                        .filter(p -> p.toString().endsWith(".meta"))
                        .toList();
                for (Path meta : metaFiles) {
                    String content = Files.readString(meta);
                    if (content.startsWith(mod.slug() + "\n")) {
                        String jarName = meta.getFileName().toString().replace(".meta", "");
                        if (Files.exists(hiddenDir.resolve(jarName))) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException ignored) {}

        // Fallback: mods/ dizininde isimle arama (eski yontem)
        try {
            return Files.list(modsDir)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .map(p -> p.getFileName().toString().toLowerCase())
                    .anyMatch(name -> name.contains(mod.localKeyword()));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Eski versiyonlari temizle (hem gizli hem normal dizinde).
     */
    private void cleanOldVersions(ModEntry mod) {
        // Gizli dizinden meta + jar temizle
        try {
            if (Files.exists(hiddenDir)) {
                var metaFiles = Files.list(hiddenDir)
                        .filter(p -> p.toString().endsWith(".meta"))
                        .toList();
                for (Path meta : metaFiles) {
                    String content = Files.readString(meta);
                    if (content.startsWith(mod.slug() + "\n")) {
                        String jarName = meta.getFileName().toString().replace(".meta", "");
                        Files.deleteIfExists(hiddenDir.resolve(jarName));
                        Files.deleteIfExists(meta);
                        LOGGER.info("Cleaned old version of {} from hidden dir", mod.displayName());
                    }
                }
            }
        } catch (IOException ignored) {}

        // Normal mods/ dizininden de temizle
        try {
            Files.list(modsDir).filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.contains(mod.localKeyword()) && name.endsWith(".jar");
            }).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                    LOGGER.info("Cleaned old {} from mods/", p.getFileName());
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private JsonArray fetchVersions(String slug) throws IOException, InterruptedException {
        String encodedLoaders = URLEncoder.encode("[\"" + LOADER + "\"]", StandardCharsets.UTF_8);
        String encodedVersions = URLEncoder.encode("[\"" + GAME_VERSION + "\"]", StandardCharsets.UTF_8);
        String url = String.format("%s/project/%s/version?loaders=%s&game_versions=%s",
                MODRINTH_API, slug, encodedLoaders, encodedVersions);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .GET().build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            LOGGER.error("API status {} for: {}", response.statusCode(), slug);
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    private JsonObject findBestVersion(JsonArray versions) {
        for (JsonElement e : versions) {
            JsonObject v = e.getAsJsonObject();
            if ("release".equals(v.get("version_type").getAsString())) return v;
        }
        for (JsonElement e : versions) {
            JsonObject v = e.getAsJsonObject();
            if ("beta".equals(v.get("version_type").getAsString())) return v;
        }
        return versions.isEmpty() ? null : versions.get(0).getAsJsonObject();
    }

    private JsonObject findPrimaryFile(JsonArray files) {
        for (JsonElement e : files) {
            JsonObject f = e.getAsJsonObject();
            if (f.has("primary") && f.get("primary").getAsBoolean()) return f;
        }
        return files.isEmpty() ? null : files.get(0).getAsJsonObject();
    }

    private void downloadFile(String url, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofMinutes(5))
                .GET().build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200)
            throw new IOException("Download failed: " + response.statusCode());

        try (InputStream in = response.body()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String computeSha512(Path file) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-512");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    d.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(d.digest());
        } catch (Exception e) {
            return "";
        }
    }

    private record ModEntry(String slug, String displayName) {
        String localKeyword() {
            return switch (slug) {
                case "viafabricplus" -> "viafabricplus";
                case "ferrite-core" -> "ferritecore";
                case "cloth-config" -> "cloth-config";
                case "modmenu" -> "modmenu";
                case "mouse-tweaks" -> "mousetweaks";
                case "appleskin" -> "appleskin";
                case "chat-heads" -> "chat_heads";
                case "3dskinlayers" -> "skinlayers3d";
                case "simple-voice-chat" -> "voicechat";
                case "fabric-language-kotlin" -> "fabric-language-kotlin";
                case "yacl" -> "yet-another-config";
                case "zoomify" -> "zoomify";
                case "not-enough-animations" -> "notenoughanimations";
                case "entityculling" -> "entityculling";
                case "c2me-fabric" -> "c2me";
                case "simple-discord-rpc" -> "simple-discord-rpc";
                default -> slug;
            };
        }
    }
}
