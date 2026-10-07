package de.hugosmp.auktion;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * Liest die Tooltip-Zeilen eines Items aus (Verzauberungen, Lore/Signatur von /sign, ...),
 * ohne die erste Zeile (Name).
 */
public final class ItemInfo {

    private ItemInfo() {
    }

    public static List<Component> lines(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (stack.isEmpty() || mc.player == null || mc.level == null) {
            return List.of();
        }
        List<Component> all = stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL);
        if (all.size() <= 1) {
            return List.of();
        }
        return new ArrayList<>(all.subList(1, all.size()));
    }

    /** Grobe Erkennung: irgendeine Zeile enthaelt "sign" (signiert/signed/signature). */
    public static boolean looksSigned(List<Component> lines) {
        for (Component c : lines) {
            if (c.getString().toLowerCase().contains("sign")) {
                return true;
            }
        }
        return false;
    }
}
