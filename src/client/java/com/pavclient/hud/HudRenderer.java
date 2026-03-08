package com.pavclient.hud;

import com.pavclient.PavClient;
import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * PavClient HUD renderer.
 * Renders:
 * - RGB "PAVMC 2.SEZON!!" text (bottom-left)
 * - Armor HUD display
 * - Custom crosshair (if enabled)
 */
public class HudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        if (client.options.hudHidden) return;

        PavConfig config = PavConfig.get();

        // RGB text in bottom-left corner
        if (config.rgbTextEnabled) {
            renderRgbText(context, client);
        }

        // Armor HUD
        if (config.armorHudEnabled) {
            renderArmorHud(context, client, config);
        }

        // Custom crosshair
        if (config.customCrosshairEnabled) {
            renderCustomCrosshair(context, client, config);
        }
    }

    /**
     * Renders the rainbow "PAVMC 2.SEZON!!" text at bottom-left.
     */
    private void renderRgbText(DrawContext context, MinecraftClient client) {
        PavConfig config = PavConfig.get();
        long time = System.currentTimeMillis();
        String text = "PAVMC 2.SEZON!!";
        int y = client.getWindow().getScaledHeight() - 12;
        int x = 4;

        // Each character gets a slightly offset rainbow color
        for (int i = 0; i < text.length(); i++) {
            int color = GuiHelper.getRainbowColorSafe(time + (i * 100L), config.rgbSpeed);
            String ch = String.valueOf(text.charAt(i));
            context.drawTextWithShadow(client.textRenderer, Text.literal(ch), x, y, color);
            x += client.textRenderer.getWidth(ch);
        }
    }

    /**
     * Renders equipped armor items and durability.
     */
    private void renderArmorHud(DrawContext context, MinecraftClient client, PavConfig config) {
        int baseX = config.armorHudX;
        int baseY;

        if (config.armorHudAnchorBottom) {
            baseY = client.getWindow().getScaledHeight() - config.armorHudY - 80;
        } else {
            baseY = config.armorHudY;
        }

        // Render armor slots (helmet, chestplate, leggings, boots)
        for (int i = 0; i < 4; i++) {
            ItemStack stack = client.player.getInventory().armor.get(3 - i); // 3=helmet, 0=boots
            if (!stack.isEmpty()) {
                int itemY = baseY + (i * 18);

                // Draw item
                context.drawItem(stack, baseX, itemY);

                // Draw durability text
                if (stack.isDamageable()) {
                    int maxDmg = stack.getMaxDamage();
                    int currentDmg = maxDmg - stack.getDamage();
                    float percent = (float) currentDmg / maxDmg;

                    int durColor;
                    if (percent > 0.5f) durColor = 0xFF44FF44;
                    else if (percent > 0.25f) durColor = 0xFFFFFF44;
                    else durColor = 0xFFFF4444;

                    context.drawTextWithShadow(client.textRenderer,
                            Text.literal(currentDmg + ""),
                            baseX + 18, itemY + 4, durColor);
                }
            }
        }
    }

    /**
     * Renders a custom crosshair based on the selected style.
     */
    private void renderCustomCrosshair(DrawContext context, MinecraftClient client, PavConfig config) {
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;
        int color = 0xFFFFFFFF;

        switch (config.crosshairStyle) {
            case 1 -> {
                // Dot crosshair
                context.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, color);
            }
            case 2 -> {
                // Circle crosshair (approximation with small lines)
                int r = 4;
                // Simple circle approximation
                context.fill(centerX - 1, centerY - r, centerX + 2, centerY - r + 1, color);
                context.fill(centerX - 1, centerY + r, centerX + 2, centerY + r + 1, color);
                context.fill(centerX - r, centerY - 1, centerX - r + 1, centerY + 2, color);
                context.fill(centerX + r, centerY - 1, centerX + r + 1, centerY + 2, color);
            }
            case 3 -> {
                // Cross (thin lines, no center dot)
                int len = 5;
                int gap = 2;
                // Top
                context.fill(centerX, centerY - gap - len, centerX + 1, centerY - gap, color);
                // Bottom
                context.fill(centerX, centerY + gap + 1, centerX + 1, centerY + gap + len + 1, color);
                // Left
                context.fill(centerX - gap - len, centerY, centerX - gap, centerY + 1, color);
                // Right
                context.fill(centerX + gap + 1, centerY, centerX + gap + len + 1, centerY + 1, color);
            }
            default -> {
                // Style 0: small plus (default custom)
                context.fill(centerX - 4, centerY, centerX + 5, centerY + 1, color);
                context.fill(centerX, centerY - 4, centerX + 1, centerY + 5, color);
            }
        }
    }
}
