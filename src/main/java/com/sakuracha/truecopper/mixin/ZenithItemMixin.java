package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.sakuracha.truecopper.TrueCopperConfig;
import com.sakuracha.truecopper.network.SlashSoundPayload;
import com.sakuracha.truecopper.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Zenith Sword is silent in its own mod. This injects a swing sound at the start of
 * {@code ZenithItem#attack}, but only when the swing really happens: the vanilla cooldown
 * check is repeated first so blocked clicks stay silent.
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
        if (volume <= 0.0F) {
            return;
        }
        SoundEvent sound = ModSounds.ZENITH_SLASH.get();
        double x = attacker.getX();
        double y = attacker.getEyeY();
        double z = attacker.getZ();
        if (attacker instanceof ServerPlayer player) {
            // Everyone else hears the swing coming from the player. The attacker's own client
            // gets a direct copy through the payload instead: a world sound at zero distance is
            // still processed positionally and does not feel attached to the swing.
            level.playSound(player, x, y, z, sound, SoundSource.PLAYERS, volume, 1.0F);
            PacketDistributor.sendToPlayer(player, new SlashSoundPayload(volume));
        } else {
            level.playSound(null, x, y, z, sound, SoundSource.PLAYERS, volume, 1.0F);
        }
    }
}
