package com.sakuracha.truecopper.event;

import com.sakuracha.truecopper.TrueCopper;
import com.sakuracha.truecopper.damage.DeferredDamage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = TrueCopper.MOD_ID)
public final class ServerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("true_copper");

    private ServerEvents() {
    }

    /**
     * Loads the patched class right away, so a Zenith Sword update that changes the damage
     * handler fails loudly at startup instead of silently losing the feature mid-fight.
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        try {
            Class.forName("com.oceanscenery.zenith.util.DamageHandler", false, TrueCopper.class.getClassLoader());
            LOGGER.info("Zenith Sword damage handler hooked");
        } catch (Throwable throwable) {
            LOGGER.error("Could not hook The Zenith Sword - is this version still compatible?", throwable);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DeferredDamage.serverTick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DeferredDamage.clear();
    }
}
