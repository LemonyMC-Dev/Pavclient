package com.pavclient.hud;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * PavClient HUD renderer.
 * - RGB "PAVMC 2.SEZON!!" text (bottom-left, scaled 2x, per-character rainbow)
 * - Armor HUD with durability
 * - Custom crosshair
 */
public class HudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        if (client.options.hudHidden) return;

        PavConfig config = PavConfig.get();

        if (config.rgbTextEnabled) {
            renderRgbText(context, client);
        }
        if (config.armorHudEnabled) {
            renderArmorHud(context, client, config);
        }
        if (config.customCrosshairEnabled) {
            renderCustomCrosshair(context, client, config);
        }
    }

    /**
     * RGB rainbow "PAVMC 2.SEZON!!" - scaled 2x, each character different color.
     * Uses System.currentTimeMillis() for smooth continuous animation.
     */
    private void renderRgbText(DrawContext context, MinecraftClient client) {
        PavConfig config = PavConfig.get();
        TextRenderer tr = client.textRenderer;
        String text = "PAVMC 2.SEZON!!";
        int screenH = client.getWindow().getScaledHeight();

        // Scale 2x for bigger text
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        float scale = 2.0f;
        // Position: bottom-left corner, accounting for scale
        float baseX = 6f;
        float baseY = (screenH - 22f) / scale;
        matrices.scale(scale, scale, 1.0f);

        long time = System.currentTimeMillis();
        float xOffset = 0;

        for (int i = 0; i < text.length(); i++) {
            // Each character offset by 150ms for wave effect
            int color = GuiHelper.getRainbowColor(time + (i * 150L), config.rgbSpeed);
            String ch = String.valueOf(text.charAt(i));
            context.drawTextWithShadow(tr, Text.literal(ch), (int)(baseX + xOffset), (int)baseY, color);
            xOffset += tr.getWidth(ch);
        }

        matrices.pop();
    }

    /**
     * Armor HUD - shows equipped armor items with durability.
     */
    private void renderArmorHud(DrawContext context, MinecraftClient client, PavConfig config) {
        int baseX = config.armorHudX;
        int baseY;

        if (config.armorHudAnchorBottom) {
            baseY = client.getWindow().getScaledHeight() - config.armorHudY - 80;
        } else {
            baseY = config.armorHudY;
        }

        for (int i = 0; i < 4; i++) {
            ItemStack stack = client.player.getInventory().armor.get(3 - i);
            if (!stack.isEmpty()) {
                int itemY = baseY + (i * 20);
                context.drawItem(stack, baseX, itemY);

                if (stack.isDamageable()) {
                    int max = stack.getMaxDamage();
                    int current = max - stack.getDamage();
                    float pct = (float) current / max;

                    int durColor = pct > 0.5f ? 0xFF69F0AE : pct > 0.25f ? 0xFFFFD740 : 0xFFFF5252;
                    context.drawTextWithShadow(client.textRenderer,
                            Text.literal(String.valueOf(current)),
                            baseX + 20, itemY + 5, durColor);
                }
            }
        }
    }

    /**
     * Custom crosshair rendering.
     */
    private void renderCustomCrosshair(DrawContext context, MinecraftClient client, PavConfig config) {
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int c = 0xFFFFFFFF;

        switch (config.crosshairStyle) {
            case 1 -> { // Dot
                context.fill(cx - 1, cy - 1, cx + 2, cy + 2, c);
            }
            case 2 -> { // Circle approximation
                int r = 5;
                context.fill(cx - 1, cy - r, cx + 2, cy - r + 1, c);
                context.fill(cx - 1, cy + r, cx + 2, cy + r + 1, c);
                context.fill(cx - r, cy - 1, cx - r + 1, cy + 2, c);
                context.fill(cx + r, cy - 1, cx + r + 1, cy + 2, c);
                // Diagonal points
                context.fill(cx + r - 2, cy - r + 2, cx + r - 1, cy - r + 3, c);
                context.fill(cx - r + 2, cy - r + 2, cx - r + 3, cy - r + 3, c);
                context.fill(cx + r - 2, cy + r - 2, cx + r - 1, cy + r - 1, c);
                context.fill(cx - r + 2, cy + r - 2, cx - r + 3, cy + r - 1, c);
            }
            case 3 -> { // Cross with gap
                int len = 6; int gap = 3;
                context.fill(cx, cy - gap - len, cx + 1, cy - gap, c);
                context.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, c);
                context.fill(cx - gap - len, cy, cx - gap, cy + 1, c);
                context.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, c);
            }
            default -> { // Plus
                context.fill(cx - 5, cy, cx + 6, cy + 1, c);
                context.fill(cx, cy - 5, cx + 1, cy + 6, c);
            }
        }
    }
}
