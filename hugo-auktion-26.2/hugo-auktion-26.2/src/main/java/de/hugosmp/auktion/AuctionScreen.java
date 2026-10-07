package de.hugosmp.auktion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AuctionScreen extends Screen {

    private static final int[] DURATIONS = {30, 60, 120, 300, 600, 900};
    private static int durationIndex = 1;

    private static final int PANEL = 0xCC0E0E16;
    private static final int PANEL_ROW = 0x40FFFFFF;
    private static final int ACCENT = 0xFFB060FF;
    private static final int GOLD = 0xFFFFE040;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int ROWS_PER_PAGE = 7;

    private ItemStack scanned = ItemStack.EMPTY;
    private int page = 0;

    private Button scanBtn, durationBtn, startBtn, cancelBtn, newBtn, olderBtn, newerBtn;

    public AuctionScreen() {
        super(Component.literal("Hugo Auktion"));
    }

    private ItemStack handItem() {
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player.getMainHandItem().copy();
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        if (scanned.isEmpty()) {
            scanned = handItem(); // direkt das Item in der Hand scannen
        }
        int leftX = 20, leftW = width / 2 - 25;
        int rightX = width / 2 + 5, rightW = width / 2 - 25;
        int by = height - 40;

        scanBtn = addRenderableWidget(Button.builder(Component.literal("Item einscannen"), b -> scanned = handItem())
                .bounds(leftX + 10, 150, leftW - 20, 20).build());

        durationBtn = addRenderableWidget(Button.builder(durationText(), b -> {
            durationIndex = (durationIndex + 1) % DURATIONS.length;
            b.setMessage(durationText());
        }).bounds(leftX + 10, 176, leftW - 20, 20).build());

        startBtn = addRenderableWidget(Button.builder(Component.literal("Auktion starten"), b -> {
            if (!scanned.isEmpty()) {
                AuctionManager.get().start(scanned, DURATIONS[durationIndex]);
                page = 0;
            }
        }).bounds(leftX + 10, 202, leftW - 20, 20).build());

        cancelBtn = addRenderableWidget(Button.builder(Component.literal("Auktion abbrechen"), b ->
                AuctionManager.get().cancel()).bounds(leftX, by, 150, 20).build());

        newBtn = addRenderableWidget(Button.builder(Component.literal("Neue Auktion"), b -> {
            AuctionManager.get().cancel();
            scanned = handItem();
        }).bounds(leftX, by, 150, 20).build());

        olderBtn = addRenderableWidget(Button.builder(Component.literal("Älter"), b -> page++)
                .bounds(rightX, by, 70, 20).build());
        newerBtn = addRenderableWidget(Button.builder(Component.literal("Neuer"), b -> page = Math.max(0, page - 1))
                .bounds(rightX + 75, by, 70, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
                .bounds(rightX + rightW - 80, by, 80, 20).build());
    }

    private Component durationText() {
        int s = DURATIONS[durationIndex];
        return Component.literal("Dauer: " + (s >= 60 ? (s / 60) + " Min" : s + " Sek"));
    }

    @Override
    public void tick() {
        AuctionManager.State st = AuctionManager.get().state();
        boolean idle = st == AuctionManager.State.IDLE;
        scanBtn.visible = idle;
        durationBtn.visible = idle;
        startBtn.visible = idle;
        startBtn.active = !scanned.isEmpty();
        cancelBtn.visible = st == AuctionManager.State.RUNNING;
        newBtn.visible = st == AuctionManager.State.ENDED;

        int pages = Math.max(1, (int) Math.ceil(AuctionManager.get().payments().size() / (double) ROWS_PER_PAGE));
        page = Math.min(page, pages - 1);
        olderBtn.active = page < pages - 1;
        newerBtn.active = page > 0;
    }

    // --- Zeichen-Helfer: bei API-Aenderungen nur hier anpassen ---

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

    private void bigItem(GuiGraphicsExtractor g, ItemStack stack, int centerX, int y) {
        if (stack.isEmpty()) return;
        float scale = 3f;
        var m = g.pose();
        m.pushMatrix();
        m.translate(centerX - 8 * scale, y);
        m.scale(scale, scale);
        g.item(stack, 0, 0);
        m.popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        AuctionManager am = AuctionManager.get();
        int leftX = 20, leftW = width / 2 - 25;
        int rightX = width / 2 + 5, rightW = width / 2 - 25;
        int top = 30, bottom = height - 50;

        g.fill(leftX, top, leftX + leftW, bottom, PANEL);
        g.fill(rightX, top, rightX + rightW, bottom, PANEL);
        g.fill(leftX, top, leftX + leftW, top + 2, ACCENT);
        g.fill(rightX, top, rightX + rightW, top + 2, ACCENT);

        centered(g, title, width / 2, 12, WHITE);

        int cx = leftX + leftW / 2;
        if (am.state() == AuctionManager.State.IDLE) {
            centered(g, "Neue Auktion", cx, top + 12, WHITE);
            bigItem(g, scanned, cx, top + 40);
            if (scanned.isEmpty()) {
                centered(g, "Kein Item in der Hand", cx, top + 90, RED);
            } else {
                centered(g, scanned.getHoverName(), cx, top + 90, WHITE);
                centered(g, "Menge: " + scanned.getCount(), cx, top + 102, GRAY);
            }
        } else {
            centered(g, "Laufende Auktion", cx, top + 12, WHITE);
            var top1 = am.topPayment();
            centered(g, top1.map(Payment::player).orElse("Noch kein Gebot"), cx, top + 28, WHITE);
            centered(g, "$" + AuctionManager.fmt(top1.map(Payment::amount).orElse(0L)), cx, top + 42, GOLD);
            bigItem(g, am.item(), cx, top + 62);
            centered(g, am.item().getHoverName(), cx, top + 112, WHITE);
            centered(g, "Menge: " + am.item().getCount(), cx, top + 124, GRAY);

            boolean running = am.state() == AuctionManager.State.RUNNING;
            long s = am.remainingMs() / 1000;
            String time = running ? String.format("%02d:%02d", s / 60, s % 60) : "BEENDET";
            centered(g, time, cx, top + 142, running ? RED : GRAY);

            int barX = leftX + 10, barW = leftW - 20, barY = bottom - 14;
            g.fill(barX, barY, barX + barW, barY + 4, 0x55FFFFFF);
            g.fill(barX, barY, barX + (int) (barW * am.progress()), barY + 4, ACCENT);
        }

        // Rechte Seite: Gesamteingang + Zahlungsliste
        int rcx = rightX + rightW / 2;
        centered(g, "Gesamteingang", rcx, top + 12, WHITE);
        centered(g, "$" + AuctionManager.fmt(am.total()), rcx, top + 26, GREEN);
        text(g, am.payments().size() + " Zahlungen", rightX + 10, top + 44, GRAY);

        List<Payment> list = am.payments();
        int from = page * ROWS_PER_PAGE;
        int y = top + 60;
        for (int i = from; i < Math.min(list.size(), from + ROWS_PER_PAGE); i++) {
            Payment p = list.get(i);
            if ((i % 2) == 0) g.fill(rightX + 6, y - 2, rightX + rightW - 6, y + 22, PANEL_ROW);
            text(g, p.player(), rightX + 12, y, p.late() ? RED : WHITE);
            text(g, "$" + AuctionManager.fmt(p.amount()), rightX + 12, y + 11, p.late() ? RED : GOLD);
            y += 26;
        }
        int pages = Math.max(1, (int) Math.ceil(list.size() / (double) ROWS_PER_PAGE));
        text(g, (page + 1) + "/" + pages, rightX + rightW - 30, bottom - 12, GRAY);

        centered(g, "Rot = nach Ablauf; zählt nur zum Gesamteingang.", width / 2, height - 12, GRAY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
