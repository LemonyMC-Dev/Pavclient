package com.pavclient.mixin;

import com.pavclient.gui.GuiHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mojang splash -> PavMC branded splash.
 * Gerçek reload progress'i gösterir.
 * Fade-out sırasında alpha'yı korur böylece overlay düzgün kapanır.
 */
@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Shadow @Final private ResourceReload reload;
    @Shadow private float progress;
    @Shadow private long reloadCompleteTime;
    @Shadow private long reloadStartTime;

    @Unique private static final long FADE_IN_MS = 500L;
    @Unique private static final long FADE_OUT_MS = 400L;

    @Inject(method = "render", at = @At("TAIL"))
    private void pavclient$drawOverMojang(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        long now = System.currentTimeMillis();

        // Alpha hesapla: fade-in ve fade-out korunacak
        float alpha = 1.0f;

        if (this.reloadCompleteTime > -1L) {
            // Yükleme bitti, fade-out
            float elapsed = (float)(now - this.reloadCompleteTime);
            alpha = 1.0f - Math.min(elapsed / FADE_OUT_MS, 1.0f);
        } else if (this.reloadStartTime > -1L) {
            // Fade-in
            float elapsed = (float)(now - this.reloadStartTime);
            alpha = Math.min(elapsed / FADE_IN_MS, 1.0f);
        }

        if (alpha <= 0.01f) return; // Tamamen şeffaf, çizme

        int alphaInt = (int)(alpha * 255) & 0xFF;
        int bgColor = (alphaInt << 24) | 0x0A0A14;

        // Mojang'ın her şeyini kapat
        context.fill(0, 0, w, h, bgColor);

        if (alpha < 0.1f) return; // Çok düşükse text çizme

        // Mor glow
        int glowAlpha = (int)(alpha * 0x22) & 0xFF;
        int glowColor = (glowAlpha << 24) | 0x7C4DFF;
        int glowX = w / 2 - 150;
        context.fillGradient(glowX, h / 2 - 80, glowX + 300, h / 2 + 80, glowColor, 0x00000000);

        // PavMC - büyük, scale 3x
        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
        // Alpha'yı rgb'ye uygula
        int textAlpha = Math.max(alphaInt, 4);
        rgb = (textAlpha << 24) | (rgb & 0x00FFFFFF);

        MatrixStack mat = context.getMatrices();
        mat.push();
        float scale = 3.0f;
        mat.scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("PavMC"), (int)(w / 2 / scale), (int)((h / 2 - 20) / scale), rgb);
        mat.pop();

        // Alt yazı
        int subColor = (textAlpha << 24) | 0x9E9E9E;
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("Y\u00fckleniyor..."), w / 2, h / 2 + 8, subColor);

        // Gerçek progress bar
        float realProgress = this.progress;
        int barW = 200;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 28;
        int barBg = ((int)(alpha * 0x33) << 24) | 0xFFFFFF;
        context.fill(barX, barY, barX + barW, barY + 3, barBg);

        int fillW = (int)(barW * realProgress);
        if (fillW > 0) {
            int barFg = (textAlpha << 24) | (rgb & 0x00FFFFFF);
            context.fill(barX, barY, barX + fillW, barY + 3, barFg);
        }

        // Yüzde göster
        int pctColor = (textAlpha << 24) | 0x666666;
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal((int)(realProgress * 100) + "%"), w / 2, barY + 8, pctColor);
    }
}
