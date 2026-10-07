package de.hugosmp.auktion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class HugoAuktionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            AuctionManager.get().handleChat(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> AuctionManager.get().tick(client));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("auktion").executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> openScreen(mc, new AuctionScreen()));
                    return 1;
                })));
    }

    static void openScreen(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }
}
