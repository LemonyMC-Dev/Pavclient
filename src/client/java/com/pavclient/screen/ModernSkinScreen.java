package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.text.Text;

/**
 * Modern gorunum/skin ozellestirme ekrani.
 */
public class ModernSkinScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernSkinScreen(Screen parent, GameOptions settings) {
        super(Text.literal("G\u00f6r\u00fcn\u00fcm \u00d6zelle\u015ftirme"));
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

        // Pelerin
        addPartToggle(cx, y, bw, bh, "Pelerin", PlayerModelPart.CAPE);
        // Ceket
        addPartToggle(cx, y + gap, bw, bh, "Ceket", PlayerModelPart.JACKET);
        // Sol Kol
        addPartToggle(cx, y + gap * 2, bw, bh, "Sol Kol", PlayerModelPart.LEFT_SLEEVE);
        // Sag Kol
        addPartToggle(cx, y + gap * 3, bw, bh, "Sa\u011f Kol", PlayerModelPart.RIGHT_SLEEVE);
        // Sol Bacak
        addPartToggle(cx, y + gap * 4, bw, bh, "Sol Bacak", PlayerModelPart.LEFT_PANTS_LEG);
        // Sag Bacak
        addPartToggle(cx, y + gap * 5, bw, bh, "Sa\u011f Bacak", PlayerModelPart.RIGHT_PANTS_LEG);
        // Sapka
        addPartToggle(cx, y + gap * 6, bw, bh, "\u015eapka", PlayerModelPart.HAT);

        // Bitti
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 32, bw, bh,
                Text.literal("\u2190 Bitti"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    private void addPartToggle(int cx, int y, int w, int h, String label, PlayerModelPart part) {
        boolean enabled = this.settings.isPlayerModelPartEnabled(part);
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y, w, h,
                Text.literal(label + ": " + (enabled ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    this.settings.setPlayerModelPart(part, !enabled);
                    this.settings.write();
                    init();
                }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 14, 260, this.height - 28);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u263a G\u00f6r\u00fcn\u00fcm \u00d6zelle\u015ftirme"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
