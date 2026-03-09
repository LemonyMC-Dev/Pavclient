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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mod downloader - mods/ klasorune RANDOM isimle indirir.
 * Guvenlik: PavClient'in indirmedigi modlari notmods/ klasorune tasir.
 * pavclient.jar ve fabric-api disindaki tum jar'lar kontrol edilir.
 *
 * Meta dosyalari: mods/.pvmeta/ altinda slug=randomname.jar eslesmesi tutulur.
 */
public class ModDownloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PavClient.MOD_NAME + "/ModDownloader");

    private static final String MODRINTH_API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "LemonyMC-Dev/PavClient/2.0.0 (pavclient)";
    private static final String GAME_VERSION = "1.21.4";
    private static final String LOADER = "fabric";

    /** Meta klasor - hangi random dosyanin hangi mod oldugunu tutar */
    private static final String META_DIR = ".pvmeta";

    /** Izin verilen dosya isim kaliplari (PavClient veya FabricAPI) */
    private static final List<String> WHITELISTED_PATTERNS = List.of(
            "pavclient", "fabric-api", "fabricapi"
    );

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
            new ModEntry("immediatelyfast", "ImmediatelyFast"),
            new ModEntry("moreculling", "More Culling")
    );

    /** Java 22+ gerektiren modlar (Pojav Java 21 kullaniyorsa indirilmez) */
    private static final List<ModEntry> JAVA22_MODS = List.of(
            new ModEntry("vanilla-rpc", "Vanilla RPC")
    );

    private static boolean isJava22OrHigher() {
        try {
            int ver = Runtime.version().feature();
            return ver >= 22;
        } catch (Exception e) {
            // Fallback: spec version parse
            String specVer = System.getProperty("java.specification.version", "21");
            try { return Integer.parseInt(specVer) >= 22; } catch (NumberFormatException ex) { return false; }
        }
    }

    private final Path modsDir;
    private final Path metaDir;
    private final Path notModsDir;
    private final HttpClient httpClient;
    private final AtomicBoolean newModsDownloaded = new AtomicBoolean(false);

    public ModDownloader(Path modsDir) {
        this.modsDir = modsDir;
        this.metaDir = modsDir.resolve(META_DIR);
        this.notModsDir = modsDir.getParent().resolve("notmods");
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
            Files.createDirectories(metaDir);
        } catch (IOException e) {
            return false;
        }

        removeSodium();
        enforceModSecurity();

        for (ModEntry mod : REQUIRED_MODS) {
            if (!isModPresentLocally(mod)) {
                return false;
            }
        }
        // Java 22+ modlari
        if (isJava22OrHigher()) {
            for (ModEntry mod : JAVA22_MODS) {
                if (!isModPresentLocally(mod)) {
                    return false;
                }
            }
            LOGGER.info("Java {} detected - Discord RPC enabled", Runtime.version().feature());
        } else {
            LOGGER.info("Java {} detected - Discord RPC disabled (requires Java 22+)", System.getProperty("java.specification.version"));
        }
        return true;
    }

    public boolean downloadAllMods() {
        LOGGER.info("Starting automatic mod download...");

        try {
            Files.createDirectories(modsDir);
            Files.createDirectories(metaDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create directories", e);
            return false;
        }

        removeSodium();
        enforceModSecurity();

        // Tum indirilebilecek modlari birlestir
        List<ModEntry> allMods = new ArrayList<>(REQUIRED_MODS);
        if (isJava22OrHigher()) {
            allMods.addAll(JAVA22_MODS);
            LOGGER.info("Java 22+ detected - including Discord RPC");
        } else {
            LOGGER.info("Java 21 detected - skipping Discord RPC (requires Java 22+)");
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(3, allMods.size()));
        List<CompletableFuture<Void>> futures = allMods.stream()
                .map(mod -> CompletableFuture.runAsync(() -> downloadMod(mod), executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // Indirme sonrasi tekrar guvenlik kontrolu
        enforceModSecurity();

        LOGGER.info("Mod download completed. New mods: {}", newModsDownloaded.get());
        return newModsDownloaded.get();
    }

    /**
     * GUVENLIK: PavClient'in indirmedigi modlari notmods/ klasorune tasir.
     * pavclient.jar ve fabric-api haric tum jar'lar kontrol edilir.
     */
    private void enforceModSecurity() {
        try {
            // Meta'dan bilinen dosya isimlerini topla
            Set<String> knownFiles = new HashSet<>();
            if (Files.exists(metaDir)) {
                Files.list(metaDir).filter(p -> p.toString().endsWith(".meta")).forEach(meta -> {
                    try {
                        String content = Files.readString(meta);
                        String[] lines = content.split("\n");
                        if (lines.length >= 2) {
                            knownFiles.add(lines[1]); // random dosya adi
                        }
                    } catch (IOException ignored) {}
                });
            }

            // mods/ klasorundeki jar'lari kontrol et
            Files.list(modsDir)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar"))
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(jarFile -> {
                        String name = jarFile.getFileName().toString().toLowerCase();

                        // Whitelist kontrolu (pavclient, fabric-api)
                        boolean isWhitelisted = WHITELISTED_PATTERNS.stream().anyMatch(name::contains);
                        if (isWhitelisted) return;

                        // Meta'da kayitli mi?
                        boolean isKnown = knownFiles.contains(jarFile.getFileName().toString());
                        if (isKnown) return;

                        // Bilinmeyen mod - notmods/ klasorune tasi
                        try {
                            Files.createDirectories(notModsDir);
                            Path target = notModsDir.resolve(jarFile.getFileName().toString());
                            Files.move(jarFile, target, StandardCopyOption.REPLACE_EXISTING);
                            LOGGER.warn("SECURITY: Moved unknown mod to notmods/: {}", jarFile.getFileName());
                        } catch (IOException e) {
                            LOGGER.warn("Failed to move unknown mod: {}", jarFile.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to enforce mod security", e);
        }
    }

    private void removeSodium() {
        try {
            Files.list(modsDir)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.contains("sodium") && name.endsWith(".jar");
                    })
                    .forEach(p -> {
                        try {
                            LOGGER.info("Removing incompatible: {} (gl4es crash)", p.getFileName());
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

            JsonObject version = findBestVersion(mod, versions);
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
            String expectedSha512 = primaryFile.getAsJsonObject("hashes").get("sha512").getAsString();

            // Zaten dogru hash ile mevcut mu?
            Path existingFile = findExistingByMeta(mod.slug());
            if (existingFile != null && Files.exists(existingFile)) {
                String existingHash = computeSha512(existingFile);
                if (expectedSha512.equals(existingHash)) {
                    LOGGER.info("{} up to date", mod.displayName());
                    return;
                }
                // Eski versiyon - sil
                Files.deleteIfExists(existingFile);
                deleteMetaForSlug(mod.slug());
            }

            // Random isimle indir
            String randomName = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + ".jar";
            Path targetFile = modsDir.resolve(randomName);

            LOGGER.info("Downloading {} -> {}", mod.displayName(), randomName);
            downloadFile(downloadUrl, targetFile);

            String downloadedHash = computeSha512(targetFile);
            if (!expectedSha512.equals(downloadedHash)) {
                LOGGER.error("Hash mismatch for {}! Deleting.", mod.displayName());
                Files.deleteIfExists(targetFile);
                return;
            }

            // Meta kaydet
            Path metaFile = metaDir.resolve(mod.slug() + ".meta");
            Files.writeString(metaFile, mod.slug() + "\n" + randomName + "\n" + expectedSha512);

            LOGGER.info("Verified: {} ({})", mod.displayName(), randomName);
            newModsDownloaded.set(true);

        } catch (Exception e) {
            LOGGER.error("Failed to download: {}", mod.displayName(), e);
        }
    }

    /** Meta dosyasindan mevcut jar dosyasini bul */
    private Path findExistingByMeta(String slug) {
        Path metaFile = metaDir.resolve(slug + ".meta");
        if (Files.exists(metaFile)) {
            try {
                String content = Files.readString(metaFile);
                String[] lines = content.split("\n");
                if (lines.length >= 2) {
                    return modsDir.resolve(lines[1]);
                }
            } catch (IOException ignored) {}
        }
        return null;
    }

    /** Slug icin meta dosyasini sil */
    private void deleteMetaForSlug(String slug) {
        try {
            Files.deleteIfExists(metaDir.resolve(slug + ".meta"));
        } catch (IOException ignored) {}
    }

    private boolean isModPresentLocally(ModEntry mod) {
        Path existing = findExistingByMeta(mod.slug());
        if (existing != null && Files.exists(existing)) {
            return true;
        }
        // Fallback: isim icerigi ile arama (eski tarz dosyalar icin)
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

    private JsonObject findBestVersion(ModEntry mod, JsonArray versions) {
        // Mod-specific minimum version constraints
        if ("fabric-language-kotlin".equals(mod.slug())) {
            JsonObject best = findBestKotlinLanguageVersion(versions);
            if (best != null) return best;
        }

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

    /**
     * Zoomify icin gereken min: fabric-language-kotlin >= 1.13.8+kotlin.2.3.0
     * Bu metoda uygun release versiyonunu secer.
     */
    private JsonObject findBestKotlinLanguageVersion(JsonArray versions) {
        JsonObject fallback = null;
        for (JsonElement e : versions) {
            JsonObject v = e.getAsJsonObject();
            String type = v.get("version_type").getAsString();
            String num = v.get("version_number").getAsString();

            // once release sakla (fallback)
            if (fallback == null && "release".equals(type)) {
                fallback = v;
            }

            // Basit ama guvenli kontrol: kotlin.2.3.0 veya uzeri track
            if ("release".equals(type) && isKotlin230OrHigher(num)) {
                return v;
            }
        }
        return fallback;
    }

    private boolean isKotlin230OrHigher(String versionNumber) {
        // Orn: 1.13.8+kotlin.2.3.0
        int k = versionNumber.indexOf("kotlin.");
        if (k < 0) return false;
        String tail = versionNumber.substring(k + "kotlin.".length());
        String[] parts = tail.split("[^0-9]+", 4);
        if (parts.length < 2) return false;

        int major = parseIntSafe(parts, 0);
        int minor = parseIntSafe(parts, 1);
        int patch = parseIntSafe(parts, 2);

        if (major != 2) return major > 2;
        if (minor != 3) return minor > 3;
        return patch >= 0;
    }

    private int parseIntSafe(String[] arr, int idx) {
        if (idx >= arr.length || arr[idx] == null || arr[idx].isBlank()) return 0;
        try {
            return Integer.parseInt(arr[idx]);
        } catch (NumberFormatException e) {
            return 0;
        }
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
                case "immediatelyfast" -> "immediatelyfast";
                case "moreculling" -> "moreculling";
                case "vanilla-rpc" -> "vanillarpc";
                default -> slug;
            };
        }
    }
}
