package com.sakuracha.truecopper.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.sakuracha.truecopper.TrueCopperConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The movement half of Touhou Little Maid's vanilla ranged-attack AI, adapted to the Zenith Sword.
 * <p>
 * The original {@code MaidAttackStrafingTask} only starts with a {@code ProjectileWeaponItem} in hand and uses
 * that item's projectile range. The Zenith Sword is a normal item whose damage is a long line, so this task repeats
 * the same movement (keep a distance, strafe sideways, back off when too close) but accepts {@link ZenithItem}.
 * <p>
 * It is always part of the work mode's brain, and {@link TrueCopperConfig#useVanillaRangedAttack()} gates it at
 * runtime. That way the option can be toggled without reassigning the maid's work mode, while the default (off)
 * leaves the previous behaviour completely untouched. The attack itself still comes from {@link MaidZenithAttackTask}.
 */
public class MaidZenithStrafingTask extends Behavior<EntityMaid> {
    /**
     * The sword's default projectile travel distance. The damage line is longer, but kiting around this distance
     * looks like a ranged attacker instead of making the maid retreat from her own target forever.
     */
    private static final float STRAFE_RANGE = 20.0F;

    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public MaidZenithStrafingTask() {
        super(ImmutableMap.of(
                        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                        MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT),
                1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return TrueCopperConfig.useVanillaRangedAttack()
                && hasZenith(maid)
                && maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                        .filter(Entity::isAlive)
                        .isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return this.checkExtraStartConditions(level, maid);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!(maid.getMainHandItem().getItem() instanceof ZenithItem)) {
            return;
        }

        maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
            float maxAttackDistance = Math.min(maid.searchRadius(), STRAFE_RANGE);
            double distance = maid.distanceTo(target);

            if (distance < maid.searchRadius()) {
                ++this.strafingTime;
            } else {
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (maid.getRandom().nextFloat() < 0.3F) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (maid.getRandom().nextFloat() < 0.3F) {
                    this.strafingBackwards = !this.strafingBackwards;
                }
                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distance > maxAttackDistance * 0.5F) {
                    this.strafingBackwards = false;
                } else if (distance < maxAttackDistance * 0.2F) {
                    this.strafingBackwards = true;
                }

                // Do not strafe away from the owner or out of the maid's home area.
                if (!maid.hasRestriction()
                        && maid.getOwner() instanceof Player player
                        && maid.distanceTo(player) >= maid.getRestrictRadius()) {
                    maid.stopInPlace();
                } else {
                    maid.getMoveControl().strafe(
                            this.strafingBackwards ? -0.5F : 0.5F,
                            this.strafingClockwise ? 0.5F : -0.5F);
                }
                maid.setYRot(Mth.rotateIfNecessary(maid.getYRot(), maid.yHeadRot, 0.0F));
                BehaviorUtils.lookAtEntity(maid, target);
            } else {
                BehaviorUtils.lookAtEntity(maid, target);
            }
        });
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.setSwingingArms(true);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        maid.setSwingingArms(false);
        maid.getMoveControl().strafe(0.0F, 0.0F);
    }

    private static boolean hasZenith(EntityMaid maid) {
        ItemStack stack = maid.getMainHandItem();
        return stack.getItem() instanceof ZenithItem;
    }
}
