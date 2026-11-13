package com.sakuracha.truecopper.damage;

import com.oceanscenery.zenith.util.DamageHandler;
import com.sakuracha.truecopper.TrueCopperConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A Zenith swing has two damage passes:
 * <ul>
 *     <li>an instantaneous one, applied the moment the player clicks, through every entity inside a
 *     200 block ray;</li>
 *     <li>one per blade, applied when a flying projectile actually sweeps over an entity, roughly
 *     0 to 21 ticks later depending on the distance.</li>
 * </ul>
 * The instantaneous pass therefore often lands before the blade is anywhere near the target.
 * This class holds that pass back until a blade arrives, so damage and visuals line up.
 *
 * <p>Rules:
 * <ul>
 *     <li>If the target is already being hit by Zenith blades (or already has a deferred hit
 *     waiting), the swing is "sustained" and is applied immediately.</li>
 *     <li>Otherwise the hit is stored and replayed by the first blade that touches the target.</li>
 *     <li>A blade needs 21 ticks to travel its full range, so a stored hit times out after
 *     {@code pendingTimeoutTicks} (default 25) and is replayed then. Damage is never dropped.</li>
 * </ul>
 *
 * <p>Only used on the logical server, from the server thread.
 */
public final class DeferredDamage {

    private static final Logger LOGGER = LoggerFactory.getLogger("true_copper/deferred_damage");

    /** How long a blade hit is remembered for the sustained-damage check. */
    private static final long HIT_MEMORY_TICKS = 400L;

    private static final Map<Entity, Pending> PENDING = new HashMap<>();
    private static final Map<Entity, Long> LAST_BLADE_HIT = new HashMap<>();

    /** Set while we replay a deferred hit, so the mixin lets our own call through. */
    private static boolean flushing;

    private DeferredDamage() {
    }

    /**
     * @return {@code true} if the caller must cancel the instantaneous damage pass
     */
    public static boolean shouldDefer(Entity attacker, Entity victim, ItemStack weapon, float scale, float damage) {
        if (flushing || attacker == null || victim == null || weapon == null) {
            return false;
        }
        if (!(victim instanceof LivingEntity) || victim.level().isClientSide()) {
            return false;
        }
        if (!TrueCopperConfig.deferInstantDamage() || victim.isRemoved() || !victim.isAlive()) {
            return false;
        }

        long now = victim.level().getGameTime();
        int timeout = TrueCopperConfig.pendingTimeoutTicks();

        Pending pending = PENDING.get(victim);
        if (pending != null) {
            // A blade from an earlier swing has not arrived yet - fold this hit into it.
            pending.merge(weapon, scale, damage, now + timeout);
            return true;
        }

        Long lastHit = LAST_BLADE_HIT.get(victim);
        if (lastHit != null && now - lastHit <= TrueCopperConfig.sustainedWindowTicks()) {
            // The target is already taking blade damage, keep the pressure up.
            return false;
        }

        PENDING.put(victim, new Pending(attacker, weapon.copy(), scale, damage, now + timeout));
        return true;
    }

    /** Called when a Zenith blade sweeps over an entity. */
    public static void onBladeHit(Entity victim) {
        if (victim == null || victim.level().isClientSide()) {
            return;
        }
        long now = victim.level().getGameTime();
        LAST_BLADE_HIT.put(victim, now);

        Pending pending = PENDING.remove(victim);
        if (pending != null) {
            flush(victim, pending);
        }
    }

    /** Drops expired entries, replays timed out hits and forgets stale blade hits. */
    public static void serverTick(MinecraftServer server) {
        if (PENDING.isEmpty() && LAST_BLADE_HIT.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();

        if (!PENDING.isEmpty()) {
            List<Map.Entry<Entity, Pending>> expired = new ArrayList<>();
            Iterator<Map.Entry<Entity, Pending>> iterator = PENDING.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Entity, Pending> entry = iterator.next();
                Entity victim = entry.getKey();
                if (victim.isRemoved() || !victim.isAlive()) {
                    iterator.remove();
                } else if (now >= entry.getValue().deadline) {
                    iterator.remove();
                    expired.add(entry);
                }
            }
            for (Map.Entry<Entity, Pending> entry : expired) {
                flush(entry.getKey(), entry.getValue());
            }
        }

        if (!LAST_BLADE_HIT.isEmpty()) {
            LAST_BLADE_HIT.entrySet().removeIf(entry ->
                    entry.getKey().isRemoved() || now - entry.getValue() > HIT_MEMORY_TICKS);
        }
    }

    public static void clear() {
        PENDING.clear();
        LAST_BLADE_HIT.clear();
        flushing = false;
    }

    private static void flush(Entity victim, Pending pending) {
        if (victim.isRemoved() || !victim.isAlive()) {
            return;
        }
        flushing = true;
        try {
            DamageHandler.applyDirectDamage(pending.attacker, victim, pending.weapon, pending.scale, pending.damage);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to replay deferred Zenith damage", throwable);
        } finally {
            flushing = false;
        }
    }

    /** A frozen snapshot of one instantaneous damage pass. */
    private static final class Pending {

        private final Entity attacker;
        private ItemStack weapon;
        private float scale;
        private float damage;
        private long deadline;

        private Pending(Entity attacker, ItemStack weapon, float scale, float damage, long deadline) {
            this.attacker = attacker;
            this.weapon = weapon;
            this.scale = scale;
            this.damage = damage;
            this.deadline = deadline;
        }

        private void merge(ItemStack weapon, float scale, float damage, long deadline) {
            this.weapon = weapon.copy();
            this.scale = scale;
            this.damage += damage;
            this.deadline = deadline;
        }
    }
}
