package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.network.FriendsChannel;
import com.pavclient.social.FriendManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class FriendChatScreen extends Screen {
    private final Screen parent;
    private final String peer;
    private TextFieldWidget input;

    public FriendChatScreen(Screen parent, String peer) {
        super(Text.literal("Sohbet"));
        this.parent = parent;
        this.peer = peer;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int w = 320;
        input = new TextFieldWidget(this.textRenderer, cx - w / 2, this.height - 58, w - 70, 22, Text.literal("Mesaj"));
        input.setMaxLength(200);
        this.addDrawableChild(input);

        this.addDrawableChild(ModernButtonWidget.success(cx + w / 2 - 64, this.height - 58, 64, 22,
                Text.literal("Gönder"), b -> {
                    String msg = input.getText().trim();
                    if (!msg.isEmpty()) {
                        FriendManager.addChatLine(peer, FriendManager.selfName(), msg);
                        FriendsChannel.sendChat(peer, msg);
                        input.setText("");
                    }
                }));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, 22,
                Text.literal("← Geri"), b -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return input != null && input.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return input != null && input.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        int chatX = cx - 160;
        int chatY = 40;
        int chatW = 320;
        int chatH = this.height - 110;

        GuiHelper.drawPanel(context, chatX - 10, 14, chatW + 20, this.height - 28);
        boolean online = FriendManager.isOnline(peer);
        String dot = online ? "§a●" : "§7○";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(peer + " " + dot), cx, 24, 0xFFFFFFFF);

        context.fill(chatX, chatY, chatX + chatW, chatY + chatH, 0x44000000);

        List<FriendManager.ChatLine> lines = FriendManager.getChat(peer);
        int max = Math.max(1, chatH / 12 - 1);
        int start = Math.max(0, lines.size() - max);
        int y = chatY + 6;
        for (int i = start; i < lines.size(); i++) {
            var line = lines.get(i);
            String prefix = line.from().equalsIgnoreCase(FriendManager.selfName()) ? "§bSen: §f" : "§d" + line.from() + ": §f";
            context.drawTextWithShadow(this.textRenderer, Text.literal(prefix + line.message()), chatX + 6, y, 0xFFFFFFFF);
            y += 12;
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
