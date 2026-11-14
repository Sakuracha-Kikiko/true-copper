package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.ZenithDamageSource;
import com.oceanscenery.zenith.mod_class.entity.ZenithProjectile;
import com.oceanscenery.zenith.util.DamageHandler;
import com.sakuracha.truecopper.TrueCopperConfig;
import com.sakuracha.truecopper.damage.DeferredDamage;
import com.sakuracha.truecopper.sound.SoundBroadcast;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Only two places in The Zenith Sword apply damage: the instantaneous pass of a swing
 * ({@code applyDirectDamage}) and the flying blade pass ({@code applyDamage}). We hold the
 * first one back and let the second one release it. Both end in {@code applyDamageAmount},
 * where the hit sound is extended to the sword's actual range.
 */
@Mixin(value = DamageHandler.class, remap = false)
public class DamageHandlerMixin {

    private static final String APPLY_DIRECT_DAMAGE =
            "applyDirectDamage(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/world/item/ItemStack;FFLcom/oceanscenery/zenith/util/DamageHandler$AttackFilter;)Z";

    private static final String APPLY_DAMAGE =
            "applyDamage(Lcom/oceanscenery/zenith/mod_class/entity/ZenithProjectile;"
                    + "Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;F"
                    + "Lcom/oceanscenery/zenith/util/DamageHandler$AttackFilter;)Z";

    private static final String APPLY_DAMAGE_AMOUNT =
            "applyDamageAmount(Lnet/minecraft/world/entity/Entity;"
                    + "Lcom/oceanscenery/zenith/mod_class/ZenithDamageSource;F"
                    + "Lnet/minecraft/world/item/ItemStack;F)Z";

    @Inject(method = APPLY_DIRECT_DAMAGE, at = @At("HEAD"), cancellable = true, remap = false)
    private static void truecopper$deferInstantDamage(Entity attacker, Entity victim, ItemStack weapon,
                                                      float scale, float damage,
                                                      DamageHandler.AttackFilter filter,
                                                      CallbackInfoReturnable<Boolean> callback) {
        if (DeferredDamage.shouldDefer(attacker, victim, weapon, scale, damage)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = APPLY_DAMAGE, at = @At("HEAD"), remap = false)
    private static void truecopper$onBladeHit(ZenithProjectile projectile, Entity victim, ItemStack weapon,
                                              float scale, DamageHandler.AttackFilter filter,
                                              CallbackInfoReturnable<Boolean> callback) {
        DeferredDamage.onBladeHit(victim);
    }

    /**
     * The result of the one {@code victim.hurt} call inside {@code applyDamageAmount}. That method
     * returns true even when the hurt call was rejected (most importantly after the victim already
     * died), so without this flag every extra blade that lands on a corpse would replay the death
     * sound.
     */
    private static final ThreadLocal<Boolean> TRUECOPPER$HURT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = APPLY_DAMAGE_AMOUNT, at = @At("HEAD"), remap = false)
    private static void truecopper$resetHurtFlag(Entity victim, ZenithDamageSource source, float damage,
                                                 ItemStack weapon, float scale, CallbackInfoReturnable<Boolean> callback) {
        TRUECOPPER$HURT.set(Boolean.FALSE);
    }

    @Redirect(method = APPLY_DAMAGE_AMOUNT,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            remap = false)
    private static boolean truecopper$recordHurt(Entity victim, DamageSource source, float amount) {
        boolean hurt = victim.hurt(source, amount);
        TRUECOPPER$HURT.set(hurt);
        return hurt;
    }

    /**
     * Replays the victim's own hurt (or death) sound to players far outside the vanilla 16 block
     * radius. Players that close already get the normal sound, so they are skipped.
     */
    @Inject(method = APPLY_DAMAGE_AMOUNT, at = @At("RETURN"), remap = false)
    private static void truecopper$extendHitSound(Entity victim, ZenithDamageSource source, float damage,
                                                  ItemStack weapon, float scale,
                                                  CallbackInfoReturnable<Boolean> callback) {
        boolean hurt = Boolean.TRUE.equals(TRUECOPPER$HURT.get());
        TRUECOPPER$HURT.remove();
        if (!callback.getReturnValue() || !hurt || !TrueCopperConfig.playZenithHitSound()) {
            return;
        }
        if (!(victim instanceof LivingEntity living) || !(living.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntityAccessor accessor = (LivingEntityAccessor) living;
        SoundEvent sound = living.isDeadOrDying()
                ? accessor.truecopper$getDeathSound()
                : accessor.truecopper$getHurtSound(source);
        if (sound == null) {
            return;
        }
        float volume = accessor.truecopper$getSoundVolume()
                * (float) TrueCopperConfig.zenithHitSoundVolume();
        SoundBroadcast.send(level, living.position(), sound, volume, living.getVoicePitch(),
                TrueCopperConfig.zenithHitSoundRange(), living.getSoundSource(), null,
                TrueCopperConfig.zenithHitSoundMinDistance());
    }
}
