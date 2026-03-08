package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.config.PavConfig;
import com.pavclient.emote.EmoteManager;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Modern 4 sekmeli Client Ayarlari.
 * Sayfalar: Gorunum | Ozellikler | HUD | Dans
 */
public class ClientSettingsScreen extends Screen {

    private final Screen parent;
    private int currentPage = 0;
    private static final String[] PAGES = {"G\u00f6r\u00fcn\u00fcm", "\u00d6zellikler", "HUD", "Dans"};

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
        int tabW = 66;
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
            case 1 -> initPageOzellikler(cx, y0, bw, bh, gap, cfg);
            case 2 -> initPageHud(cx, y0, bw, bh, gap, cfg);
            case 3 -> initPageDans(cx, y0, bw, bh, gap);
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

        addToggle(cx, y + gap * 2, w, h, "Blok Vurgulama", cfg.blockHighlight,
                btn -> { cfg.blockHighlight = !cfg.blockHighlight;
                    btn.setMessage(toggleText("Blok Vurgulama", cfg.blockHighlight)); PavConfig.save(); });

        addToggle(cx, y + gap * 3, w, h, "Kendi \u0130smini G\u00f6ster", cfg.showOwnName,
                btn -> { cfg.showOwnName = !cfg.showOwnName;
                    btn.setMessage(toggleText("Kendi \u0130smini G\u00f6ster", cfg.showOwnName)); PavConfig.save(); });

        addToggle(cx, y + gap * 4, w, h, "Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations,
                btn -> { cfg.realisticAnimations = !cfg.realisticAnimations;
                    btn.setMessage(toggleText("Ger\u00e7ek\u00e7i Hareketler", cfg.realisticAnimations)); PavConfig.save(); });
    }

    /** Sayfa 1: Ozellikler */
    private void initPageOzellikler(int cx, int y, int w, int h, int gap, PavConfig cfg) {
        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y, w, h,
                Text.literal("\u2714 Lithium + FerriteCore Aktif"), btn -> {}));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("Shader: Mod Men\u00fc \u00fczerinden"),
                btn -> {}));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 2, w, h,
                Text.literal("\u2714 \u00c7oklu Versiyon: 1.8 - 1.21.4"),
                btn -> {}));

        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, y + gap * 3, w, h,
                Text.literal("\u2714 PM | Tag Sistemi Aktif"),
                btn -> {}));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 4, w, h,
                Text.literal("Sunucu: " + PavClient.TARGET_SERVER),
                btn -> {}));
    }

    /** Sayfa 2: HUD */
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
                Text.literal("\u270e Mouse ile D\u00fczenle"),
                btn -> { if (this.client != null) this.client.setScreen(new HudEditScreen(this)); }));
    }

    /** Sayfa 3: Dans/Emote */
    private void initPageDans(int cx, int y, int w, int h, int gap) {
        String[] names = EmoteManager.EMOTE_NAMES;
        for (int i = 0; i < names.length; i++) {
            final int emoteId = i;
            this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + i * gap, w, h,
                    Text.literal("\u25b6 " + names[i]),
                    btn -> {
                        EmoteManager.playEmote(emoteId);
                        if (this.client != null) this.client.setScreen(null);
                    }));
        }

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + names.length * gap + 8, w, h,
                Text.literal("PavClient kullananlar dansini g\u00f6r\u00fcr"),
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

        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25c6 PavClient Ayarlar\u0131 \u25c6"), cx, 26, rgb);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
