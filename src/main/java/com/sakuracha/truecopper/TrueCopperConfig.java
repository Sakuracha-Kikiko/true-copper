package com.sakuracha.truecopper;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config. Values fall back to their defaults when read before the config is loaded
 * (mixins may fire very early), so nothing here ever throws.
 */
public final class TrueCopperConfig {

    public static final ModConfigSpec SPEC;

    /** Prefix of the in-game config screen translation keys. */
    private static final String LANG = "true_copper.config.";

    private static final ModConfigSpec.BooleanValue DEFER_INSTANT_DAMAGE;
    private static final ModConfigSpec.IntValue SUSTAINED_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue PENDING_TIMEOUT_TICKS;
    private static final ModConfigSpec.BooleanValue PLAY_ZENITH_SOUND;
    private static final ModConfigSpec.DoubleValue ZENITH_SOUND_VOLUME;
    private static final ModConfigSpec.IntValue ZENITH_SWING_SOUND_RANGE;
    private static final ModConfigSpec.BooleanValue PLAY_ZENITH_HIT_SOUND;
    private static final ModConfigSpec.IntValue ZENITH_HIT_SOUND_RANGE;
    private static final ModConfigSpec.DoubleValue ZENITH_HIT_SOUND_VOLUME;
    private static final ModConfigSpec.IntValue ZENITH_HIT_SOUND_MIN_DISTANCE;
    private static final ModConfigSpec.BooleanValue ENABLE_MAID_TASK;
    private static final ModConfigSpec.IntValue MAID_SEARCH_RANGE;
    private static final ModConfigSpec.IntValue MAID_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue MAID_DAMAGE_BONUS;
    private static final ModConfigSpec.BooleanValue ALLOW_MAID_DAMAGE_PLAYER;
    private static final ModConfigSpec.BooleanValue ALLOW_MAID_DAMAGE_MAID;
    private static final ModConfigSpec.BooleanValue ALLOW_PLAYER_DAMAGE_MAID;
    private static final ModConfigSpec.BooleanValue ALLOW_PLAYER_DAMAGE_PLAYER;
    private static final ModConfigSpec.BooleanValue ENABLE_SHIMMERING_TRANSMUTATION;

    private static final boolean DEFAULT_DEFER = true;
    private static final int DEFAULT_WINDOW = 10;
    private static final int DEFAULT_TIMEOUT = 25;
    private static final boolean DEFAULT_PLAY_SOUND = true;
    private static final double DEFAULT_SOUND_VOLUME = 1.0D;
    private static final int DEFAULT_SWING_SOUND_RANGE = 64;
    private static final boolean DEFAULT_PLAY_HIT_SOUND = true;
    private static final int DEFAULT_HIT_SOUND_RANGE = 64;
    private static final double DEFAULT_HIT_SOUND_VOLUME = 0.8D;
    private static final int DEFAULT_HIT_SOUND_MIN_DISTANCE = 16;
    private static final boolean DEFAULT_ENABLE_MAID_TASK = true;
    private static final int DEFAULT_MAID_SEARCH_RANGE = 64;
    private static final int DEFAULT_MAID_COOLDOWN = 8;
    private static final double DEFAULT_MAID_DAMAGE_BONUS = 1.0D;
    private static final boolean DEFAULT_ALLOW_MAID_DAMAGE_PLAYER = false;
    private static final boolean DEFAULT_ALLOW_MAID_DAMAGE_MAID = false;
    private static final boolean DEFAULT_ALLOW_PLAYER_DAMAGE_MAID = false;
    private static final boolean DEFAULT_ALLOW_PLAYER_DAMAGE_PLAYER = false;
    private static final boolean DEFAULT_ENABLE_SHIMMERING_TRANSMUTATION = true;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation(LANG + "deferred_damage")
                .comment("Zenith Sword: hold back the instantaneous part of a swing until the flying blade reaches the target.")
                .push("deferred_damage");

        DEFER_INSTANT_DAMAGE = builder
                .translation(LANG + "deferred_damage.deferInstantDamage")
                .comment("Delay the instantaneous swing damage until a Zenith blade actually hits the target.",
                        "Targets that are already being hit by Zenith blades are damaged immediately (sustained damage).")
                .define("deferInstantDamage", DEFAULT_DEFER);

        SUSTAINED_WINDOW_TICKS = builder
                .translation(LANG + "deferred_damage.sustainedWindowTicks")
                .comment("If the target was hit by any Zenith blade within this many ticks, the swing counts as sustained damage and is applied immediately.")
                .defineInRange("sustainedWindowTicks", DEFAULT_WINDOW, 0, 100);

