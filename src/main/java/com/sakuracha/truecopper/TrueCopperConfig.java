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

    private static final boolean DEFAULT_DEFER = true;
    private static final int DEFAULT_WINDOW = 10;
    private static final int DEFAULT_TIMEOUT = 25;
    private static final boolean DEFAULT_PLAY_SOUND = true;
    private static final double DEFAULT_SOUND_VOLUME = 1.0D;

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
}
