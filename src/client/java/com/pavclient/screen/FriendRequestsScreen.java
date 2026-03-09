package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.network.FriendsChannel;
import com.pavclient.social.FriendManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class FriendRequestsScreen extends Screen {
    private final Screen parent;
    private int scrollOffset = 0;

    public FriendRequestsScreen(Screen parent) {
        super(Text.literal("İstekler"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int y = 50;
        int w = 260;
        int h = 22;
        int gap = 24;

        List<String> req = FriendManager.getRequests();
        int maxRows = Math.max(6, Math.min(12, (this.height - 100) / gap));
        int maxOffset = Math.max(0, req.size() - maxRows);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        for (int i = 0; i < maxRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= req.size()) break;
            String from = req.get(idx);
            int rowY = y + i * gap;

            this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, rowY, w - 64, h,
                    Text.literal(from), b -> {}));

            this.addDrawableChild(ModernButtonWidget.success(cx + w / 2 - 62, rowY, 30, h,
                    Text.literal("+"), b -> {
                        FriendManager.addFriend(from);
                        FriendManager.removeRequest(from);
                        FriendsChannel.sendAccept(from);
                        init();
                    }));

            this.addDrawableChild(ModernButtonWidget.danger(cx + w / 2 - 30, rowY, 30, h,
                    Text.literal("-"), b -> {
                        FriendManager.removeRequest(from);
                        FriendsChannel.sendDecline(from);
                        init();
                    }));
        }

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, h,
                Text.literal("← Geri"), b -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxRows = Math.max(6, Math.min(12, (this.height - 100) / 24));
        int maxOffset = Math.max(0, FriendManager.getRequests().size() - maxRows);
        if (verticalAmount < 0 && scrollOffset < maxOffset) {
            scrollOffset++;
            init();
            return true;
        }
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 150, 14, 300, this.height - 28);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("@ Arkadaşlık İstekleri"), cx, 24, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
