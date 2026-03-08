package com.pavclient.mixin;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.screen.ModernAccessibilityScreen;
import com.pavclient.screen.ModernChatScreen;
import com.pavclient.screen.ModernControlsScreen;
import com.pavclient.screen.ModernLanguageScreen;
import com.pavclient.screen.ModernResourcePackScreen;
import com.pavclient.screen.ModernSkinScreen;
import com.pavclient.screen.ModernSoundScreen;
import com.pavclient.screen.ModernVideoScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.*;
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
 * Vanilla Ayarlar ekrani - modern PavClient GUI.
 * Kaldirilan: Olcum Verileri, Katkida Bulunanlar, Cevrimici
 * Dil: Modern sadece TR/EN ekranina yonlendirir
 * Kaynak Paketleri: kaynakpaketleri klasorune yonlendirir
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
                btn -> { if (this.client != null) this.client.setScreen(new ModernSoundScreen(this, this.settings)); }
        ));

        // Video Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap, bw, bh,
                Text.literal("\u25a3 Video Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernVideoScreen(this, this.settings)); }
        ));

        // Kontroller
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 2, bw, bh,
                Text.literal("\u2328 Kontroller"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernControlsScreen(this, this.settings)); }
        ));

        // Dil - Modern sadece TR/EN ekrani
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 3, bw, bh,
                Text.literal("\u2603 Dil"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernLanguageScreen(this, this.client.getLanguageManager())); }
        ));

        // Sohbet Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 4, bw, bh,
                Text.literal("\u2709 Sohbet Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernChatScreen(this, this.settings)); }
        ));

        // Kaynak Paketleri - kaynakpaketleri klasoru
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 5, bw, bh,
                Text.literal("\u2261 Kaynak Paketleri"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernResourcePackScreen(this)); }
        ));

        // Erisilebilirlik
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 6, bw, bh,
                Text.literal("\u2665 Eri\u015filebilirlik"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernAccessibilityScreen(this, this.settings)); }
        ));

        // Gorunum Ozellestirme
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 7, bw, bh,
                Text.literal("\u263a G\u00f6r\u00fcn\u00fcm \u00d6zelle\u015ftirme"),
                btn -> { if (this.client != null) this.client.setScreen(new ModernSkinScreen(this, this.settings)); }
        ));

        // Geri
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 32, bw, bh,
                Text.literal("\u2190 Bitti"),
                btn -> this.close()
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 14, 260, this.height - 28);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u2699 Ayarlar"), cx, 22, 0xFFB0BEC5);

        for (var child : this.children()) {
            if (child instanceof ClickableWidget w) {
                w.render(context, mouseX, mouseY, delta);
            }
        }
    }
}
