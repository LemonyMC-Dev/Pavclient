package com.pavclient.mixin;

import com.pavclient.PavClientUsers;
import com.pavclient.config.PavConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametag mixin - PlayerEntityRenderer:
 * - Kendi ismini goster toggle
 * - PavClient kullanicilarina ρм | prefix ekle
 * - Text'i degistirip vanilla renderlamaya birak
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerNametagMixin {

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void pavclient$modifyNametag(PlayerEntityRenderState state, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        String rawName = (state.name != null && !state.name.isEmpty()) ? state.name : text.getString();
        boolean isSelf = state.id == mc.player.getId();
        AbstractClientPlayerEntity renderedPlayer = null;
        if (mc.world != null) {
            var entity = mc.world.getEntityById(state.id);
            if (entity instanceof AbstractClientPlayerEntity p) {
                renderedPlayer = p;
            }
        }
        java.util.UUID renderedUuid = renderedPlayer != null ? renderedPlayer.getUuid() : null;

        // Kendi ismini gosterme kapali ise cancel
        if (isSelf && !PavConfig.get().showOwnName) {
            ci.cancel();
            return;
        }

        // PavClient kullanicilarina \u03c1\u043c | prefix ekle
        if (isSelf || PavClientUsers.isPavUser(renderedUuid, rawName)) {
            ci.cancel();

            // \u03c1\u043c | prefix (yunan rho + kiril em = hos gorunum)
            MutableText prefix = Text.literal("\u03c1\u043c")
                    .setStyle(Style.EMPTY.withColor(0xCB7BEA))
                    .append(Text.literal(" | ").formatted(Formatting.GRAY));
            MutableText fullText = prefix.append(text.copy());

            // Vanilla renderlamaya birak ama degistirilmis text ile - EntityRenderer bridge metodunu cagir
            // PlayerEntityRenderer -> super (LivingEntityRenderer -> EntityRenderer) renderLabelIfPresent
            // Bridge method (EntityRenderState parametreli) uzerinden cagirmamiz lazim
            // Ama en guvenlisi: dogrudan EntityRenderer'in renderLabelIfPresent'ini cagirmak
            pavclient$renderVanillaLabel(state, fullText, matrices, vertexConsumers, light);
        }
    }

    /**
     * Vanilla nametag renderlamasi - EntityRenderer.renderLabelIfPresent ile ayni mantik.
     */
    private void pavclient$renderVanillaLabel(PlayerEntityRenderState state, Text text,
                                               MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        var textRenderer = mc.textRenderer;

        matrices.push();
        matrices.translate(0.0f, state.height + 0.5f, 0.0f);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        float scale = 0.025f;
        matrices.scale(scale, -scale, scale);

        float halfWidth = (float)(-textRenderer.getWidth(text) / 2);
        int bgAlpha = (int)(mc.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;

        // See-through layer (arka plan dahil)
        textRenderer.draw(text, halfWidth, 0.0f, 0x20FFFFFF, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH,
                bgAlpha, light);

        // Normal layer
        textRenderer.draw(text, halfWidth, 0.0f, -1, false,
                matrices.peek().getPositionMatrix(), vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0, light);

        matrices.pop();
    }
}
