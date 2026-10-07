package de.hugosmp.auktion;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod-Menu-Anbindung: Der "Konfigurieren"-Knopf oeffnet das Auktionsmenue. */
public class HugoAuktionModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AuctionScreen(parent);
    }
}
