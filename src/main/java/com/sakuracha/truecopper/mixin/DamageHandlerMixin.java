package com.sakuracha.truecopper.mixin;

import com.oceanscenery.zenith.mod_class.entity.ZenithProjectile;
import com.oceanscenery.zenith.util.DamageHandler;
import com.sakuracha.truecopper.damage.DeferredDamage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Only two places in The Zenith Sword apply damage: the instantaneous pass of a swing
 * ({@code applyDirectDamage}) and the flying blade pass ({@code applyDamage}). We hold the
 * first one back and let the second one release it.
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
}
