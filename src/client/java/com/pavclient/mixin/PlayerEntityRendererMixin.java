package com.pavclient.mixin;

import com.pavclient.config.PavConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Kendi nametag'ini gormek icin hasLabel override.
 * Vanilla'da sneaking degilsen ve uzaktaysan gosterilmez; kendi ismin hicbir zaman gosterilmez.
 * Bu mixin showOwnName acikken kendi ismini de gosterir.
 */
@Mixin(LivingEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z",
            at = @At("RETURN"), cancellable = true)
    private void pavclient$showOwnNametag(Entity entity, double squaredDistanceToCamera,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!PavConfig.get().showOwnName) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        // Eger entity kendi oyuncumuz ise, her zaman goster
        if (entity == mc.player) {
            cir.setReturnValue(true);
        }
    }
}
