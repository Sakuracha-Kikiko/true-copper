package com.sakuracha.truecopper.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;

/**
 * Picks a target for the maid and writes it into {`ATTACK_TARGET`}.
 * <p>
 * This is a small replacement for vanilla {`StartAttacking`}: the vanilla behavior also has to keep a
 * vanilla target in range, while this work mode has its own range check and its own attack behavior.
 */
public class MaidStartAttackingTask extends Behavior<EntityMaid> {
    private final TaskZenithAttack task;

    public MaidStartAttackingTask(TaskZenithAttack task) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED), 1);
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (maid.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        return this.task.hasZenith(maid) && this.task.findTarget(maid).isPresent();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Optional<? extends LivingEntity> target = this.task.findTarget(maid);
        if (target.isPresent() && this.task.canSee(maid, target.get())) {
            maid.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target.get());
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return false;
    }
}
