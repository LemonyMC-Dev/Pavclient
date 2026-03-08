package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.mixin.KeyBindingAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModernKeybindsScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;
    private KeyBinding waitingBind;
    private int scrollOffset = 0;
    private final List<KeyBinding> allBinds = new ArrayList<>();

    public ModernKeybindsScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Tuş Atamaları"));
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        this.clearChildren();
        if (allBinds.isEmpty()) {
            allBinds.addAll(KeyBindingAccessor.pavclient$getKeysById().values());
            allBinds.sort(Comparator
                    .comparing(KeyBinding::getCategory)
                    .thenComparing(KeyBinding::getTranslationKey));
        }

        int cx = this.width / 2;
        int y = 46;
        int w = 240;
        int h = 20;
        int gap = 22;

        int maxRows = Math.max(6, Math.min(16, (this.height - 96) / gap));
        int maxOffset = Math.max(0, allBinds.size() - maxRows);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        for (int i = 0; i < maxRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= allBinds.size()) break;
            KeyBinding bind = allBinds.get(idx);
            String name = Text.translatable(bind.getTranslationKey()).getString();
            String current = waitingBind == bind ? "..." : bind.getBoundKeyLocalizedText().getString();

            final KeyBinding target = bind;
            this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + i * gap, w, h,
                    Text.literal(name + ": §b" + current),
                    btn -> {
                        waitingBind = (waitingBind == target) ? null : target;
                        init();
                    }));
        }

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, h,
                Text.literal("← Geri"), btn -> { if (this.client != null) this.client.setScreen(parent); }));

        String info = (scrollOffset + 1) + "-" + Math.min(scrollOffset + maxRows, allBinds.size()) + " / " + allBinds.size();
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 56, w, 20,
                Text.literal("Liste: " + info), btn -> {}));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingBind != null) {
            waitingBind.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
            KeyBinding.updateKeysByCode();
            settings.write();
            waitingBind = null;
            init();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxRows = Math.max(6, Math.min(16, (this.height - 96) / 22));
        int maxOffset = Math.max(0, allBinds.size() - maxRows);
        if (verticalAmount < 0 && scrollOffset < maxOffset) {
            scrollOffset++;
            init();
            return true;
        }
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 140, 14, 280, this.height - 28);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Tuş Atamaları"), cx, 24, 0xFFFFFFFF);
        if (waitingBind != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Bir tuşa bas..."), cx, this.height - 52, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }
}
