package com.pavclient.mixin;

import com.pavclient.PavClientUsers;
import com.pavclient.config.PavConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametag mixin:
 * - Kendi ismini goster toggle
 * - SADECE PavClient kullanan oyunculara PM | prefix
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerNametagMixin {

    @Unique
    private static boolean pavclient$rendering = false;

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void pavclient$modifyNametag(PlayerEntityRenderState state, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (pavclient$rendering) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        String playerName = text.getString();
        String localName = mc.player.getName().getString();
        boolean isSelf = playerName.equals(localName);

        // Kendi ismini gosterme kapali
        if (isSelf && !PavConfig.get().showOwnName) {
            ci.cancel();
            return;
        }

        // Sadece PavClient kullanicilarina PM | ekle
        if (PavClientUsers.isPavUser(playerName)) {
            ci.cancel();

            MutableText prefix = Text.literal("PM")
                    .formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal(" | ").formatted(Formatting.GRAY));
            MutableText name = Text.literal(playerName).formatted(Formatting.WHITE);
            MutableText fullText = prefix.append(name);

            pavclient$rendering = true;
            try {
                pavclient$renderNameTag(state, fullText, matrices, vertexConsumers, light);
            } finally {
                pavclient$rendering = false;
            }
        }
    }

    @Unique
    private void pavclient$renderNameTag(PlayerEntityRenderState state, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        var textRenderer = mc.textRenderer;
        float nametagY = state.height + 0.5f;

        matrices.push();
        matrices.translate(0.0f, nametagY, 0.0f);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);

        float textWidth = textRenderer.getWidth(text);
        float x = -textWidth / 2.0f;
        int bgColor = (int)(mc.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;

        textRenderer.draw(text, x, 0, 0x20FFFFFF, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
        textRenderer.draw(text, x, 0, -1, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, light);

        matrices.pop();
    }
}