        PENDING_TIMEOUT_TICKS = builder
                .translation(LANG + "deferred_damage.pendingTimeoutTicks")
                .comment("Upper bound (in ticks) for how long a deferred hit waits for the blade.",
                        "A blade needs 21 ticks to travel its full range, so this should stay above that.",
                        "When the timeout expires the damage is applied anyway - it is never thrown away.")
                .defineInRange("pendingTimeoutTicks", DEFAULT_TIMEOUT, 1, 200);

        builder.pop();

        builder.translation(LANG + "sounds")
                .comment("Zenith Sword: the sword ships without any sound, this adds a swing sound.")
                .push("sounds");

        PLAY_ZENITH_SOUND = builder
                .translation(LANG + "sounds.playZenithSlashSound")
                .comment("Play a swing sound whenever a Zenith Sword attack actually goes off.",
                        "Also applies to maids once they can use the sword.")
                .define("playZenithSlashSound", DEFAULT_PLAY_SOUND);

        ZENITH_SOUND_VOLUME = builder
                .translation(LANG + "sounds.zenithSlashVolume")
                .comment("Volume of the swing sound. Range is controlled separately by swingSoundRange;",
                        "this value only changes how loud the swing is.")
                .defineInRange("zenithSlashVolume", DEFAULT_SOUND_VOLUME, 0.0D, 4.0D);

        ZENITH_SWING_SOUND_RANGE = builder
                .translation(LANG + "sounds.swingSoundRange")
                .comment("How far (in blocks) other players can hear a Zenith Sword swing.",
                        "The attacker always hears the swing at full volume; everyone else hears it fade with distance.",
                        "The sword itself reaches 200 blocks, but sound travels so far sounds loud, so the default is 64.")
                .defineInRange("swingSoundRange", DEFAULT_SWING_SOUND_RANGE, 16, 512);

        PLAY_ZENITH_HIT_SOUND = builder
                .translation(LANG + "sounds.playZenithHitSound")
                .comment("Extend the hurt/death sound of targets hit by a Zenith Sword to nearby players.",
                        "Players inside the vanilla 16 block radius already hear the normal hurt sound and are skipped.",
                        "This is what gives a hit at 100+ blocks its impact.")
                .define("playZenithHitSound", DEFAULT_PLAY_HIT_SOUND);

        ZENITH_HIT_SOUND_RANGE = builder
                .translation(LANG + "sounds.hitSoundRange")
                .comment("How far (in blocks) the extended hurt/death sound of a Zenith hit travels.")
                .defineInRange("hitSoundRange", DEFAULT_HIT_SOUND_RANGE, 16, 512);

        ZENITH_HIT_SOUND_VOLUME = builder
                .translation(LANG + "sounds.hitSoundVolume")
                .comment("Volume multiplier for the extended hurt/death sound.",
                        "This is added on top of the target's own sound volume (usually 1.0).")
                .defineInRange("hitSoundVolume", DEFAULT_HIT_SOUND_VOLUME, 0.0D, 4.0D);

        ZENITH_HIT_SOUND_MIN_DISTANCE = builder
                .translation(LANG + "sounds.hitSoundMinDistance")
                .comment("Players closer than this (in blocks) to the target keep the vanilla hurt sound only.",
                        "Set to 0 to also send the extended sound to close players (it will sound doubled).")
                .defineInRange("hitSoundMinDistance", DEFAULT_HIT_SOUND_MIN_DISTANCE, 0, 128);

        builder.pop();

        builder.translation(LANG + "appearance")
                .comment("How the three sword forms can be transformed into each other.")
                .push("appearance");

        ENABLE_SHIMMERING_TRANSMUTATION = builder
                .translation(LANG + "appearance.enableShimmeringTransmutation")
                .comment("Allow one Zenith Sword, True Copper Shortsword or True Wooden Sword to be transmuted",
                        "in a crafting grid, cycling Zenith -> Copper -> Wooden -> Zenith.",
                        "The result keeps every data component of the input, including enchantments and custom names.")
                .define("enableShimmeringTransmutation", DEFAULT_ENABLE_SHIMMERING_TRANSMUTATION);

        builder.pop();

        builder.translation(LANG + "friendly_fire")
                .comment("Which friendly-fire directions a Zenith Sword may damage.",
                        "All four are off by default, so allies never hurt each other.")
                .push("friendly_fire");

        ALLOW_MAID_DAMAGE_PLAYER = builder
                .translation(LANG + "friendly_fire.allowMaidDamagePlayer")
                .comment("Let a maid's Zenith Sword damage players, including her owner.")
                .define("allowMaidDamagePlayer", DEFAULT_ALLOW_MAID_DAMAGE_PLAYER);

