package com.sakuracha.truecopper;

import com.sakuracha.truecopper.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * True Copper - quality of life features and compatibilities for The Zenith Sword.
 */
@Mod(TrueCopper.MOD_ID)
public final class TrueCopper {

    public static final String MOD_ID = "true_copper";

    public TrueCopper(ModContainer container, IEventBus modEventBus) {
        ModSounds.SOUNDS.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, TrueCopperConfig.SPEC);
    }
}
