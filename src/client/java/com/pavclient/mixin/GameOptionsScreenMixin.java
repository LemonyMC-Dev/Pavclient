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
 * Tum GameOptionsScreen alt siniflarini (Ses, Video, Kontroller, Sohbet, Skin, Erisilebilirlik)
 * modern PavClient GUI ile gosterir.
 * - Arka plan: PavClient branded
 * - Footer: Modern "Bitti" butonu
 * - OptionListWidget korunur (ayar slider/toggle'lari)
 */
@Mixin(GameOptionsScreen.class)
public abstract class GameOptionsScreenMixin extends Screen {

    @Shadow protected OptionListWidget body;

    protected GameOptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$modernizeFooter(CallbackInfo ci) {
        // Vanilla "Done" butonlarini kaldir ve modern olanla degistir
        List<Element> toRemove = new ArrayList<>();
        for (Element child : this.children()) {
            if (child instanceof ButtonWidget bw && !(child instanceof ModernButtonWidget)) {
                String msg = bw.getMessage().getString().toLowerCase();
                if (msg.contains("done") || msg.contains("bitti") || msg.contains("tamam")) {
                    toRemove.add(child);
                }
            }
        }
        for (Element e : toRemove) {
            if (e instanceof ClickableWidget w) this.remove(w);
        }

        // Modern "Bitti" butonu
        int cx = this.width / 2;
        this.addDrawableChild(ModernButtonWidget.create(
                cx - 110, this.height - 32, 220, 22,
                Text.literal("\u2190 Bitti"),
                btn -> this.close()
        ));

        // OptionListWidget'in boyutunu ayarla (ust bosluk baslik icin, alt bosluk buton icin)
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

        // Baslik
        context.drawCenteredTextWithShadow(this.textRenderer,
                this.title, cx, 14, 0xFFB0BEC5);

        // Butonlari renderla
        for (var child : this.children()) {
            if (child instanceof ClickableWidget w && !(child instanceof OptionListWidget)) {
                w.render(context, mouseX, mouseY, delta);
            }
        }
    }
}
