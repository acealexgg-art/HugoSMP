package de.hugosmp.auktion;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Erkennt AUSSCHLIESSLICH die exakte Server-Nachricht fuer EMPFANGENE Zahlungen:
 * "[HugoSMP] Du hast $1 von .AceMiragg erhalten."
 * Bedrock-Spieler haben einen Punkt vor dem Namen (".Name"), Java-Spieler den normalen Namen.
 *
 * - Voller Match (^...$), nichts davor, nichts dahinter, kein trim().
 * - Wird nur auf System-/Server-Nachrichten angewendet (nicht auf signierte Spieler-Chats).
 */
public final class PaymentParser {

    private static final Pattern PATTERN = Pattern.compile(
            "^\\[HugoSMP\\] Du hast \\$(\\d{1,3}(?:\\.\\d{3})*|\\d+) von (\\.?[A-Za-z0-9_]{3,16}) erhalten\\.$");

    private PaymentParser() {
    }

    public static Optional<ParsedPayment> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        Matcher m = PATTERN.matcher(raw);
        if (!m.matches()) {
            return Optional.empty();
        }
        try {
            long amount = Long.parseLong(m.group(1).replace(".", ""));
            if (amount <= 0) {
                return Optional.empty();
            }
            return Optional.of(new ParsedPayment(m.group(2), amount));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public record ParsedPayment(String player, long amount) {
    }
}
