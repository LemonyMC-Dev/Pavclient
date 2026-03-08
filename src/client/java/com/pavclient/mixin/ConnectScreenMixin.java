package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Ba\u011flan\u0131rken ekran\u0131: t\u00fcm butonlar\u0131 kald\u0131r, custom render.
 * \u0130ptal butonu dahil hi\u00e7bir buton g\u00f6r\u00fcnmez.
 */
@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {

    protected ConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$removeButtons(CallbackInfo ci) {
        List<Element> toRemove = new ArrayList<>(this.children());
        for (Element child : toRemove) {
            if (child instanceof ClickableWidget w) {
                this.remove(w);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void pavclient$drawOverConnectScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // \u00dczerine opak arkaplan \u00e7iz (vanilla render'\u0131 gizle)
        GuiHelper.drawClientBackground(context, w, h);

        MinecraftClient mc = MinecraftClient.getInstance();

        // Rainbow PavMC
        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);

        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("\u25C6 PavMC \u25C6"), w / 2, h / 2 - 25, rgb);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("Sunucuya ba\u011flan\u0131l\u0131yor..."), w / 2, h / 2 - 8, 0xFFFFFFFF);

        // Animasyonlu noktalar
        int dots = (int)((ms / 400) % 4);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal(PavClient.TARGET_SERVER + ".".repeat(dots)), w / 2, h / 2 + 8, 0xFF9E9E9E);

        // Progress bar
        float progress = (ms % 1500) / 1500.0f;
        int barW = 160;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 28;
        context.fill(barX, barY, barX + barW, barY + 2, 0x33FFFFFF);
        context.fill(barX, barY, barX + (int)(barW * progress), barY + 2, rgb);
    }
}
