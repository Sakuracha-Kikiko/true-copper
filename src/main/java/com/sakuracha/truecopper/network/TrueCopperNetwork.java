package com.sakuracha.truecopper.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Networking: one clientbound packet, only used for the attacker's own slash sound.
 */
public final class TrueCopperNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private TrueCopperNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TrueCopperNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(SlashSoundPayload.TYPE, SlashSoundPayload.STREAM_CODEC,
                SlashSoundPayload::handle);
    }
}
