package com.sakuracha.truecopper.damage;

import com.sakuracha.truecopper.TrueCopperConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * The four friendly-fire directions the config exposes. Only Zenith Sword damage is affected; the
 * check runs before both damage entry points, so the instant pass, the flying blades and damage
 * replayed by {@link DeferredDamage} all go through it.
 * <p>
 * Entity type ids are used instead of Touhou Little Maid classes because that mod is optional.
 */
public final class FriendlyFire {

    private static final String PLAYER_TYPE = "minecraft:player";
    private static final String MAID_TYPE = "touhou_little_maid:maid";

    private FriendlyFire() {
    }

    /** @return true when this attacker/victim pair must not take Zenith Sword damage. */
    public static boolean blocks(Entity attacker, Entity victim) {
        if (attacker == null || victim == null) {
            return false;
        }
        String attackerType = EntityType.getKey(attacker.getType()).toString();
        String victimType = EntityType.getKey(victim.getType()).toString();

        if (PLAYER_TYPE.equals(attackerType)) {
            if (PLAYER_TYPE.equals(victimType)) {
                return !TrueCopperConfig.allowPlayerDamagePlayer();
            }
            if (MAID_TYPE.equals(victimType)) {
                return !TrueCopperConfig.allowPlayerDamageMaid();
            }
        } else if (MAID_TYPE.equals(attackerType)) {
            if (PLAYER_TYPE.equals(victimType)) {
                return !TrueCopperConfig.allowMaidDamagePlayer();
            }
            if (MAID_TYPE.equals(victimType)) {
                return !TrueCopperConfig.allowMaidDamageMaid();
            }
        }
        return false;
    }
}
