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
 * Yatay sayfalı, toggle'lı modern Client Ayarları.
 * Sayfalar: G\u00f6r\u00fcn\u00fcm | \u00d6zellikler | HUD
 */
public class ClientSettingsScreen extends Screen {

    private final Screen parent;
    private int currentPage = 0;
    private static final String[] PAGES = {"G\u00f6r\u00fcn\u00fcm", "\u00d6zellikler", "HUD"};

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

        // Sayfa sekmeleri (yatay tab bar)
        int tabW = 76;
        int tabStartX = cx - (tabW * PAGES.length) / 2;
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
            case 1 -> initPageOzellikler(cx, y0, bw, bh, gap, cfg);
            case 2 -> initPageHud(cx, y0, bw, bh, gap, cfg);
        }

        // Geri butonu (her sayfada)
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 35, bw, bh,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    /** Sayfa 0: G\u00f6r\u00fcn\u00fcm */
    private void initPageGorunum(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        // Custom Crosshair
        addToggle(cx, y, w, h, "\u00d6zel Ni\u015fan", cfg.customCrosshairEnabled,
                btn -> { cfg.customCrosshairEnabled = !cfg.customCrosshairEnabled;
                    btn.setMessage(toggleText("\u00d6zel Ni\u015fan", cfg.customCrosshairEnabled)); PavConfig.save(); });

        // Crosshair stili
        String[] stiller = {"Art\u0131", "Nokta", "Daire", "\u0130nce Ha\u00e7"};
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length]),
                btn -> { cfg.crosshairStyle = (cfg.crosshairStyle + 1) % stiller.length;
                    btn.setMessage(Text.literal("Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length])); PavConfig.save(); }));

        // Blok vurgulama
        addToggle(cx, y + gap * 2, w, h, "Blok Vurgulama", cfg.blockHighlight,
                btn -> { cfg.blockHighlight = !cfg.blockHighlight;
                    btn.setMessage(toggleText("Blok Vurgulama", cfg.blockHighlight)); PavConfig.save(); });

        // Kendi ismini g\u00f6sterme
        addToggle(cx, y + gap * 3, w, h, "Kendi \u0130smini G\u00f6ster", cfg.showOwnName,
                btn -> { cfg.showOwnName = !cfg.showOwnName;
                    btn.setMessage(toggleText("Kendi \u0130smini G\u00f6ster", cfg.showOwnName)); PavConfig.save(); });

        // Ger\u00e7ek\u00e7i hareketler
        addToggle(cx, y + gap * 4, w, h, "Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations,
                btn -> { cfg.realisticAnimations = !cfg.realisticAnimations;
                    btn.setMessage(toggleText("Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations)); PavConfig.save(); });
    }

    /** Sayfa 1: \u00d6zellikler */
    private void initPageOzellikler(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        // Optimizasyon durumu
        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y, w, h,
                Text.literal("\u2714 Lithium + FerriteCore Aktif"), btn -> {}));

        // Shader bilgi
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("Shader: Mod Men\u00fc \u00fczerinden y\u00fcklenebilir"),
                btn -> {}));

        // ViaFabricPlus bilgi
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 2, w, h,
                Text.literal("\u2714 \u00c7oklu Versiyon: 1.8 - 1.21.4"),
                btn -> {}));

        // Dans bilgi (emote)
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 3, w, h,
                Text.literal("Dans/Emote: Yak\u0131nda..."),
                btn -> {}));

        // Sunucu bilgi
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 4, w, h,
                Text.literal("Sunucu: " + PavClient.TARGET_SERVER),
                btn -> {}));
    }

    /** Sayfa 2: HUD */
    private void initPageHud(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        // RGB Yaz\u0131
        addToggle(cx, y, w, h, "RGB Yaz\u0131", cfg.rgbTextEnabled,
                btn -> { cfg.rgbTextEnabled = !cfg.rgbTextEnabled;
                    btn.setMessage(toggleText("RGB Yaz\u0131", cfg.rgbTextEnabled)); PavConfig.save(); });

        // RGB Boyut
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("RGB Boyut: " + String.format("%.1fx", cfg.rgbScale)),
                btn -> { cfg.rgbScale += 0.5f; if (cfg.rgbScale > 4.0f) cfg.rgbScale = 1.0f;
                    btn.setMessage(Text.literal("RGB Boyut: " + String.format("%.1fx", cfg.rgbScale))); PavConfig.save(); }));

        // Z\u0131rh G\u00f6stergesi
        addToggle(cx, y + gap * 2, w, h, "Z\u0131rh G\u00f6stergesi", cfg.armorHudEnabled,
                btn -> { cfg.armorHudEnabled = !cfg.armorHudEnabled;
                    btn.setMessage(toggleText("Z\u0131rh G\u00f6stergesi", cfg.armorHudEnabled)); PavConfig.save(); });

        // Z\u0131rh Boyut
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 3, w, h,
                Text.literal("Z\u0131rh Boyut: " + String.format("%.1fx", cfg.armorHudScale)),
                btn -> { cfg.armorHudScale += 0.25f; if (cfg.armorHudScale > 3.0f) cfg.armorHudScale = 0.5f;
                    btn.setMessage(Text.literal("Z\u0131rh Boyut: " + String.format("%.1fx", cfg.armorHudScale))); PavConfig.save(); }));

        // Z\u0131rh Pozisyon
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 4, w, h,
                Text.literal("Z\u0131rh Pozisyon: " + (cfg.armorHudAnchorBottom ? "Sol Alt" : "Sol \u00dcst")),
                btn -> { cfg.armorHudAnchorBottom = !cfg.armorHudAnchorBottom;
                    btn.setMessage(Text.literal("Z\u0131rh Pozisyon: " + (cfg.armorHudAnchorBottom ? "Sol Alt" : "Sol \u00dcst"))); PavConfig.save(); }));

        // Ekran\u0131 D\u00fczenle butonu
        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y + gap * 5 + 8, w, h,
                Text.literal("\u270E Ekran\u0131 D\u00fczenle"),
                btn -> { if (this.client != null) this.client.setScreen(new HudEditScreen(this)); }));
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

        // Rainbow title
        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavClient Ayarlar\u0131 \u25C6"), cx, 26, rgb);

        // Sayfa g\u00f6stergesi
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal((currentPage + 1) + "/" + PAGES.length + " \u2022 \u25C0 \u25B6 ile gezin"),
                cx, this.height - 48, 0x55FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
