package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.screen.ConnectionFailedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Baglanirken ekrani: vanilla butonlari kaldir, custom render + iptal butonu.
 * Timeout 30 saniye sonra ConnectionFailedScreen'e yonlendirir.
 */
@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {

    @Unique
    private long pavclient$connectStartTime = -1;

    @Unique
    private static final long CONNECT_TIMEOUT_MS = 30_000L;

    protected ConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$replaceButtons(CallbackInfo ci) {
        // Vanilla butonlari kaldir
        List<Element> toRemove = new ArrayList<>(this.children());
        for (Element child : toRemove) {
            if (child instanceof ClickableWidget w) {
                this.remove(w);
            }
        }

        // Baslangic zamanini kaydet (timeout icin)
        if (pavclient$connectStartTime < 0) {
            pavclient$connectStartTime = System.currentTimeMillis();
        }

        // Iptal butonu ekle (ekranin alt kisminda)
        int bw = 160;
        this.addDrawableChild(ModernButtonWidget.danger(
                this.width / 2 - bw / 2, this.height / 2 + 50, bw, 24,
                Text.literal("\u2716 \u0130ptal"),
                btn -> {
                    if (this.client != null) {
                        this.client.disconnect();
                        this.client.setScreen(new ConnectionFailedScreen(
                                Text.literal("Ba\u011flant\u0131 iptal edildi.")));
                    }
                }
        ));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void pavclient$drawOverConnectScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // Uzerine opak arkaplan ciz (vanilla render'i gizle)
        GuiHelper.drawClientBackground(context, w, h);

        MinecraftClient mc = MinecraftClient.getInstance();

        // Timeout kontrolu
        if (pavclient$connectStartTime > 0) {
            long elapsed = System.currentTimeMillis() - pavclient$connectStartTime;
            if (elapsed > CONNECT_TIMEOUT_MS) {
                pavclient$connectStartTime = -1;
                mc.disconnect();
                mc.setScreen(new ConnectionFailedScreen(
                        Text.literal("Ba\u011flant\u0131 zaman a\u015f\u0131m\u0131na u\u011frad\u0131.")));
                return;
            }
        }

        long ms = System.nanoTime() / 1_000_000L;
        int rgb = 0xFFFFFFFF;

        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("\u25C6 PavMC \u25C6"), w / 2, h / 2 - 25, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("Sunucuya ba\u011flan\u0131l\u0131yor..."), w / 2, h / 2 - 8, 0xFFFFFFFF);

        // Animasyonlu noktalar
        int dots = (int)((ms / 400) % 4);
        context.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal(PavClient.TARGET_SERVER + ".".repeat(dots)), w / 2, h / 2 + 8, 0xFFFFFFFF);

        // Progress bar
        float progress = (ms % 1500) / 1500.0f;
        int barW = 160;
        int barX = w / 2 - barW / 2;
        int barY = h / 2 + 28;
        context.fill(barX, barY, barX + barW, barY + 2, 0x33FFFFFF);
        context.fill(barX, barY, barX + (int)(barW * progress), barY + 2, rgb);

        // Butonlari tekrar ciz (background uzerine cizdikten sonra)
        super.render(context, mouseX, mouseY, delta);
    }
}
