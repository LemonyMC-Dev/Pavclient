package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

/**
 * Modern sohbet ayarlari ekrani.
 */
public class ModernChatScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernChatScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Sohbet Ayarlar\u0131"));
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

        // Chat Gorunurlugu
        boolean chatVisible = settings.getChatVisibility().getValue() == net.minecraft.network.message.ChatVisibility.FULL;
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y, bw, bh,
                Text.literal("Sohbet: " + (chatVisible ? "\u00a7aG\u00f6r\u00fcn\u00fcr" : "\u00a7cGizli")),
                btn -> {
                    var current = settings.getChatVisibility().getValue();
                    var next = current == net.minecraft.network.message.ChatVisibility.FULL
                            ? net.minecraft.network.message.ChatVisibility.HIDDEN
                            : net.minecraft.network.message.ChatVisibility.FULL;
                    settings.getChatVisibility().setValue(next);
                    settings.write();
                    init();
                }));

        // Chat Renkleri
        boolean chatColors = settings.getChatColors().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap, bw, bh,
                Text.literal("Sohbet Renkleri: " + (chatColors ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getChatColors().setValue(!chatColors);
                    settings.write();
                    init();
                }));

        // Chat Boyutu
        double chatScale = settings.getChatScale().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 2, bw, bh,
                Text.literal("Sohbet Boyutu: " + Math.round(chatScale * 100) + "%"),
                btn -> {
                    double ns = chatScale + 0.25;
                    if (ns > 1.05) ns = 0.25;
                    settings.getChatScale().setValue(ns);
                    settings.write();
                    init();
                }));

        // Chat Opacity
        double chatOpacity = settings.getChatOpacity().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 3, bw, bh,
                Text.literal("Sohbet Opakligi: " + Math.round(chatOpacity * 100) + "%"),
                btn -> {
                    double no = chatOpacity + 0.2;
                    if (no > 1.05) no = 0.0;
                    settings.getChatOpacity().setValue(no);
                    settings.write();
                    init();
                }));

        // Chat Satir Araligi
        double chatSpacing = settings.getChatLineSpacing().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 4, bw, bh,
                Text.literal("Sat\u0131r Aral\u0131\u011f\u0131: " + Math.round(chatSpacing * 100) + "%"),
                btn -> {
                    double nls = chatSpacing + 0.25;
                    if (nls > 1.05) nls = 0.0;
                    settings.getChatLineSpacing().setValue(nls);
                    settings.write();
                    init();
                }));

        // Komut Onerileri
        boolean cmdSugg = settings.getAutoSuggestions().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 5, bw, bh,
                Text.literal("Komut \u00d6nerileri: " + (cmdSugg ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getAutoSuggestions().setValue(!cmdSugg);
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
                Text.literal("\u2709 Sohbet Ayarlar\u0131"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
