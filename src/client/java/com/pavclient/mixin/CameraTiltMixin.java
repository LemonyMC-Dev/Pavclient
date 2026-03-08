package com.pavclient.mixin;

import com.pavclient.config.PavConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gercekci Hareketler: Yururken kamera hafif saga-sola sallanir,
 * sprint ederken hafif one egilir.
 */
@Mixin(GameRenderer.class)
public class CameraTiltMixin {

    @Unique private float pavclient$tiltAngle = 0f;
    @Unique private float pavclient$sprintLean = 0f;

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void pavclient$applyRealisticMotion(CallbackInfo ci) {
        if (!PavConfig.get().realisticAnimations) {
            pavclient$tiltAngle = 0f;
            pavclient$sprintLean = 0f;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        // Yurume sallantisi
        float limbSwing = mc.player.limbAnimator.getPos();
        float limbSwingAmount = mc.player.limbAnimator.getSpeed(mc.getRenderTickCounter().getTickDelta(true));
        float targetTilt = (float) Math.sin(limbSwing * 0.6f) * limbSwingAmount * 1.5f;
        pavclient$tiltAngle += (targetTilt - pavclient$tiltAngle) * 0.15f;

        // Sprint one egilme
        float targetLean = mc.player.isSprinting() ? 1.2f : 0f;
        pavclient$sprintLean += (targetLean - pavclient$sprintLean) * 0.08f;

        // Kameraya uygula
        MatrixStack matrices = new MatrixStack();
        // Tilt ve lean degerlerini GameRenderer'a disaridan uygulamak zor,
        // bunun yerine bobView icinde yapacagiz
    }

    @Inject(method = "bobView", at = @At("TAIL"))
    private void pavclient$addExtraBob(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!PavConfig.get().realisticAnimations) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        float limbSwing = mc.player.limbAnimator.getPos();
        float limbAmount = mc.player.limbAnimator.getSpeed(tickDelta);

        // Saga sola tilt (yurume)
        float tilt = (float) Math.sin(limbSwing * 0.6f) * limbAmount * 1.8f;
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(tilt));

        // Sprint one egilme
        if (mc.player.isSprinting()) {
            float lean = limbAmount * 0.8f;
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(lean));
        }
    }
}
