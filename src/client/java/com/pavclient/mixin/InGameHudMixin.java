package com.pavclient.mixin;

import com.pavclient.config.PavConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla crosshair'ı gizle (custom crosshair açıkken üst üste binmesin).
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void pavclient$hideCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (PavConfig.get().customCrosshairEnabled) {
            ci.cancel(); // Vanilla crosshair'ı tamamen engelle
        }
    }
}
