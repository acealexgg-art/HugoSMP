package de.hugosmp.auktion;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class HugoAuktionClient implements ClientModInitializer {

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        // Keybind: in den Minecraft-Einstellungen unter Steuerung -> "Hugo Auktion" frei waehlbar
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("hugoauktion", "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hugoauktion.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category));

        // Nur GAME = System-/Server-Nachrichten. Signierte Spieler-Chats laufen ueber CHAT und
        // werden hier bewusst NICHT ausgewertet.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return; // Actionbar ignorieren
            AuctionManager.get().handleChat(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AuctionManager.get().tick(client);
            while (openKey.consumeClick()) {
                if (client.player != null) {
                    openScreen(client, new AuctionScreen(null));
                }
            }
        });
    }

    /**
     * Seit 26.2 liegt setScreen in der Gui-Klasse. Falls es dort bei dir anders heisst
     * (z. B. setScreenAndShow), nur diese eine Zeile anpassen.
     */
    static void openScreen(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }
}
