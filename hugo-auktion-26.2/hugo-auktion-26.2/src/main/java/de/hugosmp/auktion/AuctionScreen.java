package de.hugosmp.auktion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AuctionScreen extends Screen {

    private static final int PANEL = 0xCC0E0E16;
    private static final int PANEL_ROW = 0x40FFFFFF;
    private static final int ACCENT = 0xFFB060FF;
    private static final int GOLD = 0xFFFFE040;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int DARK_GRAY = 0xFF777777;

    private final Screen parent;
    private ItemStack scanned = ItemStack.EMPTY;
    private List<Component> scannedLines = List.of();
    private int page = 0;

    private Button scanBtn, durationBtn, startBtn, cancelBtn, newBtn, olderBtn, newerBtn;
    private EditBox minBidBox;

    public AuctionScreen(Screen parent) {
        super(Component.literal("Hugo Auktion"));
        this.parent = parent;
    }

    // ---------- Layout-Helfer ----------

    private int top() { return 30; }
    private int bottom() { return height - 50; }
    private int leftX() { return 20; }
    private int leftW() { return width / 2 - 25; }
    private int rightX() { return width / 2 + 5; }
    private int rightW() { return width / 2 - 25; }
    private int rowY1() { return bottom() - 78; }
    private int rowY2() { return bottom() - 54; }
    private int rowY3() { return bottom() - 30; }

    private int rowsPerPage() {
        return Math.max(1, (bottom() - 20 - (top() + 60)) / 26);
    }

    // ---------- Item scannen ----------

    private void scan() {
        if (minecraft != null && minecraft.player != null) {
            scanned = minecraft.player.getMainHandItem().copy();
            scannedLines = ItemInfo.lines(scanned);
        } else {
            scanned = ItemStack.EMPTY;
            scannedLines = List.of();
        }
    }

    // ---------- Widgets ----------

    @Override
    protected void init() {
        if (scanned.isEmpty()) {
            scan(); // direkt das Item in der Hand scannen
        }
        int lx = leftX(), lw = leftW();
        int rx = rightX(), rw = rightW();
        int by = height - 40;
        int half = (lw - 30) / 2;

        scanBtn = addRenderableWidget(Button.builder(Component.literal("Item einscannen"), b -> scan())
                .bounds(lx + 10, rowY1(), half, 20).build());

        durationBtn = addRenderableWidget(Button.builder(durationText(), b -> {
            Config.durationIndex = (Config.durationIndex + 1) % Config.DURATIONS.length;
            b.setMessage(durationText());
        }).bounds(lx + 20 + half, rowY1(), half, 20).build());

        int labelW = font.width("Mindestgebot: $");
        minBidBox = new EditBox(font, lx + 14 + labelW, rowY2(), lw - 24 - labelW, 20, Component.literal("Mindestgebot"));
        minBidBox.setMaxLength(12);
           minBidBox.setValue(Long.toString(Config.minBid));
        minBidBox.setResponder(s -> {
            String digits = s.replaceAll("[^0-9]", "");
            if (!digits.equals(s)) {
                minBidBox.setValue(digits);
                return;
            }
            try {
                Config.minBid = digits.isEmpty() ? 0 : Long.parseLong(digits);
            } catch (NumberFormatException e) {
                Config.minBid = 0;
            }
        });
        addRenderableWidget(minBidBox);

        startBtn = addRenderableWidget(Button.builder(Component.literal("Auktion starten"), b -> {
            if (!scanned.isEmpty()) {
                Config.save();
                AuctionManager.get().start(scanned, scannedLines, Config.DURATIONS[Config.durationIndex], Config.minBid);
                page = 0;
            }
        }).bounds(lx + 10, rowY3(), lw - 20, 20).build());

        cancelBtn = addRenderableWidget(Button.builder(Component.literal("Auktion abbrechen"), b ->
                AuctionManager.get().cancel()).bounds(lx, by, 150, 20).build());

        newBtn = addRenderableWidget(Button.builder(Component.literal("Neue Auktion"), b -> {
            AuctionManager.get().cancel();
            scan();
        }).bounds(lx, by, 150, 20).build());

        olderBtn = addRenderableWidget(Button.builder(Component.literal("Älter"), b -> page++)
                .bounds(rx, by, 70, 20).build());
        newerBtn = addRenderableWidget(Button.builder(Component.literal("Neuer"), b -> page = Math.max(0, page - 1))
                .bounds(rx + 75, by, 70, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
                .bounds(rx + rw - 80, by, 80, 20).build());
    }

    private Component durationText() {
        int s = Config.DURATIONS[Config.durationIndex];
        return Component.literal("Dauer: " + (s >= 60 ? (s / 60) + " Min" : s + " Sek"));
    }

    @Override
    public void tick() {
        AuctionManager.State st = AuctionManager.get().state();
        boolean idle = st == AuctionManager.State.IDLE;
        scanBtn.visible = idle;
        durationBtn.visible = idle;
        startBtn.visible = idle;
        minBidBox.visible = idle;
        startBtn.active = !scanned.isEmpty();
        cancelBtn.visible = st == AuctionManager.State.RUNNING;
        newBtn.visible = st == AuctionManager.State.ENDED;

        int per = rowsPerPage();
        int pages = Math.max(1, (int) Math.ceil(AuctionManager.get().payments().size() / (double) per));
        page = Math.min(page, pages - 1);
        olderBtn.active = page < pages - 1;
        newerBtn.active = page > 0;
    }

    @Override
    public void onClose() {
        Config.save();
        HugoAuktionClient.openScreen(minecraft, parent);
    }

    // ---------- Zeichen-Helfer: bei API-Aenderungen nur hier anpassen ----------

    private void text(GuiGraphicsExtractor g, Component c, int x, int y, int color) {
        g.text(font, c, x, y, color, true);
    }

    private void text(GuiGraphicsExtractor g, String s, int x, int y, int color) {
        text(g, Component.literal(s), x, y, color);
    }

    private void centered(GuiGraphicsExtractor g, Component c, int cx, int y, int color) {
        g.centeredText(font, c, cx, y, color);
    }

    private void centered(GuiGraphicsExtractor g, String s, int cx, int y, int color) {
        centered(g, Component.literal(s), cx, y, color);
    }

    private void bigItem(GuiGraphicsExtractor g, ItemStack stack, int centerX, int y, float scale) {
        if (stack.isEmpty()) return;
        var m = g.pose();
        m.pushMatrix();
        m.translate(centerX - 8 * scale, y);
        m.scale(scale, scale);
        g.item(stack, 0, 0);
        m.popMatrix();
    }

    /** Zeichnet Verzauberungen/Lore/Signatur des Items als Liste. */
    private void drawInfo(GuiGraphicsExtractor g, List<Component> lines, int x, int y, int maxY, int panelX2) {
        if (lines.isEmpty() || y + 10 > maxY) return;
        g.enableScissor(x - 4, y - 2, panelX2 - 6, maxY);
        int cy = y;
        if (ItemInfo.looksSigned(lines)) {
            text(g, "✔ Signatur erkannt", x, cy, GOLD);
            cy += 11;
        }
        int shown = 0;
        for (Component line : lines) {
            if (cy + 10 > maxY) break;
            text(g, line, x, cy, WHITE);
            cy += 10;
            shown++;
        }
        g.disableScissor();
        if (shown < lines.size()) {
            // Hinweis, falls nicht alles in den Platz passt
            text(g, "+" + (lines.size() - shown) + " weitere Zeilen", x, Math.min(cy, maxY - 10), DARK_GRAY);
        }
    }

    // ---------- Rendern ----------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        AuctionManager am = AuctionManager.get();
        int lx = leftX(), lw = leftW();
        int rx = rightX(), rw = rightW();
        int top = top(), bottom = bottom();

        g.fill(lx, top, lx + lw, bottom, PANEL);
        g.fill(rx, top, rx + rw, bottom, PANEL);
        g.fill(lx, top, lx + lw, top + 2, ACCENT);
        g.fill(rx, top, rx + rw, top + 2, ACCENT);

        centered(g, title, width / 2, 12, WHITE);

        int cx = lx + lw / 2;
        if (am.state() == AuctionManager.State.IDLE) {
            centered(g, "Neue Auktion", cx, top + 10, WHITE);
            bigItem(g, scanned, cx, top + 24, 2f);
            if (scanned.isEmpty()) {
                centered(g, "Kein Item in der Hand", cx, top + 62, RED);
            } else {
                centered(g, scanned.getHoverName(), cx, top + 62, WHITE);
                centered(g, "Menge: " + scanned.getCount(), cx, top + 74, GRAY);
                drawInfo(g, scannedLines, lx + 12, top + 90, rowY1() - 6, lx + lw);
            }
            text(g, "Mindestgebot: $", lx + 10, rowY2() + 6, GRAY);
        } else {
            centered(g, "Laufende Auktion", cx, top + 10, WHITE);
            var top1 = am.topPayment();
            centered(g, top1.map(Payment::player).orElse("Noch kein gültiges Gebot"), cx, top + 24, WHITE);
            centered(g, "$" + AuctionManager.fmt(top1.map(Payment::amount).orElse(0L)), cx, top + 36, GOLD);
            bigItem(g, am.item(), cx, top + 50, 2f);
            centered(g, am.item().getHoverName(), cx, top + 88, WHITE);
            centered(g, "Menge: " + am.item().getCount(), cx, top + 100, GRAY);
            drawInfo(g, am.itemLines(), lx + 12, top + 114, bottom - 62, lx + lw);

            centered(g, "Mindestgebot: $" + AuctionManager.fmt(am.minBid()), cx, bottom - 50, GRAY);
            boolean running = am.state() == AuctionManager.State.RUNNING;
            long s = am.remainingMs() / 1000;
            String time = running ? String.format("%02d:%02d", s / 60, s % 60) : "BEENDET";
            centered(g, time, cx, bottom - 36, running ? RED : GRAY);

            int barX = lx + 10, barW = lw - 20, barY = bottom - 16;
            g.fill(barX, barY, barX + barW, barY + 4, 0x55FFFFFF);
            g.fill(barX, barY, barX + (int) (barW * am.progress()), barY + 4, ACCENT);
        }

        // Rechte Seite: Gesamteingang + Zahlungsliste
        int rcx = rx + rw / 2;
        centered(g, "Gesamteingang", rcx, top + 12, WHITE);
        centered(g, "$" + AuctionManager.fmt(am.total()), rcx, top + 26, GREEN);
        text(g, am.payments().size() + " Zahlungen", rx + 10, top + 44, GRAY);

        List<Payment> list = am.payments();
        int per = rowsPerPage();
        int from = page * per;
        int y = top + 60;
        for (int i = from; i < Math.min(list.size(), from + per); i++) {
            Payment p = list.get(i);
            int nameColor = p.late() ? RED : (p.belowMin() ? DARK_GRAY : WHITE);
            int amountColor = p.late() ? RED : (p.belowMin() ? DARK_GRAY : GOLD);
            if ((i % 2) == 0) g.fill(rx + 6, y - 2, rx + rw - 6, y + 22, PANEL_ROW);
            text(g, p.player(), rx + 12, y, nameColor);
            text(g, "$" + AuctionManager.fmt(p.amount()), rx + 12, y + 11, amountColor);
            y += 26;
        }
        int pages = Math.max(1, (int) Math.ceil(list.size() / (double) per));
        text(g, (page + 1) + "/" + pages, rx + rw - 30, bottom - 12, GRAY);

        centered(g, "Rot = nach Ablauf, Grau = unter Mindestgebot; beides zählt nur zum Gesamteingang.",
                width / 2, height - 12, GRAY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
