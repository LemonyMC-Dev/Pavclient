package com.pavclient.emote;

import com.pavclient.PavClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emote/Dans sistemi.
 * PavClient kullanicilari plugin channel uzerinden emote paylasir.
 * Sunucu plugin channel'i bilmese bile client-side calisir.
 */
public final class EmoteManager {

    private EmoteManager() {}

    public static final String[] EMOTE_NAMES = {
        "Dalga", "Dans", "Selam", "Alkis", "Donus"
    };

    public static final Identifier EMOTE_CHANNEL = Identifier.of(PavClient.MOD_ID, "emote");
    public static final Identifier CLIENT_HELLO = Identifier.of(PavClient.MOD_ID, "hello");

    /** Aktif emote oynayan oyuncular: UUID -> emoteId (-1 = yok) */
    private static final Map<UUID, EmoteState> activeEmotes = new ConcurrentHashMap<>();

    /** PavClient kullanan oyuncular */
    private static final Map<UUID, Boolean> pavClientUsers = new ConcurrentHashMap<>();

    private static long localEmoteStart = 0;
    private static int localEmoteId = -1;
    private static final long EMOTE_DURATION_MS = 3000L;

    public static void init() {
        // Channel'lari kaydet (sunucu kabul etmezse sessizce basarisiz olur)
        try {
            PayloadTypeRegistry.playC2S().register(EmotePayload.ID, EmotePayload.CODEC);
            PayloadTypeRegistry.playS2C().register(EmotePayload.ID, EmotePayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(EmotePayload.ID, (payload, context) -> {
                context.client().execute(() -> {
                    if (payload.type == 0) {
                        // Emote paketi
                        activeEmotes.put(payload.playerUuid, new EmoteState(payload.emoteId, System.currentTimeMillis()));
                    } else if (payload.type == 1) {
                        // Hello - bu oyuncu PavClient kullaniyor
                        pavClientUsers.put(payload.playerUuid, true);
                    }
                });
            });
        } catch (Exception e) {
            PavClient.LOGGER.debug("[{}] Channel registration skipped: {}", PavClient.MOD_NAME, e.getMessage());
        }
    }

    /** Emote baslat */
    public static void playEmote(int emoteId) {
        localEmoteId = emoteId;
        localEmoteStart = System.currentTimeMillis();

        // Sunucuya gonder (sunucu kabul etmezse ignore edilir)
        try {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null && mc.getNetworkHandler() != null) {
                ClientPlayNetworking.send(new EmotePayload(mc.player.getUuid(), emoteId, (byte) 0));
            }
        } catch (Exception ignored) {
            // Sunucu channel'i bilmiyorsa sessizce devam et
        }
    }

    /** Bu oyuncu su an emote oynuyor mu? */
    public static int getActiveEmote(UUID uuid) {
        // Yerel oyuncu
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getUuid().equals(uuid)) {
            if (localEmoteId >= 0 && System.currentTimeMillis() - localEmoteStart < EMOTE_DURATION_MS) {
                return localEmoteId;
            }
            localEmoteId = -1;
            return -1;
        }

        // Diger oyuncular
        EmoteState state = activeEmotes.get(uuid);
        if (state != null && System.currentTimeMillis() - state.startTime < EMOTE_DURATION_MS) {
            return state.emoteId;
        }
        activeEmotes.remove(uuid);
        return -1;
    }

    public static boolean isLocalEmotePlaying() {
        return localEmoteId >= 0 && System.currentTimeMillis() - localEmoteStart < EMOTE_DURATION_MS;
    }

    public static int getLocalEmoteId() { return localEmoteId; }

    /** Bu oyuncu PavClient kullaniyor mu? */
    public static boolean isPavClientUser(UUID uuid) {
        return pavClientUsers.containsKey(uuid);
    }

    /** Hello mesaji gonder (oyuna giriste) */
    public static void sendHello() {
        try {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player != null && mc.getNetworkHandler() != null) {
                ClientPlayNetworking.send(new EmotePayload(mc.player.getUuid(), 0, (byte) 1));
            }
        } catch (Exception ignored) {}
    }

    public static void markAsPavClient(UUID uuid) {
        pavClientUsers.put(uuid, true);
    }

    private record EmoteState(int emoteId, long startTime) {}

    /** Custom payload for emote data */
    public record EmotePayload(UUID playerUuid, int emoteId, byte type) implements CustomPayload {
        public static final Id<EmotePayload> ID = new Id<>(EMOTE_CHANNEL);
        public static final PacketCodec<PacketByteBuf, EmotePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeUuid(value.playerUuid);
                buf.writeVarInt(value.emoteId);
                buf.writeByte(value.type);
            },
            buf -> new EmotePayload(buf.readUuid(), buf.readVarInt(), buf.readByte())
        );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }
}
