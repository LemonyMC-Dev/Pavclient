package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.gui.ModernSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

/**
 * Modern ses ayarlari ekrani.
 */
public class ModernSoundScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernSoundScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Ses Ayarlar\u0131"));
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

        addVolumeButton(cx, y, bw, bh, "Ana Ses", SoundCategory.MASTER);
        addVolumeButton(cx, y + gap, bw, bh, "M\u00fczik", SoundCategory.MUSIC);
        addVolumeButton(cx, y + gap * 2, bw, bh, "Bloklar", SoundCategory.BLOCKS);
        addVolumeButton(cx, y + gap * 3, bw, bh, "Canl\u0131lar", SoundCategory.HOSTILE);
        addVolumeButton(cx, y + gap * 4, bw, bh, "Oyuncular", SoundCategory.PLAYERS);
        addVolumeButton(cx, y + gap * 5, bw, bh, "Ortam", SoundCategory.AMBIENT);
        addVolumeButton(cx, y + gap * 6, bw, bh, "Hava Durumu", SoundCategory.WEATHER);

        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 32, bw, bh,
                Text.literal("\u2190 Bitti"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    private void addVolumeButton(int cx, int y, int w, int h, String label, SoundCategory category) {
        float vol = this.settings.getSoundVolume(category);
        this.addDrawableChild(new ModernSliderWidget(
                cx - w / 2, y, w, h,
                label,
                0.0, 1.0, 0.01,
                vol,
                v -> Math.round(v * 100) + "%",
                v -> { this.settings.getSoundVolumeOption(category).setValue(v); this.settings.write(); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 130, 14, 260, this.height - 28);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u266b Ses Ayarlar\u0131"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
