package com.pavclient;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * PavClient kullanici tespiti.
 * Sadece custom channel (pavclient:hello) ile dogrulanmis oyuncular PM | gosterir.
 * Kendi ismimiz her zaman PM | gosterilir.
 */
public final class PavClientUsers {

    private static final Set<String> PAV_USERS_BY_NAME = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<UUID> PAV_USERS_BY_UUID = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private PavClientUsers() {}

    /**
     * Sadece PavClient client'i kullanan oyuncular icin true doner.
     */
    public static boolean isPavUser(UUID uuid, String playerName) {
        // Kendi ismimiz her zaman
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            if (uuid != null && uuid.equals(mc.player.getUuid())) {
                return true;
            }
            if (playerName.equals(mc.player.getName().getString())) {
                return true;
            }
        }

        if (uuid != null && PAV_USERS_BY_UUID.contains(uuid)) {
            return true;
        }

        // Custom channel ile dogrulanmis kullanicilar (prefix/tag pluginleri tolere et)
        if (PAV_USERS_BY_NAME.contains(playerName)) {
            return true;
        }
        String lower = playerName.toLowerCase();
        for (String known : PAV_USERS_BY_NAME) {
            if (lower.contains(known.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPavUser(String playerName) {
        return isPavUser(null, playerName);
    }

    public static void addPavUser(UUID uuid, String playerName) {
        if (uuid != null) PAV_USERS_BY_UUID.add(uuid);
        if (playerName != null && !playerName.isBlank()) PAV_USERS_BY_NAME.add(playerName);
    }

    public static void addPavUser(String playerName) {
        addPavUser(null, playerName);
    }

    public static void removePavUser(String playerName) {
        PAV_USERS_BY_NAME.remove(playerName);
    }

    public static void removePavUser(UUID uuid) {
        PAV_USERS_BY_UUID.remove(uuid);
    }

    public static void clear() {
        PAV_USERS_BY_NAME.clear();
        PAV_USERS_BY_UUID.clear();
    }
}
