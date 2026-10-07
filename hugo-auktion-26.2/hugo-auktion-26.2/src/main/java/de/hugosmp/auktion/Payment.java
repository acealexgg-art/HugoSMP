package de.hugosmp.auktion;

/**
 * Eine erkannte Zahlung.
 * late     = kam nach Ablauf des Timers
 * belowMin = lag unter dem Mindestgebot
 * Beides zaehlt nur zum Gesamteingang, nicht als Gebot.
 */
public record Payment(String player, long amount, long timestamp, boolean late, boolean belowMin) {
}
