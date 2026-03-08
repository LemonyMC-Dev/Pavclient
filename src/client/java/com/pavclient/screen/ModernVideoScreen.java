package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.text.Text;

/**
 * Modern video ayarlari ekrani.
 */
public class ModernVideoScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernVideoScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Video Ayarlar\u0131"));
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int bw = 220;
        int bh = 22;
        int gap = 26;
        int y = 50;

        // Render Mesafesi (< >)
        int rd = settings.getViewDistance().getValue();
        addStepper(cx, y, bw, bh, "G\u00f6r\u00fc\u015f Mesafesi", String.valueOf(rd),
                () -> { int nv = rd - 2; if (nv < 2) nv = 32; settings.getViewDistance().setValue(nv); settings.write(); init(); },
                () -> { int nv = rd + 2; if (nv > 32) nv = 2; settings.getViewDistance().setValue(nv); settings.write(); init(); });

        // Grafik Kalitesi
        GraphicsMode gm = settings.getGraphicsMode().getValue();
        String gmName = switch (gm) {
            case FAST -> "H\u0131zl\u0131";
            case FANCY -> "G\u00fczel";
            case FABULOUS -> "Harika";
        };
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap, bw, bh,
                Text.literal("Grafik: " + gmName),
                btn -> {
                    GraphicsMode next = switch (gm) {
                        case FAST -> GraphicsMode.FANCY;
                        case FANCY -> GraphicsMode.FABULOUS;
                        case FABULOUS -> GraphicsMode.FAST;
                    };
                    settings.getGraphicsMode().setValue(next);
                    settings.write();
                    init();
                }));

        // FPS Limiti
        int fps = settings.getMaxFps().getValue();
        String fpsText = fps >= 260 ? "S\u0131n\u0131rs\u0131z" : String.valueOf(fps);
        addStepper(cx, y + gap * 2, bw, bh, "FPS Limiti", fpsText,
                () -> {
                    int nf = fps - 30;
                    if (fps >= 260) nf = 240;
                    if (nf < 30) nf = 260; // sınırsız
                    settings.getMaxFps().setValue(nf);
                    settings.write();
                    init();
                },
                () -> {
                    int nf = fps + 30;
                    if (nf > 260) nf = 260; // sınırsız
                    settings.getMaxFps().setValue(nf);
                    settings.write();
                    init();
                });

        // VSync
        boolean vs = settings.getEnableVsync().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 3, bw, bh,
                Text.literal("VSync: " + (vs ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getEnableVsync().setValue(!vs);
                    settings.write();
                    init();
                }));

        // Tam Ekran
        boolean fs = settings.getFullscreen().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 4, bw, bh,
                Text.literal("Tam Ekran: " + (fs ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getFullscreen().setValue(!fs);
                    if (this.client != null) this.client.getWindow().toggleFullscreen();
                    settings.write();
                    init();
                }));

        // GUI Boyutu
        int gui = settings.getGuiScale().getValue();
        addStepper(cx, y + gap * 5, bw, bh, "GUI Boyutu", (gui == 0 ? "Otomatik" : String.valueOf(gui)),
                () -> {
                    int ng = gui - 1;
                    if (ng < 0) ng = 4;
                    settings.getGuiScale().setValue(ng);
                    if (this.client != null) this.client.onResolutionChanged();
                    settings.write();
                    init();
                },
                () -> {
                    int ng = gui + 1;
                    if (ng > 4) ng = 0;
                    settings.getGuiScale().setValue(ng);
                    if (this.client != null) this.client.onResolutionChanged();
                    settings.write();
                    init();
                });

        // FOV
        int fov = settings.getFov().getValue();
        addStepper(cx, y + gap * 6, bw, bh, "FOV", String.valueOf(fov),
                () -> { int nfov = fov - 5; if (nfov < 30) nfov = 110; settings.getFov().setValue(nfov); settings.write(); init(); },
                () -> { int nfov = fov + 5; if (nfov > 110) nfov = 30; settings.getFov().setValue(nfov); settings.write(); init(); });

        // Bitti
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 32, bw, bh,
                Text.literal("\u2190 Bitti"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 14, 260, this.height - 28);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25a3 Video Ayarlar\u0131"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    private void addStepper(int cx, int y, int w, int h, String label, String value, Runnable onLeft, Runnable onRight) {
        int side = 28;
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, side, h, Text.literal("<"), b -> onLeft.run()));
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2 + side + 2, y, w - side * 2 - 4, h,
                Text.literal(label + ": " + value), b -> {}));
        this.addDrawableChild(ModernButtonWidget.create(cx + w / 2 - side, y, side, h, Text.literal(">"), b -> onRight.run()));
    }
}
