package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.sakuracha.truecopper.TrueCopperConfig;
import com.sakuracha.truecopper.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        level.playSound(null, attacker.getX(), attacker.getEyeY(), attacker.getZ(),
                ModSounds.ZENITH_SLASH.get(), SoundSource.PLAYERS, volume, 1.0F);
    }
}
