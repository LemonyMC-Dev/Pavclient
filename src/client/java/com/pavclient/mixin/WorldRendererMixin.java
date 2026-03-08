package com.pavclient.mixin;

import com.pavclient.config.PavConfig;
import com.pavclient.gui.GuiHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true)
    private void pavclient$customBlockOutline(Camera camera, Immediate vertexConsumers, MatrixStack matrices, boolean translucent, CallbackInfo ci) {
        PavConfig cfg = PavConfig.get();
        if (!cfg.blockHighlight) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null || mc.player == null) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr) || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (state.isAir()) return;

        ci.cancel(); // vanilla outline yerine custom

        int r = cfg.blockHighlightRed;
        int g = cfg.blockHighlightGreen;
        int b = cfg.blockHighlightBlue;
        if (cfg.blockHighlightRgb) {
            long ms = System.nanoTime() / 1_000_000L;
            float hue = (ms % 2500) / 2500.0f;
            int rgb = GuiHelper.hsbToRgb(hue, 0.9f, 1.0f);
            r = (rgb >> 16) & 0xFF;
            g = (rgb >> 8) & 0xFF;
            b = rgb & 0xFF;
        }

        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;
        float lineA = cfg.blockHighlightAlpha;
        float fillA = cfg.blockHighlightFillAlpha;

        Vec3d camPos = camera.getPos();
        var shape = state.getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return;
        Box box = shape.getBoundingBox().offset(pos).offset(-camPos.x, -camPos.y, -camPos.z);
        double e = cfg.blockHighlightSize;
        box = box.expand(e);

        VertexConsumer lineBuffer = vertexConsumers.getBuffer(RenderLayer.getLines());
        VertexRendering.drawBox(matrices, lineBuffer, box, rf, gf, bf, lineA);

        if (cfg.blockHighlightFill) {
            VertexConsumer fillBuffer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
            VertexRendering.drawFilledBox(matrices, fillBuffer,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    rf, gf, bf, fillA);
        }
    }
}
