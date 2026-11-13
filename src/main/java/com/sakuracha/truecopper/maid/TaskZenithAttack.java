package com.sakuracha.truecopper.maid;

import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRangedWalkToTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.oceanscenery.zenith.mod_class.item.ZenithItem;
import com.oceanscenery.zenith.registry.ZenithAttachments;
import com.oceanscenery.zenith.registry.ZenithItems;
import com.oceanscenery.zenith.util.AttachmentUtil;
import com.oceanscenery.zenith.util.ConfigUtil;
import com.sakuracha.truecopper.TrueCopper;
import com.sakuracha.truecopper.TrueCopperConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Maid work mode: swing the Zenith Sword exactly like a player does.
 * <p>
 * The sword does not use a vanilla melee hit, it casts a line of damage in front of the attacker, so the maid
 * aims itself at the target right before calling {@link ZenithItem#attack}. Everything else (cooldown, blades,
 * damage, the deferred damage feature of this mod) is the same code path the player uses.
 */
public class TaskZenithAttack implements IRangedAttackTask {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(TrueCopper.MOD_ID, "zenith_attack");
    private static final ResourceLocation DAMAGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath(TrueCopper.MOD_ID, "maid_zenith_damage_bonus");
    private static final String PLAYER_TYPE = "minecraft:player";
    private static final String MAID_TYPE = "touhou_little_maid:maid";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ZenithItems.ZENITH.get());
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 0.5f);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> startAttacking = new MaidStartAttackingTask(this);
        BehaviorControl<EntityMaid> stopAttacking = StopAttackingIfTargetInvalid.create(target -> !hasZenith(maid) || farAway(target, maid));
        BehaviorControl<EntityMaid> walkToTarget = MaidRangedWalkToTarget.create(0.6f);
        BehaviorControl<EntityMaid> attack = new MaidZenithAttackTask(this);

        return Lists.newArrayList(
                Pair.of(5, startAttacking),
                Pair.of(5, stopAttacking),
                Pair.of(5, walkToTarget),
                Pair.of(5, attack)
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createRideBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> startAttacking = new MaidStartAttackingTask(this);
        BehaviorControl<EntityMaid> stopAttacking = StopAttackingIfTargetInvalid.create(target -> !hasZenith(maid) || farAway(target, maid));
        BehaviorControl<EntityMaid> attack = new MaidZenithAttackTask(this);

        return Lists.newArrayList(
                Pair.of(5, startAttacking),
                Pair.of(5, stopAttacking),
                Pair.of(5, attack)
        );
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        if (hasZenith(maid)) {
            float searchRange = this.searchRadius(maid);
            if (maid.hasRestriction()) {
                return new AABB(maid.getRestrictCenter()).inflate(searchRange);
            }
            return maid.getBoundingBox().inflate(searchRange);
        }
        return IRangedAttackTask.super.searchDimension(maid);
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return TrueCopperConfig.maidSearchRange();
    }

    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        TargetingConditions conditions = IRangedAttackTask.TARGET_CONDITIONS;
        conditions.range(TrueCopperConfig.maidSearchRange());
        return conditions.test(maid, target);
    }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
        ItemStack stack = shooter.getMainHandItem();
        if (!(stack.getItem() instanceof ZenithItem zenith)) {
            return;
        }

        keepAlliesSafe(shooter);

        // A player in Zenith mode gets one extra copy of the sword's own damage value; hand the maid the same
        // bonus for the duration of the swing so both end up with the same numbers.
        AttributeInstance attackDamage = shooter.getAttribute(Attributes.ATTACK_DAMAGE);
        boolean boosted = false;
        double bonus = ConfigUtil.defaultDamage() * TrueCopperConfig.maidDamageBonus();
        if (attackDamage != null && bonus > 0.0D) {
            attackDamage.addTransientModifier(new AttributeModifier(DAMAGE_BONUS_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            boosted = true;
        }

        try {
            aimAt(shooter, target);
            zenith.attack(stack, shooter, shooter.level(), InteractionHand.MAIN_HAND);
        } finally {
            if (boosted) {
                attackDamage.removeModifier(DAMAGE_BONUS_ID);
            }
        }
    }

    /**
     * The sword hits everything on its damage line, including the maid's owner and other maids. Players at least
     * have the sword's own blacklist UI; a maid cannot use it, so the work mode adds players and maids to the
     * maid's type blacklist instead. The entry is kept on the maid (not just for the current swing) because the
     * flying blades resolve their damage a second later and use the same blacklist.
     */
    private static void keepAlliesSafe(EntityMaid maid) {
        if (!TrueCopperConfig.maidProtectAllies()) {
            AttachmentUtil.setTypeBlacklist(maid, PLAYER_TYPE, false);
            AttachmentUtil.setTypeBlacklist(maid, MAID_TYPE, false);
            return;
        }
        Set<String> marks = maid.getData(ZenithAttachments.ZENITH_ID_MARK).ids();
        if (!marks.contains(PLAYER_TYPE)) {
            AttachmentUtil.setTypeBlacklist(maid, PLAYER_TYPE, true);
        }
        if (!marks.contains(MAID_TYPE)) {
            AttachmentUtil.setTypeBlacklist(maid, MAID_TYPE, true);
        }
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_zenith", this::hasZenith));
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        return stack.getItem() instanceof ZenithItem;
    }

    public boolean hasZenith(EntityMaid maid) {
        return maid.getMainHandItem().getItem() instanceof ZenithItem;
    }

    public Optional<? extends LivingEntity> findTarget(EntityMaid maid) {
        return IRangedAttackTask.findFirstValidAttackTarget(maid);
    }

    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > this.searchRadius(maid);
    }

    /**
     * Point the maid at the target. The sword builds its damage line from {`getXRot()/getYRot()`},
     * and a mob does not automatically face what it is looking at, so this has to be explicit.
     */
    public static void aimAt(EntityMaid maid, LivingEntity target) {
        double dx = target.getX() - maid.getX();
        double dy = target.getEyeY() - maid.getEyeY();
        double dz = target.getZ() - maid.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontal) * (180.0D / Math.PI)));
        maid.setYRot(yaw);
        maid.setYHeadRot(yaw);
        maid.setYBodyRot(yaw);
        maid.setXRot(pitch);
    }
}
