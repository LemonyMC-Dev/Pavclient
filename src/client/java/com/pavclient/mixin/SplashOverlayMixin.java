package com.pavclient.mixin;

import com.pavclient.gui.GuiHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mojang splash'ı gizler: render SONRASI üzerine opak PavMC çizer.
 * Resource loading arka planda devam eder, Mojang hiç görünmez.
 */
@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void pavclient$drawOverMojang(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        // Mojang'ın çizdiği her şeyin üzerine tam opak arkaplan
        context.fill(0, 0, w, h, 0xFF0A0A14);

        // Mor glow
        int glowX = w / 2 - 120;
        GuiHelper.drawVerticalGradient(context, glowX, h / 2 - 60, glowX + 240, h / 2 + 60, 0x227C4DFF, 0x00000000);

        // Rainbow PavMC
        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);

        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("PavMC"), w / 2, h / 2 - 10, rgb);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("Y\u00fckleniyor..."), w / 2, h / 2 + 6, 0xFF9E9E9E);

        // Progress bar
        float progress = (ms % 2000) / 2000.0f;
        int barW = 200;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 25;
        context.fill(barX, barY, barX + barW, barY + 3, 0x33FFFFFF);
        context.fill(barX, barY, barX + (int)(barW * progress), barY + 3, rgb);
    }
}
