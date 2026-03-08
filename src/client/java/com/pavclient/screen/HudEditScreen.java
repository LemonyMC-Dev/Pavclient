package com.pavclient.screen;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * HUD Edit: Mouse ile tiklayip surukleyerek RGB yazi ve Armor HUD'u
 * yerlestir. Boyut ayari butonla.
 */
public class HudEditScreen extends Screen {

    private final Screen parent;

    // Surukleme state
    private boolean draggingRgb = false;
    private boolean draggingArmor = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditScreen(Screen parent) {
        super(Text.literal("Ekrani Duzenle"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        PavConfig cfg = PavConfig.get();
        int cx = this.width / 2;

        // RGB Scale
        this.addDrawableChild(ModernButtonWidget.create(cx - 160, this.height - 34, 100, 22,
                Text.literal("RGB: " + String.format("%.1fx", cfg.rgbScale)),
                btn -> {
                    cfg.rgbScale += 0.5f;
                    if (cfg.rgbScale > 4.0f) cfg.rgbScale = 1.0f;
                    btn.setMessage(Text.literal("RGB: " + String.format("%.1fx", cfg.rgbScale)));
                    PavConfig.save();
                }));

        // Armor Scale
        this.addDrawableChild(ModernButtonWidget.create(cx - 50, this.height - 34, 100, 22,
                Text.literal("Zirh: " + String.format("%.1fx", cfg.armorHudScale)),
                btn -> {
                    cfg.armorHudScale += 0.25f;
                    if (cfg.armorHudScale > 3.0f) cfg.armorHudScale = 0.5f;
                    btn.setMessage(Text.literal("Zirh: " + String.format("%.1fx", cfg.armorHudScale)));
                    PavConfig.save();
                }));

        // Kaydet & Geri
        this.addDrawableChild(ModernButtonWidget.success(cx + 60, this.height - 34, 100, 22,
                Text.literal("\u2714 Kaydet"),
                btn -> {
                    PavConfig.save();
                    if (this.client != null) this.client.setScreen(parent);
                }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Yari seffaf arka plan
        context.fill(0, 0, this.width, this.height, 0x88000000);

        PavConfig cfg = PavConfig.get();
        int screenH = this.client != null ? this.client.getWindow().getScaledHeight() : this.height;

        // RGB yazi onizleme kutusu
        int rgbBoxW = 120;
        int rgbBoxH = 20;
        int rgbDrawX = cfg.rgbX;
        int rgbDrawY = screenH - cfg.rgbY - rgbBoxH;
        boolean rgbHover = mouseX >= rgbDrawX && mouseX <= rgbDrawX + rgbBoxW
                        && mouseY >= rgbDrawY && mouseY <= rgbDrawY + rgbBoxH;

        int rgbBorder = rgbHover || draggingRgb ? 0xFF7C4DFF : 0x88FFFFFF;
        context.fill(rgbDrawX - 1, rgbDrawY - 1, rgbDrawX + rgbBoxW + 1, rgbDrawY + rgbBoxH + 1, rgbBorder);
        context.fill(rgbDrawX, rgbDrawY, rgbDrawX + rgbBoxW, rgbDrawY + rgbBoxH, 0x44000000);

        long ms = System.nanoTime() / 1_000_000L;
        float hue = (ms % 3000) / 3000.0f;
        int rgb = 0xFF000000 | GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
        context.drawTextWithShadow(this.textRenderer, Text.literal("RGB YAZI"), rgbDrawX + 4, rgbDrawY + 6, rgb);

        // Armor HUD onizleme kutusu
        int armorBoxW = 60;
        int armorBoxH = 80;
        int armorDrawX = cfg.armorHudX;
        int armorDrawY;
        if (cfg.armorHudAnchorBottom) {
            armorDrawY = screenH - cfg.armorHudY - armorBoxH;
        } else {
            armorDrawY = cfg.armorHudY;
        }
        boolean armorHover = mouseX >= armorDrawX && mouseX <= armorDrawX + armorBoxW
                          && mouseY >= armorDrawY && mouseY <= armorDrawY + armorBoxH;

        int armorBorder = armorHover || draggingArmor ? 0xFF69F0AE : 0x88FFFFFF;
        context.fill(armorDrawX - 1, armorDrawY - 1, armorDrawX + armorBoxW + 1, armorDrawY + armorBoxH + 1, armorBorder);
        context.fill(armorDrawX, armorDrawY, armorDrawX + armorBoxW, armorDrawY + armorBoxH, 0x44000000);
        context.drawTextWithShadow(this.textRenderer, Text.literal("ZIRH"), armorDrawX + 4, armorDrawY + 4, 0xFF69F0AE);
        context.drawTextWithShadow(this.textRenderer, Text.literal("HUD"), armorDrawX + 4, armorDrawY + 16, 0xFF69F0AE);

        // Bilgi yazisi
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Tiklayip surukleyerek tasiyabilirsiniz"), this.width / 2, 8, 0xAAFFFFFF);

        // Konum bilgisi
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("RGB: " + cfg.rgbX + "," + cfg.rgbY + " | Zirh: " + cfg.armorHudX + "," + cfg.armorHudY),
                this.width / 2, 22, 0x66FFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            PavConfig cfg = PavConfig.get();
            int screenH = this.client != null ? this.client.getWindow().getScaledHeight() : this.height;

            // RGB kutusu
            int rgbBoxW = 120, rgbBoxH = 20;
            int rgbDrawX = cfg.rgbX;
            int rgbDrawY = screenH - cfg.rgbY - rgbBoxH;
            if (mouseX >= rgbDrawX && mouseX <= rgbDrawX + rgbBoxW
                && mouseY >= rgbDrawY && mouseY <= rgbDrawY + rgbBoxH) {
                draggingRgb = true;
                dragOffsetX = (int) mouseX - rgbDrawX;
                dragOffsetY = (int) mouseY - rgbDrawY;
                return true;
            }

            // Armor kutusu
            int armorBoxW = 60, armorBoxH = 80;
            int armorDrawX = cfg.armorHudX;
            int armorDrawY = cfg.armorHudAnchorBottom
                ? screenH - cfg.armorHudY - armorBoxH : cfg.armorHudY;
            if (mouseX >= armorDrawX && mouseX <= armorDrawX + armorBoxW
                && mouseY >= armorDrawY && mouseY <= armorDrawY + armorBoxH) {
                draggingArmor = true;
                dragOffsetX = (int) mouseX - armorDrawX;
                dragOffsetY = (int) mouseY - armorDrawY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        PavConfig cfg = PavConfig.get();
        int screenH = this.client != null ? this.client.getWindow().getScaledHeight() : this.height;

        if (draggingRgb) {
            cfg.rgbX = Math.max(0, (int) mouseX - dragOffsetX);
            int newDrawY = (int) mouseY - dragOffsetY;
            cfg.rgbY = Math.max(5, screenH - newDrawY - 20);
            return true;
        }

        if (draggingArmor) {
            cfg.armorHudX = Math.max(0, (int) mouseX - dragOffsetX);
            if (cfg.armorHudAnchorBottom) {
                int newDrawY = (int) mouseY - dragOffsetY;
                cfg.armorHudY = Math.max(0, screenH - newDrawY - 80);
            } else {
                cfg.armorHudY = Math.max(0, (int) mouseY - dragOffsetY);
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingRgb || draggingArmor) {
            draggingRgb = false;
            draggingArmor = false;
            PavConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
