package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.text.Text;

/**
 * Modern dil secim ekrani - sadece Turkce ve Ingilizce.
 */
public class ModernLanguageScreen extends Screen {

    private final Screen parent;
    private final LanguageManager languageManager;

    public ModernLanguageScreen(Screen parent, LanguageManager languageManager) {
        super(Text.literal("Dil Se\u00e7imi"));
        this.parent = parent;
        this.languageManager = languageManager;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int bw = 220;
        int bh = 28;
        int startY = 70;

        String currentLang = this.languageManager.getLanguage();

        // Turkce
        boolean isTr = currentLang.equals("tr_tr");
        this.addDrawableChild(isTr
                ? ModernButtonWidget.success(cx - bw / 2, startY, bw, bh,
                Text.literal("\u2714 T\u00fcrk\u00e7e (Se\u00e7ili)"), btn -> {})
                : ModernButtonWidget.create(cx - bw / 2, startY, bw, bh,
                Text.literal("T\u00fcrk\u00e7e"), btn -> {
                    this.languageManager.setLanguage("tr_tr");
                    if (this.client != null) {
                        this.client.options.language = "tr_tr";
                        this.client.reloadResources();
                        this.client.options.write();
                    }
                    init();
                }));

        // English
        boolean isEn = currentLang.equals("en_us");
        this.addDrawableChild(isEn
                ? ModernButtonWidget.success(cx - bw / 2, startY + 36, bw, bh,
                Text.literal("\u2714 English (Selected)"), btn -> {})
                : ModernButtonWidget.create(cx - bw / 2, startY + 36, bw, bh,
                Text.literal("English"), btn -> {
                    this.languageManager.setLanguage("en_us");
                    if (this.client != null) {
                        this.client.options.language = "en_us";
                        this.client.reloadResources();
                        this.client.options.write();
                    }
                    init();
                }));

        // Geri
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 35, bw, 24,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 18, 260, this.height - 36);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u2603 Dil Se\u00e7imi"), cx, 30, 0xFFB0BEC5);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a77De\u011fi\u015fiklik i\u00e7in oyun yeniden y\u00fcklenecektir"), cx, 50, 0xFF888888);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
