package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Modern 4 sekmeli Client Ayarlari.
 * Sayfalar: Gorunum | HUD | Bilgi
 */
public class ClientSettingsScreen extends Screen {

    private final Screen parent;
    private int currentPage = 0;
    private static final String[] PAGES = {"G\u00f6r\u00fcn\u00fcm", "HUD", "Bilgi"};

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal("PavClient Ayarlar\u0131"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int bw = 240;
        int bh = 24;
        int gap = 28;
        int y0 = 75;

        // Yatay tab bar
        int tabW = 62;
        int totalTabW = tabW * PAGES.length;
        int tabStartX = cx - totalTabW / 2;
        for (int i = 0; i < PAGES.length; i++) {
            final int page = i;
            ModernButtonWidget tab;
            if (i == currentPage) {
                tab = ModernButtonWidget.success(tabStartX + i * tabW, 42, tabW - 2, 20,
                        Text.literal(PAGES[i]), btn -> {});
            } else {
                tab = ModernButtonWidget.create(tabStartX + i * tabW, 42, tabW - 2, 20,
                        Text.literal(PAGES[i]), btn -> { currentPage = page; init(); });
            }
            this.addDrawableChild(tab);
        }

        PavConfig cfg = PavConfig.get();

        switch (currentPage) {
            case 0 -> initPageGorunum(cx, y0, bw, bh, gap, cfg);
            case 1 -> initPageHud(cx, y0, bw, bh, gap, cfg);
            case 2 -> initPageBilgi(cx, y0, bw, bh, gap);
        }

        // Geri butonu
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 35, bw, bh,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    /** Sayfa 0: Gorunum */
    private void initPageGorunum(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        addToggle(cx, y, w, h, "\u00d6zel Ni\u015fan", cfg.customCrosshairEnabled,
                btn -> { cfg.customCrosshairEnabled = !cfg.customCrosshairEnabled;
                    btn.setMessage(toggleText("\u00d6zel Ni\u015fan", cfg.customCrosshairEnabled)); PavConfig.save(); });

        String[] stiller = {"Art\u0131", "Nokta", "Daire", "\u0130nce Ha\u00e7"};
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length]),
                btn -> { cfg.crosshairStyle = (cfg.crosshairStyle + 1) % stiller.length;
                    btn.setMessage(Text.literal("Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length])); PavConfig.save(); }));

        int rowY = y + gap * 2;
        int toggleW = w - 34;
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, rowY, toggleW, h,
                toggleText("Blok Vurgulama", cfg.blockHighlight),
                btn -> {
                    cfg.blockHighlight = !cfg.blockHighlight;
                    btn.setMessage(toggleText("Blok Vurgulama", cfg.blockHighlight));
                    PavConfig.save();
                }));
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2 + toggleW + 4, rowY, 30, h,
                Text.literal("..."),
                btn -> { if (this.client != null) this.client.setScreen(new ModernBlockHighlightScreen(this)); }));

        addToggle(cx, y + gap * 3, w, h, "Kendi \u0130smini G\u00f6ster", cfg.showOwnName,
                btn -> { cfg.showOwnName = !cfg.showOwnName;
                    btn.setMessage(toggleText("Kendi \u0130smini G\u00f6ster", cfg.showOwnName)); PavConfig.save(); });

        addToggle(cx, y + gap * 4, w, h, "Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations,
                btn -> { cfg.realisticAnimations = !cfg.realisticAnimations;
                    btn.setMessage(toggleText("Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations)); PavConfig.save(); });
    }

    /** Sayfa 1: HUD */
    private void initPageHud(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        addToggle(cx, y, w, h, "RGB Yaz\u0131", cfg.rgbTextEnabled,
                btn -> { cfg.rgbTextEnabled = !cfg.rgbTextEnabled;
                    btn.setMessage(toggleText("RGB Yaz\u0131", cfg.rgbTextEnabled)); PavConfig.save(); });

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("RGB Boyut: " + String.format("%.1fx", cfg.rgbScale)),
                btn -> { cfg.rgbScale += 0.5f; if (cfg.rgbScale > 4.0f) cfg.rgbScale = 1.0f;
                    btn.setMessage(Text.literal("RGB Boyut: " + String.format("%.1fx", cfg.rgbScale))); PavConfig.save(); }));

        addToggle(cx, y + gap * 2, w, h, "Z\u0131rh G\u00f6stergesi", cfg.armorHudEnabled,
                btn -> { cfg.armorHudEnabled = !cfg.armorHudEnabled;
                    btn.setMessage(toggleText("Z\u0131rh G\u00f6stergesi", cfg.armorHudEnabled)); PavConfig.save(); });

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 3, w, h,
                Text.literal("Z\u0131rh Boyut: " + String.format("%.1fx", cfg.armorHudScale)),
                btn -> { cfg.armorHudScale += 0.25f; if (cfg.armorHudScale > 3.0f) cfg.armorHudScale = 0.5f;
                    btn.setMessage(Text.literal("Z\u0131rh Boyut: " + String.format("%.1fx", cfg.armorHudScale))); PavConfig.save(); }));

        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y + gap * 4 + 8, w, h,
                Text.literal("\u270e D\u00fczenle"),
                btn -> { if (this.client != null) this.client.setScreen(new HudEditScreen(this)); }));
    }

    /** Sayfa 2: Bilgi */
    private void initPageBilgi(int cx, int y, int w, int h, int gap) {
        // Discord
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, w, h,
                Text.literal("\u00a7bDiscord: \u00a7fdiscord.gg/pavmc"),
                btn -> {}));

        // YouTube
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("\u00a7cYouTube: \u00a7fPavMC"),
                btn -> {}));

        // Cizgi ayirici (bos buton)
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 2, w, h,
                Text.literal("\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"),
                btn -> {}));

        // Sunucu
        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y + gap * 3, w, h,
                Text.literal("\u25c6 " + PavClient.TARGET_SERVER),
                btn -> {}));

        // Developer
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 4 + 8, w, h,
                Text.literal("\u00a77Developed by \u00a7fLemonyMC"),
                btn -> {}));

        // Version
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 5 + 8, w, h,
                Text.literal("\u00a78v" + PavClient.CLIENT_VERSION),
                btn -> {}));
    }

    private void addToggle(int cx, int y, int w, int h, String label, boolean value, ButtonWidget.PressAction action) {
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, w, h, toggleText(label, value), action));
    }

    private Text toggleText(String label, boolean value) {
        return Text.literal(label + ": " + (value ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;

        GuiHelper.drawPanel(context, cx - 145, 18, 290, this.height - 36);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25c6 PavClient Ayarlar\u0131 \u25c6"), cx, 26, 0xFFB0BEC5);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
