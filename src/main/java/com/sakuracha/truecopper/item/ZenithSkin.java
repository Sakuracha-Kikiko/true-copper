package com.sakuracha.truecopper.item;

import org.jetbrains.annotations.Nullable;

/**
 * Cosmetic skins for the Zenith Sword family. The projectile type is synced to the client;
 * the trail colour is only used by the client renderer.
 */
public enum ZenithSkin {
    COPPER(21, new int[]{183, 88, 25}),
    WOODEN(22, new int[]{134, 101, 38});

    private final int projectileType;
    private final int[] trailColor;

    ZenithSkin(int projectileType, int[] trailColor) {
        this.projectileType = projectileType;
        this.trailColor = trailColor;
    }

    public int projectileType() {
        return this.projectileType;
    }

    /**
     * RGB base colour for the flying blade trail. Do not mutate the returned array.
     */
    public int[] trailColor() {
        return this.trailColor;
    }

    @Nullable
    public static ZenithSkin byProjectileType(int type) {
        if (type == COPPER.projectileType) {
            return COPPER;
        }
        if (type == WOODEN.projectileType) {
            return WOODEN;
        }
        return null;
    }
}
