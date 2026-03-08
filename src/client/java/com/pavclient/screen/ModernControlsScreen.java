package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

/**
 * Modern kontroller ekrani - alt ekranlara yonlendirir.
 */
public class ModernControlsScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernControlsScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Kontroller"));
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int bw = 220;
        int bh = 22;
        int gap = 28;
        int y = 60;

        // Mouse Ayarlari
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y, bw, bh,
                Text.literal("\u25cb Mouse Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new MouseOptionsScreen(this, this.settings)); }
        ));

        // Tus Atamalari
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap, bw, bh,
                Text.literal("\u2328 Tu\u015f Atamalar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new KeybindsScreen(this, this.settings)); }
        ));

        // Otomatik Atlama
        boolean autoJump = settings.getAutoJump().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 2, bw, bh,
                Text.literal("Otomatik Atlama: " + (autoJump ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getAutoJump().setValue(!autoJump);
                    settings.write();
                    init();
                }));

        // Sneaking Toggle
        boolean sneakToggle = settings.getSneakToggled().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 3, bw, bh,
                Text.literal("E\u011filme Kilidi: " + (sneakToggle ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getSneakToggled().setValue(!sneakToggle);
                    settings.write();
                    init();
                }));

        // Sprint Toggle
        boolean sprintToggle = settings.getSprintToggled().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - bw / 2, y + gap * 4, bw, bh,
                Text.literal("Ko\u015fma Kilidi: " + (sprintToggle ? "\u00a7aA\u00c7IK" : "\u00a7cKAPALI")),
                btn -> {
                    settings.getSprintToggled().setValue(!sprintToggle);
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
                Text.literal("\u2328 Kontroller"), cx, 26, 0xFFB0BEC5);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
