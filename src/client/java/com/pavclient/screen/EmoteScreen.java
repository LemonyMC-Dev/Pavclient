package com.pavclient.screen;

import com.pavclient.emote.EmoteManager;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Emote/Dans secim ekrani. Modern radial-style menu.
 */
public class EmoteScreen extends Screen {

    private final Screen parent;

    public EmoteScreen(Screen parent) {
        super(Text.literal("Dans / Emote"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int bw = 180;
        int bh = 28;
        int gap = 34;

        String[] icons = {"\uD83D\uDC4B", "\uD83D\uDD7A", "\u270B", "\uD83D\uDC4F", "\uD83D\uDD04"};
        String[] names = EmoteManager.EMOTE_NAMES;

        int startY = cy - (names.length * gap) / 2;

        for (int i = 0; i < names.length; i++) {
            final int emoteId = i;
            this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + i * gap, bw, bh,
                Text.literal(names[i]),
                btn -> {
                    EmoteManager.playEmote(emoteId);
                    if (this.client != null) this.client.setScreen(null);
                }
            ));
        }

        // Geri
        this.addDrawableChild(ModernButtonWidget.danger(
            cx - bw / 2, startY + names.length * gap + 10, bw, bh,
            Text.literal("\u2716 Kapat"),
            btn -> { if (this.client != null) this.client.setScreen(null); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Yari seffaf arka plan (oyun gorunsun)
        context.fill(0, 0, this.width, this.height, 0xAA0A0A14);

        int cx = this.width / 2;
        int panelH = EmoteManager.EMOTE_NAMES.length * 34 + 80;
        int panelY = this.height / 2 - panelH / 2;
        GuiHelper.drawPanel(context, cx - 110, panelY, 220, panelH);

        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Dans / Emote"), cx, panelY + 8, rgb);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
