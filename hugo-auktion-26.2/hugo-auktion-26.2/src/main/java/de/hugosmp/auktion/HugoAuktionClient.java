package de.hugosmp.auktion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class HugoAuktionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Nur GAME = System-/Server-Nachrichten. Signierte Spieler-Chats laufen ueber CHAT und
        // werden hier bewusst NICHT ausgewertet.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return; // Actionbar ignorieren
            AuctionManager.get().handleChat(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> AuctionManager.get().tick(client));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("auktion").executes(ctx -> {
                    Minecraft mc = ctx.getSource().getClient();
                    mc.execute(() -> openScreen(mc, new AuctionScreen()));
                    return 1;
                })));
    }

    /**
     * Seit 26.2 liegt setScreen in der Gui-Klasse. Falls es dort bei dir anders heisst
     * (z. B. setScreenAndShow), nur diese eine Zeile anpassen.
     */
    static void openScreen(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }
}
