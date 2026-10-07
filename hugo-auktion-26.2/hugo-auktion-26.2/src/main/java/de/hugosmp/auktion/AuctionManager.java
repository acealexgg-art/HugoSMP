package de.hugosmp.auktion;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AuctionManager {

    public enum State { IDLE, RUNNING, ENDED }

    private static final AuctionManager INSTANCE = new AuctionManager();

    public static AuctionManager get() {
        return INSTANCE;
    }

    private State state = State.IDLE;
    private ItemStack item = ItemStack.EMPTY;
    private long startTime;
    private long endTime;
    /** Neueste zuerst. */
    private final List<Payment> payments = new ArrayList<>();

    private AuctionManager() {
    }

    public State state() { return state; }
    public ItemStack item() { return item; }
    public List<Payment> payments() { return payments; }
    public long remainingMs() { return Math.max(0, endTime - System.currentTimeMillis()); }

    public float progress() {
        long total = endTime - startTime;
        if (total <= 0) return 0f;
        return Math.min(1f, Math.max(0f, 1f - (float) remainingMs() / total));
    }

    public void start(ItemStack stack, int seconds) {
        if (state == State.RUNNING || stack.isEmpty()) return;
        this.item = stack.copy();
        this.payments.clear();
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + seconds * 1000L;
        this.state = State.RUNNING;
    }

    public void cancel() {
        state = State.IDLE;
        item = ItemStack.EMPTY;
        payments.clear();
    }

    /** Hoechste Einzelzahlung, die VOR Ablauf einging. */
    public Optional<Payment> topPayment() {
        Payment best = null;
        for (Payment p : payments) {
            if (p.late()) continue;
            if (best == null || p.amount() > best.amount()) best = p;
        }
        return Optional.ofNullable(best);
    }

    public long total() {
        long sum = 0;
        for (Payment p : payments) sum += p.amount();
        return sum;
    }

    /** Wird fuer jede Server-/System-Nachricht aufgerufen. */
    public void handleChat(String raw) {
        if (state == State.IDLE) return;
        PaymentParser.parse(raw).ifPresent(pp -> {
            boolean late = state == State.ENDED || System.currentTimeMillis() > endTime;
            payments.add(0, new Payment(pp.player(), pp.amount(), System.currentTimeMillis(), late));
        });
    }

    /** Pro Client-Tick: Auktion beenden, wenn die Zeit abgelaufen ist. */
    public void tick(Minecraft client) {
        if (state == State.RUNNING && System.currentTimeMillis() >= endTime) {
            state = State.ENDED;
            if (client.player != null) {
                Component msg = topPayment()
                        .map(p -> Component.literal("[Auktion] Beendet! Gewinner: " + p.player() + " mit $" + fmt(p.amount()))
                                .withStyle(ChatFormatting.GOLD))
                        .orElse(Component.literal("[Auktion] Beendet! Keine Gebote.").withStyle(ChatFormatting.GRAY));
              client.player.sendSystemMessage(msg);
            }
        }
    }

    public static String fmt(long amount) {
        return String.format(Locale.GERMANY, "%,d", amount);
    }
}
