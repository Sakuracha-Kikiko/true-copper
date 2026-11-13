package com.sakuracha.truecopper.client;

import com.sakuracha.truecopper.network.ExtendedSoundPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Client side of {@link ExtendedSoundPayload}. The sound is played with
 * {@link SoundInstance.Attenuation#NONE} (so OpenAL still positions it, but does not fade it),
 * and the fade is applied by hand: linear from the source out to the payload's range.
 */
public final class ClientExtendedSound {

    private ClientExtendedSound() {
    }

    public static void play(ExtendedSoundPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || payload.range() <= 0.0F || payload.volume() <= 0.0F) {
            return;
        }
        double distance = Math.sqrt(
                minecraft.player.distanceToSqr(payload.x(), payload.y(), payload.z()));
        float gain = Mth.clamp(1.0F - (float) (distance / payload.range()), 0.0F, 1.0F);
        float volume = payload.volume() * gain;
        if (volume <= 0.001F) {
            return;
        }
        SoundSource[] sources = SoundSource.values();
        int source = Mth.clamp(payload.source(), 0, sources.length - 1);
        minecraft.getSoundManager().play(new SimpleSoundInstance(
                payload.sound(), sources[source], volume, payload.pitch(),
                SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE,
                payload.x(), payload.y(), payload.z(), false));
    }
}
