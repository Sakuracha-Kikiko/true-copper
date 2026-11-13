package com.sakuracha.truecopper.sound;

import com.sakuracha.truecopper.TrueCopper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events added by True Copper.
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, TrueCopper.MOD_ID);

    /** Played every time a Zenith Sword swing actually goes off (player or maid). */
    public static final DeferredHolder<SoundEvent, SoundEvent> ZENITH_SLASH = SOUNDS.register(
            "zenith_slash",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(TrueCopper.MOD_ID, "zenith_slash")));

    private ModSounds() {
    }
}
