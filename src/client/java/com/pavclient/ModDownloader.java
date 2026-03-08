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

/**
 * Automatic mod downloader using the Modrinth API v2.
 * Downloads required mods for PavClient to function properly.
 *
 * Mods downloaded:
 * - ViaFabricPlus (multi-version protocol, includes ViaBackwards + ViaRewind)
 * - Sodium (rendering optimization)
 * - Lithium (general optimization)
 * - FerriteCore (memory optimization)
 * - Mod Menu (mod list GUI)
 * - Cloth Config (settings API, dependency for many mods)
 */
public class ModDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME + "/ModDownloader");

    private static final String MODRINTH_API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "LemonyMC-Dev/PavClient/1.0.0 (pavclient)";
    private static final String GAME_VERSION = "1.21.4";
    private static final String LOADER = "fabric";

    /**
     * List of mods to download, identified by their Modrinth slug.
     */
    private static final List<ModEntry> REQUIRED_MODS = List.of(
            new ModEntry("viafabricplus", "ViaFabricPlus"),
            new ModEntry("sodium", "Sodium"),
            new ModEntry("lithium", "Lithium"),
            new ModEntry("ferrite-core", "FerriteCore"),
            new ModEntry("modmenu", "Mod Menu"),
            new ModEntry("cloth-config", "Cloth Config")
    );

    private final Path modsDir;
    private final HttpClient httpClient;

    public ModDownloader(Path modsDir) {
        this.modsDir = modsDir;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Downloads all required mods asynchronously.
     * Already-downloaded mods are skipped (hash check).
     */
    public void downloadAllMods() {
        LOGGER.info("Starting automatic mod download...");

        // Ensure mods directory exists
        try {
            Files.createDirectories(modsDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create mods directory: {}", modsDir, e);
            return;
        }

        // Download mods in parallel using virtual threads
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<CompletableFuture<Void>> futures = REQUIRED_MODS.stream()
                .map(mod -> CompletableFuture.runAsync(() -> downloadMod(mod), executor))
                .toList();

        // Wait for all downloads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
        LOGGER.info("Mod download process completed.");
    }

    /**
     * Downloads a single mod from Modrinth.
     */
    private void downloadMod(ModEntry mod) {
        try {
            LOGGER.info("Checking mod: {} ({})", mod.displayName(), mod.slug());

            // Step 1: Get the latest version for our game version + loader
            JsonArray versions = fetchVersions(mod.slug());
            if (versions == null || versions.isEmpty()) {
                LOGGER.warn("No compatible version found for {} (MC {} / {})", mod.displayName(), GAME_VERSION, LOADER);
                return;
            }

            // Pick the first release version (latest)
            JsonObject version = findBestVersion(versions);
            if (version == null) {
                LOGGER.warn("No suitable release version found for {}", mod.displayName());
                return;
            }

            // Step 2: Get the primary file from the version
            JsonArray files = version.getAsJsonArray("files");
            JsonObject primaryFile = findPrimaryFile(files);
            if (primaryFile == null) {
                LOGGER.warn("No primary file found for {} version {}", mod.displayName(),
                        version.get("version_number").getAsString());
                return;
            }

            String downloadUrl = primaryFile.get("url").getAsString();
            String fileName = primaryFile.get("filename").getAsString();
            String expectedSha512 = primaryFile.getAsJsonObject("hashes").get("sha512").getAsString();

            Path targetFile = modsDir.resolve(fileName);

            // Step 3: Check if already downloaded with correct hash
            if (Files.exists(targetFile)) {
                String existingHash = computeSha512(targetFile);
                if (expectedSha512.equals(existingHash)) {
                    LOGGER.info("Mod {} is already up to date: {}", mod.displayName(), fileName);
                    return;
                } else {
                    LOGGER.info("Mod {} has outdated version, re-downloading...", mod.displayName());
                    // Remove old versions of this mod (different filenames)
                    cleanOldVersions(mod.slug(), fileName);
                }
            }

            // Step 4: Download the file
            LOGGER.info("Downloading {} -> {}", mod.displayName(), fileName);
            downloadFile(downloadUrl, targetFile);

            // Step 5: Verify hash
            String downloadedHash = computeSha512(targetFile);
            if (!expectedSha512.equals(downloadedHash)) {
                LOGGER.error("Hash mismatch for {}! Expected: {}, Got: {}. Deleting corrupted file.",
                        mod.displayName(), expectedSha512, downloadedHash);
                Files.deleteIfExists(targetFile);
                return;
            }

            LOGGER.info("Successfully downloaded and verified: {} ({})", mod.displayName(), fileName);

        } catch (Exception e) {
            LOGGER.error("Failed to download mod: {}", mod.displayName(), e);
        }
    }

    /**
     * Fetches available versions for a mod from Modrinth API.
     */
    private JsonArray fetchVersions(String slug) throws IOException, InterruptedException {
        String encodedLoaders = URLEncoder.encode("[\"" + LOADER + "\"]", StandardCharsets.UTF_8);
        String encodedVersions = URLEncoder.encode("[\"" + GAME_VERSION + "\"]", StandardCharsets.UTF_8);

        String url = String.format("%s/project/%s/version?loaders=%s&game_versions=%s",
                MODRINTH_API, slug, encodedLoaders, encodedVersions);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            LOGGER.error("Modrinth API returned status {} for slug: {}", response.statusCode(), slug);
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        }
    }

    /**
     * Finds the best (latest release) version from the versions array.
     */
    private JsonObject findBestVersion(JsonArray versions) {
        // First try to find a release version
        for (JsonElement elem : versions) {
            JsonObject ver = elem.getAsJsonObject();
            if ("release".equals(ver.get("version_type").getAsString())) {
                return ver;
            }
        }
        // Fallback to beta
        for (JsonElement elem : versions) {
            JsonObject ver = elem.getAsJsonObject();
            if ("beta".equals(ver.get("version_type").getAsString())) {
                return ver;
            }
        }
        // Last resort: any version
        if (!versions.isEmpty()) {
            return versions.get(0).getAsJsonObject();
        }
        return null;
    }

    /**
     * Finds the primary file in a version's files array.
     */
    private JsonObject findPrimaryFile(JsonArray files) {
        // Look for primary file first
        for (JsonElement elem : files) {
            JsonObject file = elem.getAsJsonObject();
            if (file.has("primary") && file.get("primary").getAsBoolean()) {
                return file;
            }
        }
        // Fallback to first file
        if (!files.isEmpty()) {
            return files.get(0).getAsJsonObject();
        }
        return null;
    }

    /**
     * Downloads a file from the given URL to the target path.
     */
    private void downloadFile(String url, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Download failed with status " + response.statusCode() + " for URL: " + url);
        }

        try (InputStream in = response.body()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Computes SHA-512 hash of a file.
     */
    private String computeSha512(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] fileBytes = Files.readAllBytes(file);
            byte[] hashBytes = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            LOGGER.error("Failed to compute SHA-512 for: {}", file, e);
            return "";
        }
    }

    /**
     * Removes old versions of a mod from the mods directory.
     * Tries to match by mod slug prefix in the filename.
     */
    private void cleanOldVersions(String slug, String currentFileName) {
        try {
            // Common naming patterns: sodium-fabric-0.6.0+mc1.21.4.jar
            // We try to find files starting with the slug name
            Files.list(modsDir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        String slugLower = slug.replace("-", "").toLowerCase();
                        String slugDash = slug.toLowerCase();
                        return (name.startsWith(slugLower) || name.startsWith(slugDash))
                                && name.endsWith(".jar")
                                && !name.equals(currentFileName.toLowerCase());
                    })
                    .forEach(p -> {
                        try {
                            LOGGER.info("Removing old version: {}", p.getFileName());
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete old mod file: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to scan mods directory for old versions of: {}", slug, e);
        }
    }

    /**
     * Represents a mod entry to be downloaded.
     */
    private record ModEntry(String slug, String displayName) {
    }
}
