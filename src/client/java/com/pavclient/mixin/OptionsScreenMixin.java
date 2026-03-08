package com.pavclient.mixin;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla Ayarlar ekranini modern PavClient GUI ile yeniden yapar.
 * Kaldirilan butonlar: Olcum Verileri, Katkida Bulunanlar, Cevrimici
 */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    @Shadow @Final private Screen parent;
    @Shadow @Final private net.minecraft.client.option.GameOptions settings;

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$replaceWithModernButtons(CallbackInfo ci) {
        // Tum mevcut butonlari kaldir
        List<Element> toRemove = new ArrayList<>(this.children());
        for (Element child : toRemove) {
            if (child instanceof ClickableWidget w) {
                this.remove(w);
            }
        }

        int cx = this.width / 2;
        int bw = 220;
        int bh = 22;
        int gap = 26;
        int startY = 42;

        // Ses Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY, bw, bh,
                Text.literal("\u266b Ses Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new SoundOptionsScreen(this, this.settings)); }
        ));

        // Video Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap, bw, bh,
                Text.literal("\u25a3 Video Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new VideoOptionsScreen(this, this.client, this.settings)); }
        ));

        // Kontroller
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 2, bw, bh,
                Text.literal("\u2328 Kontroller"),
                btn -> { if (this.client != null) this.client.setScreen(new ControlsOptionsScreen(this, this.settings)); }
        ));

        // Dil
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 3, bw, bh,
                Text.literal("\u2603 Dil"),
                btn -> { if (this.client != null) this.client.setScreen(new LanguageOptionsScreen(this, this.settings, this.client.getLanguageManager())); }
        ));

        // Sohbet Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 4, bw, bh,
                Text.literal("\u2709 Sohbet Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new ChatOptionsScreen(this, this.settings)); }
        ));

        // Kaynak Paketleri
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 5, bw, bh,
                Text.literal("\u2261 Kaynak Paketleri"),
                btn -> { if (this.client != null) this.client.setScreen(new PackScreen(
                        this.client.getResourcePackManager(), this::pavclient$refreshPacks, this.client.getResourcePackDir(), Text.translatable("resourcePack.title"))); }
        ));

        // Erisilebilirlik
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 6, bw, bh,
                Text.literal("\u2665 Eri\u015filebilirlik"),
                btn -> { if (this.client != null) this.client.setScreen(new AccessibilityOptionsScreen(this, this.settings)); }
        ));

        // Gorunum Ozellestirme
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 7, bw, bh,
                Text.literal("\u263a G\u00f6r\u00fcn\u00fcm \u00d6zelle\u015ftirme"),
                btn -> { if (this.client != null) this.client.setScreen(new SkinOptionsScreen(this, this.settings)); }
        ));

        // Geri (Bitti)
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 32, bw, bh,
                Text.literal("\u2190 Bitti"),
                btn -> this.close()
        ));
    }

    private void pavclient$refreshPacks(net.minecraft.resource.ResourcePackManager packManager) {
        if (this.client != null) {
            this.settings.refreshResourcePacks(packManager);
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void pavclient$clearLayout(CallbackInfo ci) {
        // Layout widget'in eklenmesine izin ver ama biz TAIL'de kaldirip kendi butonlarimizi koyacagiz
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Modern background
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 14, 260, this.height - 28);

        // Baslik
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u2699 Ayarlar"), cx, 22, 0xFFB0BEC5);

        // Butonlari renderla (super.render Screen sinifindan)
        for (var child : this.children()) {
            if (child instanceof ClickableWidget w) {
                w.render(context, mouseX, mouseY, delta);
            }
        }
    }
}
