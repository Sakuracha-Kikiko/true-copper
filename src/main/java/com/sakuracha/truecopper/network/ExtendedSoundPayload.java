package com.sakuracha.truecopper.network;

import com.sakuracha.truecopper.TrueCopper;
import com.sakuracha.truecopper.client.ClientExtendedSound;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A sound that the client plays with no distance attenuation and a volume calculated from its
 * own distance to the source. Vanilla sound packets are dropped by the server for players
 * outside the sound event's range (16 blocks for most sounds), so a 200 block sword could never
 * be heard at the distance it hits. This packet carries the falloff parameters instead.
 */
public record ExtendedSoundPayload(ResourceLocation sound, double x, double y, double z,
                                   float volume, float pitch, float range, int source)
        implements CustomPacketPayload {

    public static final Type<ExtendedSoundPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrueCopper.MOD_ID, "extended_sound"));

    public static final StreamCodec<FriendlyByteBuf, ExtendedSoundPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ResourceLocation.STREAM_CODEC.encode(buf, payload.sound());
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
                buf.writeFloat(payload.volume());
                buf.writeFloat(payload.pitch());
                buf.writeFloat(payload.range());
                buf.writeVarInt(payload.source());
            },
            buf -> new ExtendedSoundPayload(
                    ResourceLocation.STREAM_CODEC.decode(buf),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExtendedSoundPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientExtendedSound.play(payload));
    }
}