        ALLOW_MAID_DAMAGE_MAID = builder
                .translation(LANG + "friendly_fire.allowMaidDamageMaid")
                .comment("Let a maid's Zenith Sword damage other maids.")
                .define("allowMaidDamageMaid", DEFAULT_ALLOW_MAID_DAMAGE_MAID);

        ALLOW_PLAYER_DAMAGE_MAID = builder
                .translation(LANG + "friendly_fire.allowPlayerDamageMaid")
                .comment("Let a player's Zenith Sword damage maids.")
                .define("allowPlayerDamageMaid", DEFAULT_ALLOW_PLAYER_DAMAGE_MAID);

        ALLOW_PLAYER_DAMAGE_PLAYER = builder
                .translation(LANG + "friendly_fire.allowPlayerDamagePlayer")
                .comment("Let players damage each other with the Zenith Sword. Turn this on for PvP.")
                .define("allowPlayerDamagePlayer", DEFAULT_ALLOW_PLAYER_DAMAGE_PLAYER);

        builder.pop();

        builder.translation(LANG + "maid")
                .comment("Maid support: a work mode that lets maids swing the Zenith Sword like a player.")
                .push("maid");

        ENABLE_MAID_TASK = builder
                .translation(LANG + "maid.enableZenithMaidTask")
                .comment("Register the Zenith Sword work mode for maids. Only takes effect when Touhou Little Maid",
                        "is installed; the work mode is simply absent otherwise.")
                .gameRestart()
                .define("enableZenithMaidTask", DEFAULT_ENABLE_MAID_TASK);

        MAID_SEARCH_RANGE = builder
                .translation(LANG + "maid.zenithSearchRange")
                .comment("How far (in blocks) a maid searches for and keeps a target while using the Zenith Sword.")
                .defineInRange("zenithSearchRange", DEFAULT_MAID_SEARCH_RANGE, 8, 128);

        MAID_COOLDOWN_TICKS = builder
                .translation(LANG + "maid.zenithAttackCooldownTicks")
                .comment("Ticks between two swings. The sword itself has a short internal cooldown, this is the",
                        "maid's own pacing. 20 ticks is one second.")
                .defineInRange("zenithAttackCooldownTicks", DEFAULT_MAID_COOLDOWN, 1, 200);

        MAID_DAMAGE_BONUS = builder
                .translation(LANG + "maid.zenithDamageBonus")
                .comment("Extra attack damage while a maid swings, as a multiple of the sword's own damage value.",
                        "A player in Zenith mode gets one extra copy of it, so 1.0 matches a player; 0 disables it.")
                .defineInRange("zenithDamageBonus", DEFAULT_MAID_DAMAGE_BONUS, 0.0D, 10.0D);

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

    public static float zenithSwingSoundRange() {
        return SPEC.isLoaded() ? ZENITH_SWING_SOUND_RANGE.get() : DEFAULT_SWING_SOUND_RANGE;
    }

    public static boolean playZenithHitSound() {
        return !SPEC.isLoaded() || PLAY_ZENITH_HIT_SOUND.get();
    }

    public static boolean enableShimmeringTransmutation() {
        return !SPEC.isLoaded() || ENABLE_SHIMMERING_TRANSMUTATION.get();
    }

    public static float zenithHitSoundRange() {
        return SPEC.isLoaded() ? ZENITH_HIT_SOUND_RANGE.get() : DEFAULT_HIT_SOUND_RANGE;
    }

    public static double zenithHitSoundVolume() {
        return SPEC.isLoaded() ? ZENITH_HIT_SOUND_VOLUME.get() : DEFAULT_HIT_SOUND_VOLUME;
    }

    public static double zenithHitSoundMinDistance() {
        return SPEC.isLoaded() ? ZENITH_HIT_SOUND_MIN_DISTANCE.get() : DEFAULT_HIT_SOUND_MIN_DISTANCE;
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

    public static boolean allowMaidDamagePlayer() {
        return SPEC.isLoaded() && ALLOW_MAID_DAMAGE_PLAYER.get();
    }

    public static boolean allowMaidDamageMaid() {
        return SPEC.isLoaded() && ALLOW_MAID_DAMAGE_MAID.get();
    }

    public static boolean allowPlayerDamageMaid() {
        return SPEC.isLoaded() && ALLOW_PLAYER_DAMAGE_MAID.get();
    }

    public static boolean allowPlayerDamagePlayer() {
        return SPEC.isLoaded() && ALLOW_PLAYER_DAMAGE_PLAYER.get();
    }
}
