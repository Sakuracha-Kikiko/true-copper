package com.sakuracha.truecopper.sound;

import com.sakuracha.truecopper.network.ExtendedSoundPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Sends an {@link ExtendedSoundPayload} to every player inside a custom radius. The sound is
 * played client side with no attenuation, so the radius here is the only limit, and the client
 * fades it out towards the edge.
 */
public final class SoundBroadcast {

    private SoundBroadcast() {
    }

    /**
     * @param except      a player that should not receive the packet, usually the attacker
     * @param minDistance players closer than this are skipped (squared distance is used)
     */
    public static void send(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch,
                            float range, SoundSource source, @Nullable ServerPlayer except,
                            double minDistance) {
        if (volume <= 0.0F || range <= 0.0F) {
            return;
        }
        double rangeSqr = (double) range * range;
        double minSqr = minDistance * minDistance;
        for (ServerPlayer player : level.players()) {
            if (player == except) {
                continue;
            }
            double distanceSqr = player.distanceToSqr(pos.x, pos.y, pos.z);
            if (distanceSqr > rangeSqr || distanceSqr < minSqr) {
                continue;
            }
            PacketDistributor.sendToPlayer(player, new ExtendedSoundPayload(
                    sound.getLocation(), pos.x, pos.y, pos.z, volume, pitch, range,
                    source.ordinal()));
        }
    }
}
