package com.pavclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PavClient kullanicisi tespiti.
 * Custom plugin channel ile haberlesme yapar.
 * Ayrica tum sunucudaki oyunculara PM | gosterir (client-side branding).
 */
public final class PavClientUsers {

    /** PavClient kullanan oyuncularin isimleri */
    private static final Set<String> PAV_USERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private PavClientUsers() {}

    /**
     * Bir oyuncunun PavClient kullanicisi olup olmadigini kontrol eder.
     * Simdilik: Ayni sunucuda olan tum oyuncular icin PM | gosterilir.
     * Kendi ismimiz her zaman PM | gosterilir.
     */
    public static boolean isPavUser(String playerName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return false;

        // Kendi ismimiz her zaman PavClient kullanicisi
        if (playerName.equals(mc.player.getName().getString())) {
            return true;
        }

        // Eger custom channel ile dogrulanmis bir kullanici ise
        if (PAV_USERS.contains(playerName)) {
            return true;
        }

        // Sunucudaki tum oyunculara PM | goster (client-side)
        // Bu sayede diger PavClient kullananlar da PM | gorur
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            for (PlayerListEntry entry : handler.getPlayerList()) {
                if (entry.getProfile().getName().equals(playerName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Bir oyuncuyu PavClient kullanicisi olarak kaydet */
    public static void addPavUser(String playerName) {
        PAV_USERS.add(playerName);
    }

    /** Bir oyuncuyu PavClient kullanicisi listesinden cikar */
    public static void removePavUser(String playerName) {
        PAV_USERS.remove(playerName);
    }

    /** Tum PavClient kullanicilarini temizle (disconnect'te) */
    public static void clear() {
        PAV_USERS.clear();
    }
}
