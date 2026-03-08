package com.pavclient.screen;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Shown after first-time mod installation.
 * "Gerekli modlar indirildi! Oyunu Yeniden Baslatin [Kurulum Basarili]"
 * + Kapat button
 */
public class InstallCompleteScreen extends Screen {

    public InstallCompleteScreen() {
        super(Text.literal("PavClient - Kurulum Basarili"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // "Oyunu Kapat" button (closes the game for restart)
        this.addDrawableChild(ModernButtonWidget.danger(
                centerX - 100, centerY + 40, 200, 25,
                Text.literal("Oyunu Kapat"),
                button -> {
                    if (this.client != null) {
                        this.client.scheduleStop();
                    }
                }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawDarkOverlay(context, this.width, this.height);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Panel
        GuiHelper.drawPanel(context, centerX - 160, centerY - 70, 320, 140);

        // Title with rainbow effect
        long time = System.currentTimeMillis();
        int rgbColor = GuiHelper.getRainbowColorSafe(time, 1.5f);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("PavClient"),
                centerX, centerY - 55,
                rgbColor
        );

        // Success message
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Gerekli modlar indirildi!"),
                centerX, centerY - 30,
                0xFF44FF44
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Oyunu Yeniden Baslatin"),
                centerX, centerY - 15,
                0xFFFFFFFF
        );

        // [Kurulum Basarili] badge
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("[Kurulum Basarili]"),
                centerX, centerY + 5,
                0xFF6C63FF
        );

        // Version
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION),
                centerX, centerY + 20,
                0x88FFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
