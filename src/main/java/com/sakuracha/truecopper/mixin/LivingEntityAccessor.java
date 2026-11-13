package com.sakuracha.truecopper.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * The hurt and death sounds are protected on LivingEntity. We only read them, to replay the
 * target's own sound far away; the vanilla sound is left untouched.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker("getHurtSound")
    SoundEvent truecopper$getHurtSound(DamageSource source);

    @Invoker("getDeathSound")
    SoundEvent truecopper$getDeathSound();

    @Invoker("getSoundVolume")
    float truecopper$getSoundVolume();
}
