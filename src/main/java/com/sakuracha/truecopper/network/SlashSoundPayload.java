package com.sakuracha.truecopper.network;

import com.sakuracha.truecopper.TrueCopper;
import com.sakuracha.truecopper.client.ClientSlashSound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Tells the attacking player's own client to play the slash sound as a direct, non-positional
 * sound. Everyone else gets the normal positional sound packet from the server.
 */
public record SlashSoundPayload(float volume) implements CustomPacketPayload {

    public static final Type<SlashSoundPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TrueCopper.MOD_ID, "slash_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlashSoundPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.FLOAT, SlashSoundPayload::volume, SlashSoundPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SlashSoundPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientSlashSound.play(payload.volume()));
    }
}
