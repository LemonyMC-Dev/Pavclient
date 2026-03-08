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
 * PavClient Ayarlar\u0131 ekran\u0131.
 * T\u00fcm client \u00f6zellikleri burada a\u00e7\u0131l\u0131p kapat\u0131labilir.
 */
public class ClientSettingsScreen extends Screen {

    private final Screen parent;

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal("PavClient Ayarlar\u0131"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        PavConfig cfg = PavConfig.get();
        int cx = this.width / 2;
        int bw = 250;
        int bh = 24;
        int gap = 30;
        int y0 = 60;

        // RGB Yaz\u0131
        addSetting(cx, y0, bw, bh,
                "RGB Yaz\u0131: " + onOff(cfg.rgbTextEnabled),
                btn -> { cfg.rgbTextEnabled = !cfg.rgbTextEnabled;
                    btn.setMessage(Text.literal("RGB Yaz\u0131: " + onOff(cfg.rgbTextEnabled))); PavConfig.save(); });

        // Z\u0131rh HUD
        addSetting(cx, y0 + gap, bw, bh,
                "Z\u0131rh G\u00f6stergesi: " + onOff(cfg.armorHudEnabled),
                btn -> { cfg.armorHudEnabled = !cfg.armorHudEnabled;
                    btn.setMessage(Text.literal("Z\u0131rh G\u00f6stergesi: " + onOff(cfg.armorHudEnabled))); PavConfig.save(); });

        // Z\u0131rh pozisyon
        addSetting(cx, y0 + gap * 2, bw, bh,
                "Z\u0131rh Pozisyon: " + (cfg.armorHudAnchorBottom ? "Sol Alt" : "Sol \u00dcst"),
                btn -> { cfg.armorHudAnchorBottom = !cfg.armorHudAnchorBottom;
                    btn.setMessage(Text.literal("Z\u0131rh Pozisyon: " + (cfg.armorHudAnchorBottom ? "Sol Alt" : "Sol \u00dcst"))); PavConfig.save(); });

        // \u00d6zel Ni\u015fan
        addSetting(cx, y0 + gap * 3, bw, bh,
                "\u00d6zel Ni\u015fan: " + onOff(cfg.customCrosshairEnabled),
                btn -> { cfg.customCrosshairEnabled = !cfg.customCrosshairEnabled;
                    btn.setMessage(Text.literal("\u00d6zel Ni\u015fan: " + onOff(cfg.customCrosshairEnabled))); PavConfig.save(); });

        // Ni\u015fan stili
        String[] stiller = {"Art\u0131", "Nokta", "Daire", "\u0130nce Ha\u00e7"};
        addSetting(cx, y0 + gap * 4, bw, bh,
                "Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length],
                btn -> { cfg.crosshairStyle = (cfg.crosshairStyle + 1) % stiller.length;
                    btn.setMessage(Text.literal("Ni\u015fan Stili: " + stiller[cfg.crosshairStyle % stiller.length])); PavConfig.save(); });

        // RGB h\u0131z\u0131
        addSetting(cx, y0 + gap * 5, bw, bh,
                "RGB H\u0131z\u0131: " + String.format("%.1f", cfg.rgbSpeed) + "x",
                btn -> { cfg.rgbSpeed += 0.5f; if (cfg.rgbSpeed > 5.0f) cfg.rgbSpeed = 0.5f;
                    btn.setMessage(Text.literal("RGB H\u0131z\u0131: " + String.format("%.1f", cfg.rgbSpeed) + "x")); PavConfig.save(); });

        // Optimizasyon bilgi
        this.addDrawableChild(ModernButtonWidget.success(
                cx - bw / 2, y0 + gap * 6, bw, bh,
                Text.literal("\u2714 Optimizasyon: Lithium + FerriteCore"),
                btn -> {}
        ));

        // Geri
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, y0 + gap * 7 + 10, bw, bh,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    private void addSetting(int cx, int y, int w, int h, String label, net.minecraft.client.gui.widget.ButtonWidget.PressAction action) {
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, w, h, Text.literal(label), action));
    }

    private String onOff(boolean v) { return v ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI"; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);

        int cx = this.width / 2;

        // Panel
        GuiHelper.drawPanel(context, cx - 145, 22, 290, this.height - 44);

        // Rainbow title
        long time = System.currentTimeMillis();
        int rgb = GuiHelper.getRainbowColor(time, 1.5f);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavClient Ayarlar\u0131 \u25C6"), cx, 32, rgb);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION), cx, 44, 0x55FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
