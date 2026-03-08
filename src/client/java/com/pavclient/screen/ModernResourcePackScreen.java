package com.pavclient.screen;

import com.pavclient.gui.GuiHelper;
import com.pavclient.gui.ModernButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern kaynak paketi ekrani.
 * "kaynakpaketleri" klasorunden okur (resourcepacks degil).
 */
public class ModernResourcePackScreen extends Screen {

    private final Screen parent;
    private final Path packDir;
    private List<String> packNames = new ArrayList<>();
    private int scrollOffset = 0;

    public ModernResourcePackScreen(Screen parent) {
        super(Text.literal("Kaynak Paketleri"));
        this.parent = parent;
        // Oyun dizininde "kaynakpaketleri" klasoru
        this.packDir = net.minecraft.client.MinecraftClient.getInstance().runDirectory.toPath().resolve("kaynakpaketleri");
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int bw = 240;
        int bh = 22;

        // Klasoru olustur
        try {
            Files.createDirectories(packDir);
        } catch (IOException ignored) {}

        // Paketleri oku
        packNames.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packDir)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.endsWith(".zip") || Files.isDirectory(entry)) {
                    packNames.add(name);
                }
            }
        } catch (IOException ignored) {}

        // Paket listesi
        int startY = 65;
        int maxVisible = Math.min(packNames.size(), 8);
        for (int i = 0; i < maxVisible; i++) {
            int idx = i + scrollOffset;
            if (idx >= packNames.size()) break;
            String packName = packNames.get(idx);
            this.addDrawableChild(ModernButtonWidget.create(
                    cx - bw / 2, startY + i * 26, bw, bh,
                    Text.literal("\u2261 " + packName),
                    btn -> {} // Bilgi gostermek icin
            ));
        }

        // Bos ise bilgi
        if (packNames.isEmpty()) {
            // render'da mesaj gosterecegiz
        }

        // Klasoru ac butonu
        this.addDrawableChild(ModernButtonWidget.success(
                cx - bw / 2, this.height - 60, bw, bh,
                Text.literal("\u2606 Klas\u00f6r\u00fc A\u00e7"),
                btn -> net.minecraft.util.Util.getOperatingSystem().open(packDir.toFile())
        ));

        // Geri butonu
        this.addDrawableChild(ModernButtonWidget.create(
                cx - bw / 2, this.height - 34, bw, bh,
                Text.literal("\u2190 Geri"),
                btn -> { if (this.client != null) this.client.setScreen(parent); }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        GuiHelper.drawClientBackground(context, this.width, this.height);
        int cx = this.width / 2;
        GuiHelper.drawPanel(context, cx - 140, 18, 280, this.height - 36);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u2261 Kaynak Paketleri"), cx, 28, 0xFFB0BEC5);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a77Klas\u00f6r: kaynakpaketleri/"), cx, 44, 0xFF888888);

        if (packNames.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("\u00a78Kaynak paketi bulunamad\u0131"), cx, 90, 0xFF666666);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("\u00a78.zip veya klas\u00f6r ekleyin"), cx, 104, 0xFF555555);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--;
            init();
        } else if (verticalAmount < 0 && scrollOffset < packNames.size() - 8) {
            scrollOffset++;
            init();
        }
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
