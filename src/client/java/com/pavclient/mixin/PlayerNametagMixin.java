package com.pavclient.mixin;

import com.pavclient.PavClientUsers;
import com.pavclient.config.PavConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
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
 * Oyuncu nametag mixin:
 * - "Kendi ismini goster" toggle
 * - Tum oyuncularin ismine "PM | " prefix
 */
@Mixin(EntityRenderer.class)
public class PlayerNametagMixin {

    @Unique
    private static boolean pavclient$rendering = false;

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void pavclient$modifyNametag(EntityRenderState state, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Sonsuz dongu korumasi
        if (pavclient$rendering) return;
        if (!(state instanceof PlayerEntityRenderState)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String playerName = text.getString();
        String localName = mc.player.getName().getString();
        boolean isSelf = playerName.equals(localName);

        // Kendi ismini gosterme kontrolu
        if (isSelf && !PavConfig.get().showOwnName) {
            ci.cancel();
            return;
        }

        // PM | prefix ekle - sunucudaki tum oyunculara
        boolean isPavUser = PavClientUsers.isPavUser(playerName);
        if (isPavUser || isSelf) {
            ci.cancel();

            MutableText prefix = Text.literal("PM")
                    .formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal(" | ").formatted(Formatting.GRAY));
            MutableText name = Text.literal(playerName).formatted(Formatting.WHITE);
            MutableText fullText = prefix.append(name);

            // Nametag'i PM | prefix ile renderla
            pavclient$renderNameTag(state, fullText, matrices, vertexConsumers, light);
        }
    }

    @Unique
    private void pavclient$renderNameTag(EntityRenderState state, Text text, MatrixStack matrices,
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

        // Arka plan (see-through)
        textRenderer.draw(text, x, 0, 0x20FFFFFF, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, bgColor, light);
        // On plan
        textRenderer.draw(text, x, 0, -1, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, light);

        matrices.pop();
    }
}
