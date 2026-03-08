package com.pavclient;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PavClient kullanici tespiti.
 * Sadece custom channel (pavclient:hello) ile dogrulanmis oyuncular PM | gosterir.
 * Kendi ismimiz her zaman PM | gosterilir.
 */
public final class PavClientUsers {

    private static final Set<String> PAV_USERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private PavClientUsers() {}

    /**
     * Sadece PavClient client'i kullanan oyuncular icin true doner.
     */
    public static boolean isPavUser(String playerName) {
        // Kendi ismimiz her zaman
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc != null && mc.player != null && playerName.equals(mc.player.getName().getString())) {
            return true;
        }
        // Custom channel ile dogrulanmis kullanicilar
        return PAV_USERS.contains(playerName);
    }

    public static void addPavUser(String playerName) {
        PAV_USERS.add(playerName);
    }

    public static void removePavUser(String playerName) {
        PAV_USERS.remove(playerName);
    }

    public static void clear() {
        PAV_USERS.clear();
    }
}
