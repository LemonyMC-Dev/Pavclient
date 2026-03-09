package com.pavclient.network;

import com.pavclient.PavClientUsers;
import com.pavclient.social.FriendManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class FriendsChannel {
    public static final Identifier CHANNEL_ID = Identifier.of("pavclient", "friends");

    private static long lastHeartbeatMs = 0L;

    private FriendsChannel() {}

    public record FriendsPayload(byte type, String sender, String target, String message, long ts) implements CustomPayload {
        public static final Id<FriendsPayload> ID = new Id<>(CHANNEL_ID);
        public static final PacketCodec<PacketByteBuf, FriendsPayload> CODEC = PacketCodec.of(
                (p, buf) -> {
                    buf.writeByte(p.type);
                    buf.writeString(p.sender);
                    buf.writeString(p.target);
                    buf.writeString(p.message);
                    buf.writeLong(p.ts);
                },
                buf -> new FriendsPayload(buf.readByte(), buf.readString(), buf.readString(), buf.readString(), buf.readLong())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FriendsPayload.ID, FriendsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FriendsPayload.ID, FriendsPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(FriendsPayload.ID, (payload, ctx) -> {
            String self = FriendManager.selfName();
            String sender = payload.sender();
            String target = payload.target();

            if (sender != null && !sender.isBlank()) {
                FriendManager.markOnline(sender);
                PavClientUsers.addPavUser(sender);
            }

            // 1: heartbeat broadcast
            if (payload.type() == 1) return;

            // Mesaj/request sadece hedefe
            if (target == null || !target.equalsIgnoreCase(self)) return;

            switch (payload.type()) {
                case 2 -> FriendManager.addRequest(sender);                // friend request
                case 3 -> FriendManager.addFriend(sender);                 // accepted
                case 4 -> FriendManager.removeRequest(sender);             // declined
                case 5 -> {                                                // chat
                    if (FriendManager.isFriend(sender)) {
                        FriendManager.addChatLine(sender, sender, payload.message());
                    }
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            FriendManager.markOnline(FriendManager.selfName());
            sendHeartbeat();
            lastHeartbeatMs = System.currentTimeMillis();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getNetworkHandler() == null) return;
            long now = System.currentTimeMillis();
            if (now - lastHeartbeatMs > 10_000L) {
                sendHeartbeat();
                lastHeartbeatMs = now;
            }
        });
    }

    public static void sendFriendRequest(String toName) {
        send((byte) 2, toName, "");
    }

    public static void sendAccept(String toName) {
        send((byte) 3, toName, "");
    }

    public static void sendDecline(String toName) {
        send((byte) 4, toName, "");
    }

    public static void sendChat(String toName, String msg) {
        send((byte) 5, toName, msg == null ? "" : msg);
    }

    private static void sendHeartbeat() {
        send((byte) 1, "*", "");
    }

    private static void send(byte type, String target, String message) {
        try {
            ClientPlayNetworking.send(new FriendsPayload(type, FriendManager.selfName(), target, message, System.currentTimeMillis()));
        } catch (Exception ignored) {
        }
    }
}
