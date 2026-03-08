package com.pavclient.mixin;

import com.pavclient.PavClient;
import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.screen.ClientSettingsScreen;
import com.pavclient.screen.ConfirmQuitScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Completely replaces ESC menu with PavClient's custom modern menu.
 * ALL default buttons removed. Custom buttons added:
 * - Oyuna D\u00f6n
 * - Ayarlar (vanilla settings)
 * - Client Ayarlar\u0131 (PavClient settings)
 * - Yeniden Ba\u011flan
 * - Oyunu Kapat (with confirm)
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void pavclient$replaceAllButtons(CallbackInfo ci) {
        // Remove ALL existing buttons
        List<Element> toRemove = new ArrayList<>(this.children());
        for (Element child : toRemove) {
            if (child instanceof ClickableWidget w) {
                this.remove(w);
            }
        }

        int cx = this.width / 2;
        int bw = 220;
        int bh = 26;
        int gap = 32;
        int startY = this.height / 2 - 70;

        // "Oyuna D\u00f6n"
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY, bw, bh,
                Text.literal("\u25B6 Oyuna D\u00f6n"),
                btn -> { if (this.client != null) this.client.setScreen(null); }
        ));

        // "Ayarlar" (vanilla Minecraft settings)
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap, bw, bh,
                Text.literal("\u2699 Ayarlar"),
                btn -> { if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options)); }
        ));

        // "Client Ayarlar\u0131" (PavClient settings)
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, startY + gap * 2, bw, bh,
                Text.literal("\u2726 Client Ayarlar\u0131"),
                btn -> { if (this.client != null) this.client.setScreen(new ClientSettingsScreen(this)); }
        ));

        // "Yeniden Ba\u011flan" (green)
        this.addDrawableChild(ModernButtonWidget.success(
                cx - bw / 2, startY + gap * 3, bw, bh,
                Text.literal("\u21BB Yeniden Ba\u011flan"),
                btn -> pavclient$reconnect()
        ));

        // "Oyunu Kapat" (red, with confirm)
        this.addDrawableChild(ModernButtonWidget.danger(
                cx - bw / 2, startY + gap * 4, bw, bh,
                Text.literal("\u2716 Oyunu Kapat"),
                btn -> { if (this.client != null) this.client.setScreen(new ConfirmQuitScreen(this)); }
        ));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void pavclient$renderCustomBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Draw PavClient branded overlay on top of the game
        context.fill(0, 0, this.width, this.height, 0xBB0A0A14);

        int cx = this.width / 2;

        // Rainbow PavClient title
        long time = System.currentTimeMillis();
        int rgb = GuiHelper.getRainbowColor(time, 1.5f);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u25C6 PavClient \u25C6"), cx, this.height / 2 - 95, rgb);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("v" + PavClient.CLIENT_VERSION + " \u2022 " + PavClient.TARGET_SERVER),
                cx, this.height / 2 - 82, 0x55FFFFFF);
    }

    private void pavclient$reconnect() {
        if (this.client == null) return;

        if (this.client.world != null) {
            this.client.world.disconnect();
        }
        this.client.disconnect();

        ServerInfo info = new ServerInfo(PavClient.MOD_NAME,
                PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT, ServerInfo.ServerType.OTHER);
        ServerAddress addr = ServerAddress.parse(PavClient.TARGET_SERVER + ":" + PavClient.TARGET_PORT);
        // Parent olarak ConnectionFailedScreen ver, TitleScreen degil (sonsuz dongu onlemi)
        ConnectScreen.connect(
                new com.pavclient.screen.ConnectionFailedScreen(
                        Text.literal("Ba\u011flant\u0131 kurulamad\u0131.")),
                this.client, addr, info, false, null);
    }
}
