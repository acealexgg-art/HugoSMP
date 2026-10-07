package de.hugosmp.auktion;

/** Eine erkannte Zahlung. {@code late} = kam nach Ablauf des Timers (zaehlt nur zum Gesamteingang). */
public record Payment(String player, long amount, long timestamp, boolean late) {
}
