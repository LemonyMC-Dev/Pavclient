package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * PavClient Settings Screen.
 * Accessible from ESC menu via "Client Ayarlari" button.
 *
 * Settings:
 * - RGB text on/off
 * - Armor HUD on/off
 * - Custom Crosshair on/off + style select
 * - Optimization info
 */
public class ClientSettingsScreen extends Screen {

    private final Screen parent;

    public ClientSettingsScreen(Screen parent) {
        super(Text.literal("PavClient Ayarlari"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        PavConfig config = PavConfig.get();
        int centerX = this.width / 2;
        int startY = 55;
        int buttonW = 240;
        int buttonH = 22;
        int spacing = 28;

        // RGB Text toggle
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY, buttonW, buttonH,
                Text.literal("RGB Yazi: " + (config.rgbTextEnabled ? "ACIK" : "KAPALI")),
                button -> {
                    config.rgbTextEnabled = !config.rgbTextEnabled;
                    button.setMessage(Text.literal("RGB Yazi: " + (config.rgbTextEnabled ? "ACIK" : "KAPALI")));
                    PavConfig.save();
                }
        ));

        // Armor HUD toggle
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing, buttonW, buttonH,
                Text.literal("Zirh HUD: " + (config.armorHudEnabled ? "ACIK" : "KAPALI")),
                button -> {
                    config.armorHudEnabled = !config.armorHudEnabled;
                    button.setMessage(Text.literal("Zirh HUD: " + (config.armorHudEnabled ? "ACIK" : "KAPALI")));
                    PavConfig.save();
                }
        ));

        // Armor HUD position toggle
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing * 2, buttonW, buttonH,
                Text.literal("Zirh HUD Pozisyon: " + (config.armorHudAnchorBottom ? "Sol Alt" : "Sol Ust")),
                button -> {
                    config.armorHudAnchorBottom = !config.armorHudAnchorBottom;
                    button.setMessage(Text.literal("Zirh HUD Pozisyon: " + (config.armorHudAnchorBottom ? "Sol Alt" : "Sol Ust")));
                    PavConfig.save();
                }
        ));

        // Custom Crosshair toggle
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing * 3, buttonW, buttonH,
                Text.literal("Ozel Nisan: " + (config.customCrosshairEnabled ? "ACIK" : "KAPALI")),
                button -> {
                    config.customCrosshairEnabled = !config.customCrosshairEnabled;
                    button.setMessage(Text.literal("Ozel Nisan: " + (config.customCrosshairEnabled ? "ACIK" : "KAPALI")));
                    PavConfig.save();
                }
        ));

        // Crosshair style
        String[] styles = {"Plus", "Nokta", "Daire", "Ince Haç"};
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing * 4, buttonW, buttonH,
                Text.literal("Nisan Stili: " + styles[config.crosshairStyle % styles.length]),
                button -> {
                    config.crosshairStyle = (config.crosshairStyle + 1) % styles.length;
                    button.setMessage(Text.literal("Nisan Stili: " + styles[config.crosshairStyle % styles.length]));
                    PavConfig.save();
                }
        ));

        // RGB speed
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing * 5, buttonW, buttonH,
                Text.literal("RGB Hizi: " + String.format("%.1f", config.rgbSpeed) + "x"),
                button -> {
                    config.rgbSpeed += 0.5f;
                    if (config.rgbSpeed > 5.0f) config.rgbSpeed = 0.5f;
                    button.setMessage(Text.literal("RGB Hizi: " + String.format("%.1f", config.rgbSpeed) + "x"));
                    PavConfig.save();
                }
        ));

        // Optimization info (read-only status)
        this.addDrawableChild(ModernButtonWidget.success(
                centerX - buttonW / 2, startY + spacing * 6, buttonW, buttonH,
                Text.literal("Optimizasyon: Lithium + FerriteCore AKTIF"),
                button -> { /* read-only */ }
        ));

        // Back button
        this.addDrawableChild(ModernButtonWidget.create(
                centerX - buttonW / 2, startY + spacing * 7 + 8, buttonW, buttonH,
                Text.literal("Geri"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawDarkOverlay(context, this.width, this.height);

        int centerX = this.width / 2;

        // Panel background
        GuiHelper.drawPanel(context, centerX - 140, 20, 280, this.height - 40);

        // Title with rainbow
        long time = System.currentTimeMillis();
        int rgbColor = GuiHelper.getRainbowColorSafe(time, 1.5f);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("PavClient Ayarlari"), centerX, 30, rgbColor);

        // Version info
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION),
                centerX, 42, 0x66FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
