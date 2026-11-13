package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.sakuracha.truecopper.TrueCopperConfig;
import com.sakuracha.truecopper.network.SlashSoundPayload;
import com.sakuracha.truecopper.sound.ModSounds;
import com.sakuracha.truecopper.sound.SoundBroadcast;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Zenith Sword is silent in its own mod. This injects a swing sound at the start of
 * {@code ZenithItem#attack}, but only when the swing really happens: the vanilla cooldown
 * check is repeated first so blocked clicks stay silent. The sound is broadcast by hand so it
 * can be heard across the sword's own 200 block reach instead of the vanilla 16 blocks.
 */
@Mixin(value = ZenithItem.class, remap = false)
public abstract class ZenithItemMixin {

    @Inject(
            method = "attack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/InteractionHand;)V",
            at = @At("HEAD"))
    private void truecopper$playSlashSound(ItemStack usedItem, LivingEntity attacker, Level level,
                                           InteractionHand hand, CallbackInfo ci) {
        if (level.isClientSide() || !TrueCopperConfig.playZenithSound()) {
            return;
        }
        // attack() itself checks this; repeating it here keeps cooldown-blocked clicks silent.
        if (!((ZenithItem) (Object) this).canAttack(usedItem, attacker)) {
            return;
        }
        float volume = (float) TrueCopperConfig.zenithSoundVolume();
        if (volume <= 0.0F || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        SoundEvent sound = ModSounds.ZENITH_SLASH.get();
        ServerPlayer attackerPlayer = attacker instanceof ServerPlayer player ? player : null;
        SoundBroadcast.send(serverLevel,
                new Vec3(attacker.getX(), attacker.getEyeY(), attacker.getZ()),
                sound, volume, 1.0F, TrueCopperConfig.zenithSwingSoundRange(),
                SoundSource.PLAYERS, attackerPlayer, 0.0D);
        if (attackerPlayer != null) {
            // The attacker hears their own swing directly instead of positionally, so it feels
            // attached to the hand. Everyone else hears the broadcast version.
            PacketDistributor.sendToPlayer(attackerPlayer, new SlashSoundPayload(volume));
        }
    }
}
