package com.pavclient.social;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FriendManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pavclient_friends.json");

    private static final Set<String> FRIENDS = new LinkedHashSet<>();
    private static final Set<String> REQUESTS = new LinkedHashSet<>();
    private static final Map<String, List<ChatLine>> CHATS = new HashMap<>();
    private static final Map<String, Long> ONLINE_SEEN = new ConcurrentHashMap<>();

    private FriendManager() {}

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            Persisted p = GSON.fromJson(Files.readString(FILE), Persisted.class);
            FRIENDS.clear();
            REQUESTS.clear();
            CHATS.clear();
            if (p != null) {
                if (p.friends != null) FRIENDS.addAll(p.friends);
                if (p.requests != null) REQUESTS.addAll(p.requests);
                if (p.chats != null) CHATS.putAll(p.chats);
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        Persisted p = new Persisted();
        p.friends = new ArrayList<>(FRIENDS);
        p.requests = new ArrayList<>(REQUESTS);
        p.chats = new HashMap<>(CHATS);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(p));
        } catch (IOException ignored) {
        }
    }

    public static String selfName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) return mc.player.getName().getString();
        if (mc.getNetworkHandler() != null) return mc.getNetworkHandler().getProfile().getName();
        return "Oyuncu";
    }

    public static void markOnline(String name) {
        if (name == null || name.isBlank()) return;
        ONLINE_SEEN.put(norm(name), System.currentTimeMillis());
    }

    public static boolean isOnline(String name) {
        Long last = ONLINE_SEEN.get(norm(name));
        return last != null && (System.currentTimeMillis() - last) < 25_000L;
    }

    public static void addFriend(String name) {
        if (name == null || name.isBlank()) return;
        FRIENDS.add(name);
        REQUESTS.removeIf(r -> r.equalsIgnoreCase(name));
        save();
    }

    public static void removeFriend(String name) {
        FRIENDS.removeIf(f -> f.equalsIgnoreCase(name));
        save();
    }

    public static boolean isFriend(String name) {
        for (String f : FRIENDS) if (f.equalsIgnoreCase(name)) return true;
        return false;
    }

    public static void addRequest(String from) {
        if (from == null || from.isBlank()) return;
        if (isFriend(from)) return;
        REQUESTS.add(from);
        save();
    }

    public static void removeRequest(String from) {
        REQUESTS.removeIf(r -> r.equalsIgnoreCase(from));
        save();
    }

    public static List<String> getRequests() {
        return new ArrayList<>(REQUESTS);
    }

    public static List<String> getFriendsSorted() {
        List<String> out = new ArrayList<>(FRIENDS);
        out.sort((a, b) -> {
            boolean ao = isOnline(a);
            boolean bo = isOnline(b);
            if (ao != bo) return ao ? -1 : 1;
            return a.compareToIgnoreCase(b);
        });
        return out;
    }

    public static void addChatLine(String peer, String from, String msg) {
        if (peer == null || peer.isBlank() || msg == null) return;
        String key = norm(peer);
        CHATS.computeIfAbsent(key, k -> new ArrayList<>()).add(new ChatLine(from, msg, System.currentTimeMillis()));
        List<ChatLine> lines = CHATS.get(key);
        if (lines.size() > 200) lines.remove(0);
        save();
    }

    public static List<ChatLine> getChat(String peer) {
        return new ArrayList<>(CHATS.getOrDefault(norm(peer), List.of()));
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).trim();
    }

    public record ChatLine(String from, String message, long time) {}

    private static class Persisted {
        List<String> friends = new ArrayList<>();
        List<String> requests = new ArrayList<>();
        Map<String, List<ChatLine>> chats = new HashMap<>();
    }
}
