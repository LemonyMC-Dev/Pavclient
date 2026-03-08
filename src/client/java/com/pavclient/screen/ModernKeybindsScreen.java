package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class ModernKeybindsScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;
    private KeyBinding waitingBind;

    private static final String[] BIND_NAMES = {
            "İleri", "Sol", "Geri", "Sağ", "Zıpla", "Eğil", "Koş", "Envanter", "Saldır", "Kullan", "Sohbet", "Oyuncu Listesi"
    };

    public ModernKeybindsScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Tuş Atamaları"));
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int y = 46;
        int w = 240;
        int h = 20;
        int gap = 22;

        KeyBinding[] binds = {
                settings.forwardKey, settings.leftKey, settings.backKey, settings.rightKey,
                settings.jumpKey, settings.sneakKey, settings.sprintKey, settings.inventoryKey,
                settings.attackKey, settings.useKey, settings.chatKey, settings.playerListKey
        };

        int maxRows = Math.min(12, (this.height - 90) / gap);
        for (int i = 0; i < maxRows; i++) {
            KeyBinding bind = binds[i];
            String name = BIND_NAMES[i];
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
