package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

/**
 * Modern erisilebilirlik ayarlari ekrani.
 */
public class ModernAccessibilityScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernAccessibilityScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Eri\u015filebilirlik"));
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

        // Narrator
        boolean narrator = settings.getNarrator().getValue() != net.minecraft.client.option.NarratorMode.OFF;
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y, bw, bh,
                Text.literal("Anlat\u0131c\u0131: " + (narrator ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    var current = settings.getNarrator().getValue();
                    var next = current == net.minecraft.client.option.NarratorMode.OFF
                            ? net.minecraft.client.option.NarratorMode.ALL
                            : net.minecraft.client.option.NarratorMode.OFF;
                    settings.getNarrator().setValue(next);
                    settings.write();
                    init();
                }));

        // Altyazi
        boolean subtitles = settings.getShowSubtitles().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap, bw, bh,
                Text.literal("Altyaz\u0131lar: " + (subtitles ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getShowSubtitles().setValue(!subtitles);
                    settings.write();
                    init();
                }));

        // Parlaklik (Gamma)
        double gamma = settings.getGamma().getValue();
        String gammaText = gamma >= 1.0 ? "Maks" : Math.round(gamma * 100) + "%";
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 2, bw, bh,
                Text.literal("Parlakl\u0131k: " + gammaText),
                btn -> {
                    double ng = gamma + 0.25;
                    if (ng > 1.05) ng = 0.0;
                    settings.getGamma().setValue(ng);
                    settings.write();
                    init();
                }));

        // Dis Korumalari (Distortion)
        double distortion = settings.getDistortionEffectScale().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 3, bw, bh,
                Text.literal("Bozulma Efekti: " + Math.round(distortion * 100) + "%"),
                btn -> {
                    double nd = distortion + 0.25;
                    if (nd > 1.05) nd = 0.0;
                    settings.getDistortionEffectScale().setValue(nd);
                    settings.write();
                    init();
                }));

        // FOV Efekti
        double fovEffect = settings.getFovEffectScale().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 4, bw, bh,
                Text.literal("FOV Efekti: " + Math.round(fovEffect * 100) + "%"),
                btn -> {
                    double nf = fovEffect + 0.25;
                    if (nf > 1.05) nf = 0.0;
                    settings.getFovEffectScale().setValue(nf);
                    settings.write();
                    init();
                }));

        // Koyu Arka Plan
        double bgOpacity = settings.getTextBackgroundOpacity().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 5, bw, bh,
                Text.literal("Metin Arka Plan\u0131: " + Math.round(bgOpacity * 100) + "%"),
                btn -> {
                    double nb = bgOpacity + 0.25;
                    if (nb > 1.05) nb = 0.0;
                    settings.getTextBackgroundOpacity().setValue(nb);
                    settings.write();
                    init();
                }));

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
                Text.literal("\u2665 Eri\u015filebilirlik"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
