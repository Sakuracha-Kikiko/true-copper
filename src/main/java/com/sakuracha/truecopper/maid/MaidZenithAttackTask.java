package com.sakuracha.truecopper.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.sakuracha.truecopper.TrueCopperConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Swings the Zenith Sword at the current attack target on the maid's own cooldown.
 * The sword is a line attack, so this behavior does not need any melee range check.
 */
public class MaidZenithAttackTask extends Behavior<EntityMaid> {
    private final TaskZenithAttack task;
    private long nextAttackTime;

    public MaidZenithAttackTask(TaskZenithAttack task) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 1200);
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && this.task.hasZenith(maid) && this.task.canSee(maid, target);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && this.task.hasZenith(maid) && this.task.canSee(maid, target);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        maid.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (gameTime < this.nextAttackTime) {
            return;
        }
        if (!this.task.hasZenith(maid) || !this.task.canSee(maid, target)) {
            return;
        }
        this.task.performRangedAttack(maid, target, 0.0F);
        this.nextAttackTime = gameTime + TrueCopperConfig.maidCooldownTicks();
    }
}
