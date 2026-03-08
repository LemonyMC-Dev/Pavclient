package com.pavclient.mixin;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Tum GameOptionsScreen alt siniflarini modern PavClient GUI ile gosterir.
 * Ses, Video, Kontroller, Sohbet, Skin, Erisilebilirlik - hepsi etkilenir.
 * - Arka plan: PavClient branded
 * - TUM vanilla ButtonWidget'lar ModernButtonWidget ile degistirilir
 * - OptionListWidget korunur (slider/toggle'lar)
 */
@Mixin(GameOptionsScreen.class)
public abstract class GameOptionsScreenMixin extends Screen {

    @Shadow protected OptionListWidget body;

    protected GameOptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$modernizeAll(CallbackInfo ci) {
        int cx = this.width / 2;

        // Tum vanilla ButtonWidget'lari bul ve ModernButtonWidget ile degistir
        List<ButtonWidget> vanillaButtons = new ArrayList<>();
        for (Element child : this.children()) {
            if (child instanceof ButtonWidget bw && !(child instanceof ModernButtonWidget)) {
                vanillaButtons.add(bw);
            }
        }

        for (ButtonWidget vb : vanillaButtons) {
            this.remove(vb);

            Text msg = vb.getMessage();
            int x = vb.getX();
            int y = vb.getY();
            int w = vb.getWidth();
            int h = vb.getHeight();

            // Done/Bitti butonu icin ozel konum
            String msgStr = msg.getString().toLowerCase();
            if (msgStr.contains("done") || msgStr.contains("bitti") || msgStr.contains("tamam")) {
                this.addDrawableChild(ModernButtonWidget.create(
                        cx - 110, this.height - 32, 220, 22,
                        Text.literal("\u2190 Bitti"),
                        btn -> this.close()
                ));
            } else {
                // Diger butonlari ayni pozisyonda modern yap
                // onPress'i koruyarak yeni buton olustur
                final ButtonWidget.PressAction action = btn -> vb.onPress();
                this.addDrawableChild(ModernButtonWidget.create(x, y, w, Math.min(h, 22), msg, action));
            }
        }

        // OptionListWidget boyutunu ayarla
        if (this.body != null) {
            this.body.setDimensionsAndPosition(this.width, this.height - 76, 0, 38);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Modern arka plan
        GuiHelper.drawClientBackground(context, this.width, this.height);

        // OptionListWidget renderla
        if (this.body != null) {
            this.body.render(context, mouseX, mouseY, delta);
        }

        // Panel baslik alani
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 145, 4, 290, 30);

        // Baslik - soft pastel
        context.drawCenteredTextWithShadow(this.textRenderer,
                this.title, cx, 14, 0xFFB0BEC5);

        // Butonlari renderla (OptionListWidget haric)
        for (var child : this.children()) {
            if (child instanceof ClickableWidget w && !(child instanceof OptionListWidget)) {
                w.render(context, mouseX, mouseY, delta);
            }
        }
    }
}
