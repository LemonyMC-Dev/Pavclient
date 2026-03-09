package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import com.pavclient.network.FriendsChannel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FriendAddScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;

    public FriendAddScreen(Screen parent) {
        super(Text.literal("Arkadaş Ekle"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int w = 260;

        nameField = new TextFieldWidget(this.textRenderer, cx - w / 2, 70, w, 22, Text.literal("İsim"));
        nameField.setPlaceholder(Text.literal("Oyuncu ismi yaz..."));
        this.addDrawableChild(nameField);

        this.addDrawableChild(ModernButtonWidget.success(cx - w / 2, 104, w, 24,
                Text.literal("Arkadaşlık İsteği Gönder"), b -> {
                    String target = nameField.getText().trim();
                    if (!target.isEmpty()) {
                        FriendsChannel.sendFriendRequest(target);
                        if (this.client != null) this.client.setScreen(parent);
                    }
                }));

        this.addDrawableChild(ModernButtonWidget.create(cx - w / 2, this.height - 32, w, 22,
                Text.literal("← Geri"), b -> { if (this.client != null) this.client.setScreen(parent); }));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return nameField != null && nameField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return nameField != null && nameField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 150, 20, 300, this.height - 40);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("+ Arkadaş Ekle"), cx, 34, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
