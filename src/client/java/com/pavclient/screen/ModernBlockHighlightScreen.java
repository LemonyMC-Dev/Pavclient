package com.pavclient.screen;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
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

        addStepper(cx, y + gap, w, h, "Kırmızı", String.valueOf(cfg.blockHighlightRed),
                () -> { cfg.blockHighlightRed = Math.max(0, cfg.blockHighlightRed - 5); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightRed = Math.min(255, cfg.blockHighlightRed + 5); PavConfig.save(); init(); });

        addStepper(cx, y + gap * 2, w, h, "Yeşil", String.valueOf(cfg.blockHighlightGreen),
                () -> { cfg.blockHighlightGreen = Math.max(0, cfg.blockHighlightGreen - 5); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightGreen = Math.min(255, cfg.blockHighlightGreen + 5); PavConfig.save(); init(); });

        addStepper(cx, y + gap * 3, w, h, "Mavi", String.valueOf(cfg.blockHighlightBlue),
                () -> { cfg.blockHighlightBlue = Math.max(0, cfg.blockHighlightBlue - 5); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightBlue = Math.min(255, cfg.blockHighlightBlue + 5); PavConfig.save(); init(); });

        addStepper(cx, y + gap * 4, w, h, "Çizgi Opaklık", Math.round(cfg.blockHighlightAlpha * 100) + "%",
                () -> { cfg.blockHighlightAlpha = Math.max(0.1f, cfg.blockHighlightAlpha - 0.1f); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightAlpha = Math.min(1.0f, cfg.blockHighlightAlpha + 0.1f); PavConfig.save(); init(); });

        addStepper(cx, y + gap * 5, w, h, "Boyut", String.format("%.3f", cfg.blockHighlightSize),
                () -> { cfg.blockHighlightSize = Math.max(0.0f, cfg.blockHighlightSize - 0.001f); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightSize = Math.min(0.05f, cfg.blockHighlightSize + 0.001f); PavConfig.save(); init(); });

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 6, w, h,
                Text.literal("Bloğu Kapla: " + (cfg.blockHighlightFill ? "§aAÇIK" : "§cKAPALI")),
                btn -> {
                    cfg.blockHighlightFill = !cfg.blockHighlightFill;
                    PavConfig.save();
                    init();
                }));

        addStepper(cx, y + gap * 7, w, h, "Dolgu Opaklık", Math.round(cfg.blockHighlightFillAlpha * 100) + "%",
                () -> { cfg.blockHighlightFillAlpha = Math.max(0.05f, cfg.blockHighlightFillAlpha - 0.05f); PavConfig.save(); init(); },
                () -> { cfg.blockHighlightFillAlpha = Math.min(0.8f, cfg.blockHighlightFillAlpha + 0.05f); PavConfig.save(); init(); });

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, h,
                Text.literal("← Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    private void addStepper(int cx, int y, int w, int h, String label, String value, Runnable onLeft, Runnable onRight) {
        int side = 28;
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, side, h, Text.literal("<"), b -> onLeft.run()));
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2 + side + 2, y, w - side * 2 - 4, h,
                Text.literal(label + ": " + value), b -> {}));
        this.addDrawableChild(ModernButtonWidget.create(cx + w / 2 - side, y, side, h, Text.literal(">"), b -> onRight.run()));
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
