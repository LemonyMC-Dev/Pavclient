package com.pavclient.network;

import com.pavclient.PavClientUsers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * PavClient custom payload channel (sunucu relay tabanli).
 * Sunucu/proxy pavclient:hello paketini alan tum clientlara yayinlarsa
 * oyuncular birbirini PM etiketiyle gorur.
 */
public final class PavClientChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger("PavClient/Channel");
    public static final Identifier CHANNEL_ID = Identifier.of("pavclient", "hello");

    private static long lastHeartbeatMs = 0L;

    public record HelloPayload(UUID playerUuid, String playerName, byte type) implements CustomPayload {
        public static final Id<HelloPayload> ID = new Id<>(CHANNEL_ID);
        public static final PacketCodec<PacketByteBuf, HelloPayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    buf.writeUuid(payload.playerUuid);
                    buf.writeString(payload.playerName);
                    buf.writeByte(payload.type);
                },
                buf -> new HelloPayload(buf.readUuid(), buf.readString(), buf.readByte())
        );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        // Register payload type
        PayloadTypeRegistry.playC2S().register(HelloPayload.ID, HelloPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HelloPayload.ID, HelloPayload.CODEC);

        // Sunucudan gelen pavclient:hello mesajlarini dinle
        ClientPlayNetworking.registerGlobalReceiver(HelloPayload.ID, (payload, context) -> {
            String name = payload.playerName();
            UUID uuid = payload.playerUuid();
            LOGGER.debug("[PavClient] Presence from {} ({})", name, uuid);
            PavClientUsers.addPavUser(uuid, name);
        });

        // Sunucuya baglaninca hello gonder
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Temizle (yeni sunucu)
            PavClientUsers.clear();

            // Kendi ismimizi ekle - handler uzerinden isim al (client.player null olabilir)
            String myName = handler.getProfile().getName();
            UUID myUuid = handler.getProfile().getId();
            if (myName != null && !myName.isEmpty()) {
                PavClientUsers.addPavUser(myUuid, myName);
                LOGGER.info("[PavClient] Added self as PavUser: {}", myName);
            } else if (client.player != null) {
                PavClientUsers.addPavUser(client.player.getUuid(), client.player.getName().getString());
            }

            // Ilk hello
            sendPresence(handler);
        });

        // 10 saniyede bir heartbeat (sunucu relay ederse client-to-client gorunur)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getNetworkHandler() == null) return;
            long now = System.currentTimeMillis();
            if (now - lastHeartbeatMs >= 10_000L) {
                sendPresence(client.getNetworkHandler());
                lastHeartbeatMs = now;
            }
        });

        // Disconnect olunca temizle
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PavClientUsers.clear();
            lastHeartbeatMs = 0L;
        });
    }

    private static void sendPresence(net.minecraft.client.network.ClientPlayNetworkHandler handler) {
        try {
            String name = handler.getProfile().getName();
            UUID uuid = handler.getProfile().getId();
            if (name == null || name.isBlank() || uuid == null) return;
            ClientPlayNetworking.send(new HelloPayload(uuid, name, (byte) 1));
        } catch (Exception e) {
            LOGGER.debug("[PavClient] Server does not support pavclient channel: {}", e.getMessage());
        }
    }
}
