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
 * Mojang splash uzerine PavMC branded overlay cizer.
 * Vanilla renderdan SONRA calisir ve ustune opak arka plan + PavMC yazar.
 */
@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Shadow @Final private ResourceReload reload;
    @Shadow private float progress;
    @Shadow private long reloadCompleteTime;
    @Shadow private long reloadStartTime;

    @Unique private static final long FADE_OUT_MS = 400L;

    @Inject(method = "render", at = @At("TAIL"))
    private void pavclient$drawOverMojang(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        // Fade-out hesapla (splash bittiginde yavasce kaybolsun)
        float alpha = 1.0f;
        if (this.reloadCompleteTime > -1L) {
            float elapsed = (float)(System.currentTimeMillis() - this.reloadCompleteTime);
            alpha = 1.0f - Math.min(elapsed / FADE_OUT_MS, 1.0f);
            if (alpha <= 0.01f) return;
        }

        int alphaInt = Math.max((int)(alpha * 255), 4);

        // Mojang logosunu kapat - ustune koyu arka plan
        context.fill(0, 0, w, h, (alphaInt << 24) | 0x0A0A14);

        // Mor glow
        int glowColor = (Math.max((int)(alpha * 34), 1) << 24) | 0x7C4DFF;
        context.fillGradient(w / 2 - 150, h / 2 - 80, w / 2 + 150, h / 2 + 80, glowColor, 0x00000000);

        // PavMC - rainbow, 3x buyuk
        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = (alphaInt << 24) | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);

        MatrixStack mat = context.getMatrices();
        mat.push();
        mat.scale(3.0f, 3.0f, 1.0f);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("PavMC"), (int)(w / 2 / 3.0f), (int)((h / 2 - 20) / 3.0f), rgb);
        mat.pop();

        // "Yukleniyor..." yazisi
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("Y\u00fckleniyor..."), w / 2, h / 2 + 8, (alphaInt << 24) | 0x9E9E9E);

        // Progress bar
        float realProgress = this.progress;
        int barW = 200;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 28;
        context.fill(barX, barY, barX + barW, barY + 3, (Math.max((int)(alpha * 51), 1) << 24) | 0xFFFFFF);
        int fillW = (int)(barW * realProgress);
        if (fillW > 0) {
            context.fill(barX, barY, barX + fillW, barY + 3, rgb);
        }

        // Yuzde
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal((int)(realProgress * 100) + "%"), w / 2, barY + 8, (alphaInt << 24) | 0x666666);
    }
}
