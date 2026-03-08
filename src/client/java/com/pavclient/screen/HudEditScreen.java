package com.pavclient.screen;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Ekranı Düzenle: HUD elementlerinin boyut ve konumunu ayarla.
 * Sol/Sağ/Yukarı/Aşağı butonlarla RGB yazı ve Armor HUD pozisyonu ayarlanır.
 */
public class HudEditScreen extends Screen {

    private final Screen parent;
    private int editing = 0; // 0=RGB, 1=Armor

    public HudEditScreen(Screen parent) {
        super(Text.literal("Ekran\u0131 D\u00fczenle"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        PavConfig cfg = PavConfig.get();
        int cx = this.width / 2;

        // Sekme: RGB / Armor
        this.addDrawableChild(editing == 0
                ? ModernButtonWidget.success(cx - 102, 40, 100, 20, Text.literal("RGB Yaz\u0131"), btn -> {})
                : ModernButtonWidget.create(cx - 102, 40, 100, 20, Text.literal("RGB Yaz\u0131"), btn -> { editing = 0; init(); }));

        this.addDrawableChild(editing == 1
                ? ModernButtonWidget.success(cx + 2, 40, 100, 20, Text.literal("Z\u0131rh HUD"), btn -> {})
                : ModernButtonWidget.create(cx + 2, 40, 100, 20, Text.literal("Z\u0131rh HUD"), btn -> { editing = 1; init(); }));

        int y = 80;
        int bw = 140;

        if (editing == 0) {
            // RGB X
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y, 70, 22, Text.literal("X \u25C0"),
                    btn -> { cfg.rgbX = Math.max(0, cfg.rgbX - 5); PavConfig.save(); }));
            this.addDrawableChild(ModernButtonWidget.create(cx + 2, y, 70, 22, Text.literal("X \u25B6"),
                    btn -> { cfg.rgbX += 5; PavConfig.save(); }));

            // RGB Y
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y + 28, 70, 22, Text.literal("Y \u25B2"),
                    btn -> { cfg.rgbY += 5; PavConfig.save(); }));
            this.addDrawableChild(ModernButtonWidget.create(cx + 2, y + 28, 70, 22, Text.literal("Y \u25BC"),
                    btn -> { cfg.rgbY = Math.max(5, cfg.rgbY - 5); PavConfig.save(); }));

            // RGB Scale
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y + 56, bw + 2, 22,
                    Text.literal("Boyut: " + String.format("%.1fx", cfg.rgbScale)),
                    btn -> { cfg.rgbScale += 0.5f; if (cfg.rgbScale > 4.0f) cfg.rgbScale = 1.0f;
                        btn.setMessage(Text.literal("Boyut: " + String.format("%.1fx", cfg.rgbScale))); PavConfig.save(); }));
        } else {
            // Armor X
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y, 70, 22, Text.literal("X \u25C0"),
                    btn -> { cfg.armorHudX = Math.max(0, cfg.armorHudX - 5); PavConfig.save(); }));
            this.addDrawableChild(ModernButtonWidget.create(cx + 2, y, 70, 22, Text.literal("X \u25B6"),
                    btn -> { cfg.armorHudX += 5; PavConfig.save(); }));

            // Armor Y
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y + 28, 70, 22, Text.literal("Y \u25B2"),
                    btn -> { cfg.armorHudY += 5; PavConfig.save(); }));
            this.addDrawableChild(ModernButtonWidget.create(cx + 2, y + 28, 70, 22, Text.literal("Y \u25BC"),
                    btn -> { cfg.armorHudY = Math.max(0, cfg.armorHudY - 5); PavConfig.save(); }));

            // Armor Scale
            this.addDrawableChild(ModernButtonWidget.create(cx - 72, y + 56, bw + 2, 22,
                    Text.literal("Boyut: " + String.format("%.1fx", cfg.armorHudScale)),
                    btn -> { cfg.armorHudScale += 0.25f; if (cfg.armorHudScale > 3.0f) cfg.armorHudScale = 0.5f;
                        btn.setMessage(Text.literal("Boyut: " + String.format("%.1fx", cfg.armorHudScale))); PavConfig.save(); }));
        }

        // Geri
        this.addDrawableChild(ModernButtonWidget.create(cx - 72, y + 92, bw + 2, 22,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;

        GuiHelper.drawPanel(context, cx - 110, 15, 220, this.height - 30);

        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u270E Ekran\u0131 D\u00fczenle"), cx, 22, rgb);

        PavConfig cfg = PavConfig.get();
        if (editing == 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("X:" + cfg.rgbX + " Y:" + cfg.rgbY + " Boyut:" + String.format("%.1f", cfg.rgbScale)),
                    cx, this.height - 22, 0xAAFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("X:" + cfg.armorHudX + " Y:" + cfg.armorHudY + " Boyut:" + String.format("%.1f", cfg.armorHudScale)),
                    cx, this.height - 22, 0xAAFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
