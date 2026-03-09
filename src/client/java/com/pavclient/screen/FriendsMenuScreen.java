package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.social.FriendManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class FriendsMenuScreen extends Screen {
    private final Screen parent;
    private int scrollOffset = 0;

    public FriendsMenuScreen(Screen parent) {
        super(Text.literal("Arkadaşlar"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int x = 18;
        int y = 34;
        int w = 270;
        int h = 22;
        int gap = 24;

        this.addDrawableChild(ModernButtonWidget.create(x, y, w - 38, h,
                Text.literal("Oyuncu: " + FriendManager.selfName()), b -> {}));

        this.addDrawableChild(ModernButtonWidget.create(x + w - 34, y, 34, h,
                Text.literal("@"), b -> {
                    if (this.client != null) this.client.setScreen(new FriendRequestsScreen(this));
                }));

        List<String> friends = FriendManager.getFriendsSorted();
        int maxRows = Math.max(6, Math.min(14, (this.height - 115) / gap));
        int maxOffset = Math.max(0, friends.size() - maxRows);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        for (int i = 0; i < maxRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= friends.size()) break;
            String f = friends.get(idx);
            boolean online = FriendManager.isOnline(f);
            String dot = online ? "§a● " : "§7○ ";
            this.addDrawableChild(ModernButtonWidget.create(x, y + 34 + i * gap, w, h,
                    Text.literal(dot + f),
                    b -> { if (this.client != null) this.client.setScreen(new FriendChatScreen(this, f)); }));
        }

        this.addDrawableChild(ModernButtonWidget.success(x + w - 34, this.height - 58, 34, 24,
                Text.literal("+"), b -> {
                    if (this.client != null) this.client.setScreen(new FriendAddScreen(this));
                }));

        this.addDrawableChild(ModernButtonWidget.create(x, this.height - 32, w, 22,
                Text.literal("← Kapat"), b -> {
                    if (this.client != null) this.client.setScreen(parent);
                }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxRows = Math.max(6, Math.min(14, (this.height - 115) / 24));
        int maxOffset = Math.max(0, FriendManager.getFriendsSorted().size() - maxRows);
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
        GuiHelper.drawPanel(context, 10, 10, 286, this.height - 20);
        context.drawTextWithShadow(this.textRenderer, Text.literal("≡ Arkadaş Menüsü"), 22, 18, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
