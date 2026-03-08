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

public class HudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.options.hudHidden) return;
        PavConfig cfg = PavConfig.get();
        if (cfg.rgbTextEnabled) renderRgbText(context, mc, cfg);
        if (cfg.armorHudEnabled) renderArmorHud(context, mc, cfg);
        if (cfg.customCrosshairEnabled) renderCrosshair(context, mc, cfg);
    }

    /**
     * RGB: System.nanoTime ile her frame farkl\u0131 renk. Asla sabit kalmaz.
     * Her karakter farkl\u0131 hue offset -> gradient dalga efekti.
     */
    private void renderRgbText(DrawContext context, MinecraftClient mc, PavConfig cfg) {
        TextRenderer tr = mc.textRenderer;
        String text = "PAVMC 2.SEZON!!";
        int screenH = mc.getWindow().getScaledHeight();

        MatrixStack mat = context.getMatrices();
        mat.push();
        float scale = cfg.rgbScale;
        mat.scale(scale, scale, 1.0f);

        float drawX = cfg.rgbX / scale;
        float drawY = (screenH - cfg.rgbY) / scale;

        // nanoTime -> ms çevir, çok hızlı döngü
        long ms = System.nanoTime() / 1_000_000L;
        float baseHue = (ms % 3000) / 3000.0f; // 3 saniyede tam döngü

        float xOff = 0;
        for (int i = 0; i < text.length(); i++) {
            float charHue = (baseHue + i * 0.07f) % 1.0f;
            int color = 0xFF000000 | GuiHelper.hsbToRgb(charHue, 0.9f, 1.0f);
            String ch = String.valueOf(text.charAt(i));
            context.drawTextWithShadow(tr, Text.literal(ch), (int)(drawX + xOff), (int)drawY, color);
            xOff += tr.getWidth(ch);
        }
        mat.pop();
    }

    private void renderArmorHud(DrawContext context, MinecraftClient mc, PavConfig cfg) {
        MatrixStack mat = context.getMatrices();
        mat.push();
        float scale = cfg.armorHudScale;
        mat.scale(scale, scale, 1.0f);

        int bx = (int)(cfg.armorHudX / scale);
        int by;
        if (cfg.armorHudAnchorBottom) {
            by = (int)((mc.getWindow().getScaledHeight() - cfg.armorHudY - 80) / scale);
        } else {
            by = (int)(cfg.armorHudY / scale);
        }

        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().armor.get(3 - i);
            if (!stack.isEmpty()) {
                int iy = by + (i * 20);
                context.drawItem(stack, bx, iy);
                if (stack.isDamageable()) {
                    int max = stack.getMaxDamage();
                    int cur = max - stack.getDamage();
                    float pct = (float) cur / max;
                    int dc = pct > 0.5f ? 0xFF69F0AE : pct > 0.25f ? 0xFFFFD740 : 0xFFFF5252;
                    context.drawTextWithShadow(mc.textRenderer,
                            Text.literal(String.valueOf(cur)), bx + 20, iy + 5, dc);
                }
            }
        }
        mat.pop();
    }

    private void renderCrosshair(DrawContext context, MinecraftClient mc, PavConfig cfg) {
        int cx = mc.getWindow().getScaledWidth() / 2;
        int cy = mc.getWindow().getScaledHeight() / 2;
        int c = 0xCCFFFFFF;
        switch (cfg.crosshairStyle) {
            case 1 -> context.fill(cx, cy, cx + 1, cy + 1, c);
            case 2 -> {
                int r = 4;
                context.fill(cx - 1, cy - r, cx + 2, cy - r + 1, c);
                context.fill(cx - 1, cy + r, cx + 2, cy + r + 1, c);
                context.fill(cx - r, cy - 1, cx - r + 1, cy + 2, c);
                context.fill(cx + r, cy - 1, cx + r + 1, cy + 2, c);
            }
            case 3 -> {
                int len = 5, gap = 2;
                context.fill(cx, cy - gap - len, cx + 1, cy - gap, c);
                context.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, c);
                context.fill(cx - gap - len, cy, cx - gap, cy + 1, c);
                context.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, c);
            }
            default -> {
                context.fill(cx - 4, cy, cx + 5, cy + 1, c);
                context.fill(cx, cy - 4, cx + 1, cy + 5, c);
            }
        }
    }
}
