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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Automatic mod downloader using the Modrinth API v2.
 *
 * Mods:
 * - ViaFabricPlus (multi-version, includes ViaBackwards + ViaRewind)
 * - Lithium (optimization - Pojav compatible)
 * - FerriteCore (memory optimization)
 * - Mod Menu (mod list GUI)
 * - Cloth Config (settings API)
 *
 * NOTE: Sodium REMOVED - causes SIGSEGV crash on Pojav/ZalithLauncher (gl4es)
 */
public class ModDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME + "/ModDownloader");

    private static final String MODRINTH_API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "LemonyMC-Dev/PavClient/2.0.0 (pavclient)";
    private static final String GAME_VERSION = "1.21.4";
    private static final String LOADER = "fabric";

    private static final List<ModEntry> REQUIRED_MODS = List.of(
            new ModEntry("viafabricplus", "ViaFabricPlus"),
            new ModEntry("lithium", "Lithium"),
            new ModEntry("ferrite-core", "FerriteCore"),
            new ModEntry("modmenu", "Mod Menu"),
            new ModEntry("cloth-config", "Cloth Config")
    );

    private final Path modsDir;
    private final HttpClient httpClient;
    private final AtomicBoolean newModsDownloaded = new AtomicBoolean(false);

    public ModDownloader(Path modsDir) {
        this.modsDir = modsDir;
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
            Files.createDirectories(modsDir);
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
        LOGGER.info("Starting automatic mod download...");

        try {
            Files.createDirectories(modsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create mods directory: {}", modsDir, e);
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
        try {
            Files.list(modsDir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("sodium") && name.endsWith(".jar");
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
            LOGGER.warn("Failed to scan for Sodium", e);
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
            String fileName = primaryFile.get("filename").getAsString();
            String expectedSha512 = primaryFile.getAsJsonObject("hashes").get("sha512").getAsString();
            Path targetFile = modsDir.resolve(fileName);

            if (Files.exists(targetFile)) {
                String existingHash = computeSha512(targetFile);
                if (expectedSha512.equals(existingHash)) {
                    LOGGER.info("{} up to date: {}", mod.displayName(), fileName);
                    return;
                }
                LOGGER.info("{} outdated, updating...", mod.displayName());
                cleanOldVersions(mod.slug(), fileName);
            }

            LOGGER.info("Downloading {} -> {}", mod.displayName(), fileName);
            downloadFile(downloadUrl, targetFile);

            String downloadedHash = computeSha512(targetFile);
            if (!expectedSha512.equals(downloadedHash)) {
                LOGGER.error("Hash mismatch for {}! Deleting.", mod.displayName());
                Files.deleteIfExists(targetFile);
                return;
            }

            LOGGER.info("Verified: {} ({})", mod.displayName(), fileName);
            newModsDownloaded.set(true);

        } catch (Exception e) {
            LOGGER.error("Failed to download: {}", mod.displayName(), e);
        }
    }

    private boolean isModPresentLocally(ModEntry mod) {
        try {
            return Files.list(modsDir)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .map(p -> p.getFileName().toString().toLowerCase())
                    .anyMatch(name -> name.contains(mod.localKeyword()));
        } catch (IOException e) {
            return false;
        }
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

    private void cleanOldVersions(String slug, String currentFileName) {
        try {
            Files.list(modsDir).filter(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return (name.startsWith(slug.replace("-", "")) || name.startsWith(slug))
                        && name.endsWith(".jar") && !name.equals(currentFileName.toLowerCase());
            }).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private record ModEntry(String slug, String displayName) {
        String localKeyword() {
            return switch (slug) {
                case "viafabricplus" -> "viafabricplus";
                case "ferrite-core" -> "ferritecore";
                case "cloth-config" -> "cloth-config";
                case "modmenu" -> "modmenu";
                default -> slug;
            };
        }
    }
}
