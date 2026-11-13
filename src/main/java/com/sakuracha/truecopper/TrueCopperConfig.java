package com.sakuracha.truecopper;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config. Values fall back to their defaults when read before the config is loaded
 * (mixins may fire very early), so nothing here ever throws.
 */
public final class TrueCopperConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue DEFER_INSTANT_DAMAGE;
    private static final ModConfigSpec.IntValue SUSTAINED_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue PENDING_TIMEOUT_TICKS;
    private static final ModConfigSpec.BooleanValue PLAY_ZENITH_SOUND;
    private static final ModConfigSpec.DoubleValue ZENITH_SOUND_VOLUME;
    private static final ModConfigSpec.BooleanValue ENABLE_MAID_TASK;
    private static final ModConfigSpec.IntValue MAID_SEARCH_RANGE;
    private static final ModConfigSpec.IntValue MAID_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue MAID_DAMAGE_BONUS;
    private static final ModConfigSpec.BooleanValue MAID_PROTECT_ALLIES;

    private static final boolean DEFAULT_DEFER = true;
    private static final int DEFAULT_WINDOW = 10;
    private static final int DEFAULT_TIMEOUT = 25;
    private static final boolean DEFAULT_PLAY_SOUND = true;
    private static final double DEFAULT_SOUND_VOLUME = 1.0D;
    private static final boolean DEFAULT_ENABLE_MAID_TASK = true;
    private static final int DEFAULT_MAID_SEARCH_RANGE = 32;
    private static final int DEFAULT_MAID_COOLDOWN = 20;
    private static final double DEFAULT_MAID_DAMAGE_BONUS = 1.0D;
    private static final boolean DEFAULT_MAID_PROTECT_ALLIES = true;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Zenith Sword: hold back the instantaneous part of a swing until the flying blade reaches the target.")
                .push("deferred_damage");

        DEFER_INSTANT_DAMAGE = builder
                .comment("Delay the instantaneous swing damage until a Zenith blade actually hits the target.",
                        "Targets that are already being hit by Zenith blades are damaged immediately (sustained damage).")
                .define("deferInstantDamage", DEFAULT_DEFER);

        SUSTAINED_WINDOW_TICKS = builder
                .comment("If the target was hit by any Zenith blade within this many ticks, the swing counts as sustained damage and is applied immediately.")
                .defineInRange("sustainedWindowTicks", DEFAULT_WINDOW, 0, 100);

        PENDING_TIMEOUT_TICKS = builder
                .comment("Upper bound (in ticks) for how long a deferred hit waits for the blade.",
                        "A blade needs 21 ticks to travel its full range, so this should stay above that.",
                        "When the timeout expires the damage is applied anyway - it is never thrown away.")
                .defineInRange("pendingTimeoutTicks", DEFAULT_TIMEOUT, 1, 200);

        builder.pop();

        builder.comment("Zenith Sword: the sword ships without any sound, this adds a swing sound.")
                .push("sounds");

        PLAY_ZENITH_SOUND = builder
                .comment("Play a swing sound whenever a Zenith Sword attack actually goes off.",
                        "Also applies to maids once they can use the sword.")
                .define("playZenithSlashSound", DEFAULT_PLAY_SOUND);

        ZENITH_SOUND_VOLUME = builder
                .comment("Volume of the swing sound. Vanilla sounds get louder up to 1.0 and are audible",
                        "from roughly 16 blocks per unit of volume; higher values carry further.")
                .defineInRange("zenithSlashVolume", DEFAULT_SOUND_VOLUME, 0.0D, 4.0D);

        builder.pop();

        builder.comment("Maid support: a work mode that lets maids swing the Zenith Sword like a player.")
                .push("maid");

        ENABLE_MAID_TASK = builder
                .comment("Register the Zenith Sword work mode for maids. Only takes effect when Touhou Little Maid",
                        "is installed; the work mode is simply absent otherwise.")
                .define("enableZenithMaidTask", DEFAULT_ENABLE_MAID_TASK);

        MAID_SEARCH_RANGE = builder
                .comment("How far (in blocks) a maid searches for and keeps a target while using the Zenith Sword.")
                .defineInRange("zenithSearchRange", DEFAULT_MAID_SEARCH_RANGE, 8, 128);

        MAID_COOLDOWN_TICKS = builder
                .comment("Ticks between two swings. The sword itself has a short internal cooldown, this is the",
                        "maid's own pacing. 20 ticks is one second.")
                .defineInRange("zenithAttackCooldownTicks", DEFAULT_MAID_COOLDOWN, 1, 200);

        MAID_DAMAGE_BONUS = builder
                .comment("Extra attack damage while a maid swings, as a multiple of the sword's own damage value.",
                        "A player in Zenith mode gets one extra copy of it, so 1.0 matches a player; 0 disables it.")
                .defineInRange("zenithDamageBonus", DEFAULT_MAID_DAMAGE_BONUS, 0.0D, 10.0D);

        MAID_PROTECT_ALLIES = builder
                .comment("Never let a maid's Zenith Sword damage players or other maids.",
                        "The sword swings hit everything on a long line, so without this a maid can hit its owner.",
                        "Implemented with the sword's own type blacklist on the maid, and also covers the flying blades.")
                .define("protectAllies", DEFAULT_MAID_PROTECT_ALLIES);

        builder.pop();
        SPEC = builder.build();
    }

    private TrueCopperConfig() {
    }

    public static boolean deferInstantDamage() {
        return !SPEC.isLoaded() || DEFER_INSTANT_DAMAGE.get();
    }

    public static int sustainedWindowTicks() {
        return SPEC.isLoaded() ? SUSTAINED_WINDOW_TICKS.get() : DEFAULT_WINDOW;
    }

    public static int pendingTimeoutTicks() {
        return SPEC.isLoaded() ? PENDING_TIMEOUT_TICKS.get() : DEFAULT_TIMEOUT;
    }

    public static boolean playZenithSound() {
        return !SPEC.isLoaded() || PLAY_ZENITH_SOUND.get();
    }

    public static double zenithSoundVolume() {
        return SPEC.isLoaded() ? ZENITH_SOUND_VOLUME.get() : DEFAULT_SOUND_VOLUME;
    }

    public static boolean enableMaidTask() {
        return !SPEC.isLoaded() || ENABLE_MAID_TASK.get();
    }

    public static float maidSearchRange() {
        return SPEC.isLoaded() ? MAID_SEARCH_RANGE.get() : DEFAULT_MAID_SEARCH_RANGE;
    }

    public static int maidCooldownTicks() {
        return SPEC.isLoaded() ? MAID_COOLDOWN_TICKS.get() : DEFAULT_MAID_COOLDOWN;
    }

    public static double maidDamageBonus() {
        return SPEC.isLoaded() ? MAID_DAMAGE_BONUS.get() : DEFAULT_MAID_DAMAGE_BONUS;
    }

    public static boolean maidProtectAllies() {
        return !SPEC.isLoaded() || MAID_PROTECT_ALLIES.get();
    }
}
