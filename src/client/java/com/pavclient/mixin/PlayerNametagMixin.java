package com.pavclient.mixin;

import com.pavclient.config.PavConfig;
import com.pavclient.emote.EmoteManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Oyuncu nametag mixin:
 * - "Kendi ismini goster" toggle
 * - PavClient kullanan oyuncularin ismine "PM |" prefix
 * - Emote oynayan oyuncularin basinda emote ismi goster
 */
@Mixin(EntityRenderer.class)
public class PlayerNametagMixin {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void pavclient$modifyNametag(EntityRenderState state, Text text, MatrixStack matrices,
                                          VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Kendi ismini gosterme kontrolu
        String playerName = text.getString();
        String localName = mc.player.getName().getString();

        if (playerName.equals(localName) && !PavConfig.get().showOwnName) {
            ci.cancel();
            return;
        }
    }
}
