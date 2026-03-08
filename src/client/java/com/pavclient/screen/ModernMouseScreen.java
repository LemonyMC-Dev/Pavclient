package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class ModernMouseScreen extends Screen {
    private final Screen parent;
    private final GameOptions settings;

    public ModernMouseScreen(Screen parent, GameOptions settings) {
        super(Text.literal("Mouse Ayarları"));
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int y = 60;
        int w = 220;
        int h = 22;
        int gap = 28;

        addStepper(cx, y, w, h, "Hassasiyet", String.format("%.2f", settings.getMouseSensitivity().getValue()),
                () -> { settings.getMouseSensitivity().setValue(Math.max(0.01, settings.getMouseSensitivity().getValue() - 0.05)); settings.write(); init(); },
                () -> { settings.getMouseSensitivity().setValue(Math.min(2.0, settings.getMouseSensitivity().getValue() + 0.05)); settings.write(); init(); });

        boolean invert = settings.getInvertYMouse().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap, w, h,
                Text.literal("Y Eksenini Ters Çevir: " + (invert ? "§aAÇIK" : "§cKAPALI")),
                btn -> { settings.getInvertYMouse().setValue(!invert); settings.write(); init(); }));

        boolean raw = settings.getRawMouseInput().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 2, w, h,
                Text.literal("Ham Mouse Girişi: " + (raw ? "§aAÇIK" : "§cKAPALI")),
                btn -> { settings.getRawMouseInput().setValue(!raw); settings.write(); init(); }));

        boolean discrete = settings.getDiscreteMouseScroll().getValue();
        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, y + gap * 3, w, h,
                Text.literal("Kesikli Kaydırma: " + (discrete ? "§aAÇIK" : "§cKAPALI")),
                btn -> { settings.getDiscreteMouseScroll().setValue(!discrete); settings.write(); init(); }));

        addStepper(cx, y + gap * 4, w, h, "Tekerlek Hassasiyeti", String.format("%.2f", settings.getMouseWheelSensitivity().getValue()),
                () -> { settings.getMouseWheelSensitivity().setValue(Math.max(0.01, settings.getMouseWheelSensitivity().getValue() - 0.1)); settings.write(); init(); },
                () -> { settings.getMouseWheelSensitivity().setValue(Math.min(10.0, settings.getMouseWheelSensitivity().getValue() + 0.1)); settings.write(); init(); });

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, h,
                Text.literal("← Geri"), btn -> { if (this.client != null) this.client.setScreen(parent); }));
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
        GuiHelper.drawPanel(context, cx - 130, 16, 260, this.height - 32);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Mouse Ayarları"), cx, 26, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
