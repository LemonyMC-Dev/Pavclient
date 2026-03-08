package com.pavclient.network;

import com.pavclient.PavClientUsers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PavClient custom payload channel.
 * Sunucuya baglaninca "pavclient:hello" gonderir.
 * Sunucu bu mesaji diger oyunculara iletirse, diger PavClient kullanicilari tespit edilir.
 * Sunucu desteklemezse sadece kendi PM | gosterilir.
 */
public final class PavClientChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger("PavClient/Channel");
    public static final Identifier CHANNEL_ID = Identifier.of("pavclient", "hello");

    public record HelloPayload(String playerName) implements CustomPayload {
        public static final Id<HelloPayload> ID = new Id<>(CHANNEL_ID);
        public static final PacketCodec<PacketByteBuf, HelloPayload> CODEC = PacketCodec.of(
                (payload, buf) -> buf.writeString(payload.playerName),
                buf -> new HelloPayload(buf.readString())
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
            LOGGER.info("[PavClient] Received hello from: {}", name);
            PavClientUsers.addPavUser(name);
        });

        // Sunucuya baglaninca hello gonder
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Temizle (yeni sunucu)
            PavClientUsers.clear();

            // Kendi ismimizi ekle - handler uzerinden isim al (client.player null olabilir)
            String myName = handler.getProfile().getName();
            if (myName != null && !myName.isEmpty()) {
                PavClientUsers.addPavUser(myName);
                LOGGER.info("[PavClient] Added self as PavUser: {}", myName);
            } else if (client.player != null) {
                PavClientUsers.addPavUser(client.player.getName().getString());
            }

            // Sunucuya hello gonder (sunucu desteklemiyorsa sessizce basarisiz olur)
            try {
                String name = myName != null ? myName : (client.player != null ? client.player.getName().getString() : "unknown");
                ClientPlayNetworking.send(new HelloPayload(name));
                LOGGER.info("[PavClient] Sent hello to server");
            } catch (Exception e) {
                LOGGER.debug("[PavClient] Server does not support pavclient channel: {}", e.getMessage());
            }
        });

        // Disconnect olunca temizle
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PavClientUsers.clear();
        });
    }
}
