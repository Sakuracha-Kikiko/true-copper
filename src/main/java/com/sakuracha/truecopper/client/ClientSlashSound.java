package com.sakuracha.truecopper.client;

import com.sakuracha.truecopper.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Plays the slash sound for the player who swung: relative to the listener, no attenuation and no
 * stereo panning, so it feels like it comes from the swing itself instead of the world.
 */
public final class ClientSlashSound {

    private ClientSlashSound() {
    }

    public static void play(float volume) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || volume <= 0.0F) {
            return;
        }
        minecraft.getSoundManager().play(new SimpleSoundInstance(
                ModSounds.ZENITH_SLASH.get().getLocation(),
                SoundSource.PLAYERS,
                volume,
                1.0F,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true));
    }
}
