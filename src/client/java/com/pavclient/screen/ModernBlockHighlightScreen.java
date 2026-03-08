package com.pavclient.screen;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.gui.ModernSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModernBlockHighlightScreen extends Screen {
    private final Screen parent;

    public ModernBlockHighlightScreen(Screen parent) {
        super(Text.literal("Blok Vurgu Ayarları"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        PavConfig cfg = PavConfig.get();

        int cx = this.width / 2;
        int y = 54;
        int w = 220;
        int h = 22;
        int gap = 26;

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, w, h,
                Text.literal("RGB Mod: " + (cfg.blockHighlightRgb ? "§aAÇIK" : "§cKAPALI")),
                btn -> {
                    cfg.blockHighlightRgb = !cfg.blockHighlightRgb;
                    PavConfig.save();
                    init();
                }));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap, w, h,
                "Kırmızı",
                0, 255, 1,
                cfg.blockHighlightRed,
                v -> String.valueOf((int) Math.round(v)),
                v -> { cfg.blockHighlightRed = (int) Math.round(v); PavConfig.save(); }
        ));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap * 2, w, h,
                "Yeşil",
                0, 255, 1,
                cfg.blockHighlightGreen,
                v -> String.valueOf((int) Math.round(v)),
                v -> { cfg.blockHighlightGreen = (int) Math.round(v); PavConfig.save(); }
        ));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap * 3, w, h,
                "Mavi",
                0, 255, 1,
                cfg.blockHighlightBlue,
                v -> String.valueOf((int) Math.round(v)),
                v -> { cfg.blockHighlightBlue = (int) Math.round(v); PavConfig.save(); }
        ));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap * 4, w, h,
                "Çizgi Opaklık",
                0.1, 1.0, 0.01,
                cfg.blockHighlightAlpha,
                v -> Math.round(v * 100) + "%",
                v -> { cfg.blockHighlightAlpha = v.floatValue(); PavConfig.save(); }
        ));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap * 5, w, h,
                "Boyut",
                0.0, 0.05, 0.0005,
                cfg.blockHighlightSize,
                v -> String.format("%.3f", v),
                v -> { cfg.blockHighlightSize = v.floatValue(); PavConfig.save(); }
        ));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 6, w, h,
                Text.literal("Bloğu Kapla: " + (cfg.blockHighlightFill ? "§aAÇIK" : "§cKAPALI")),
                btn -> {
                    cfg.blockHighlightFill = !cfg.blockHighlightFill;
                    PavConfig.save();
                    init();
                }));

        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y + gap * 7, w, h,
                "Dolgu Opaklık",
                0.05, 0.8, 0.01,
                cfg.blockHighlightFillAlpha,
                v -> Math.round(v * 100) + "%",
                v -> { cfg.blockHighlightFillAlpha = v.floatValue(); PavConfig.save(); }
        ));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, h,
                Text.literal("← Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 140, 16, 280, this.height - 32);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Blok Vurgu Ayarları"), cx, 26, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
