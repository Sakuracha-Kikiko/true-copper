package com.sakuracha.truecopper.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only wiring, kept in its own class so the dedicated server never loads it.
 */
public final class ClientSetup {

    private ClientSetup() {
    }

    /**
     * Registers NeoForge's built-in configuration screen, so every option can be edited
     * from the mod list instead of the config file.
     */
    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
