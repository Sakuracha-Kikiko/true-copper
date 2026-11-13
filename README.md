# True Copper

Quality-of-life features and compatibilities for [The Zenith Sword](https://github.com/OceanScenery/MinecraftZenithMod).

## Features

### Deferred swing damage

A Zenith Sword swing deals damage twice:

1. **Instantly**, the moment the attack is triggered - every entity inside the 200 block ray of the
   swing is damaged at once.
2. **Per blade**, when one of the flying sword projectiles actually sweeps over an entity - which is
   anywhere from the same tick up to 21 ticks later, depending on distance.

The first pass is what makes a target lose health before the blade has even left the player's hand.
True Copper holds that pass back and replays it when a blade arrives, so the numbers match the
animation.

Behaviour:

- **Isolated swing** (nothing has been cut by a Zenith blade recently): the instantaneous damage is
  stored and replayed by the first blade that touches the target.
- **Sustained damage** (the target was hit by any Zenith blade within `sustainedWindowTicks`, or a
  stored hit is already waiting): the damage is applied immediately, so continuous pressure is not
  delayed.
- **Blades never arrive** (target moved, blade was blocked by terrain, ...): after
  `pendingTimeoutTicks` the stored damage is applied anyway. Damage is never lost, it is only
  reordered.

Only living entities are affected; item entities, experience orbs and similar non-living targets keep
the vanilla instant behaviour.

### Swing sound

Every Zenith Sword swing plays `true_copper:zenith_slash`. It is client-side only and can be turned
off or rebalanced in the config.

### Maid work mode

Adds a **Zenith Sword** work mode for Touhou Little Maid: a maid holding a Zenith Sword fights like a
player would.

- She faces the target before every swing, because the sword builds its ray from the attacker's
  rotation.
- She uses her own cooldown instead of the vanilla attack speed, which for a maid is meaningless with
  this sword.
- While swinging she receives the same extra damage a player gets in Zenith mode, so the numbers match.
- Deferred damage applies to her hits too, so the blades still arrive before the health bar moves.
- Her swings never hurt players or other maids: the sword hits everything on a long line, so without this a
  maid could cut her own owner in half while aiming at a zombie.

The instant damage, the blade damage and the damage filter of the Zenith Sword itself are untouched:
a maid simply calls the same attack entry point a player does.

## Configuration

Everything below can be edited in game: **Mods → True Copper → Config**. That screen is NeoForge's own
config UI, no extra config library is required. The TOML file is still there for pack makers.

`config/true_copper-common.toml`:

```toml
[deferred_damage]
    # Delay the instantaneous swing damage until a Zenith blade actually hits the target.
    deferInstantDamage = true
    # A target hit by a Zenith blade within this many ticks counts as sustained damage.
    sustainedWindowTicks = 10
    # How long a stored hit waits for its blade before it is applied anyway.
    pendingTimeoutTicks = 25

[sounds]
    # Play a slash sound whenever a Zenith Sword swing starts.
    playZenithSlashSound = true
    zenithSlashVolume = 1.0

[maid]
    # Register the "Zenith Sword" maid work mode.
    enableZenithMaidTask = true
    # How far a maid looks for targets while using the sword.
    zenithSearchRange = 32
    # Fixed number of ticks between two swings (not scaled by attack speed, which is meaningless here).
    zenithAttackCooldownTicks = 20
    # Extra damage while swinging, as a multiple of the sword's own damage value (1.0 = like a player).
    zenithDamageBonus = 1.0
    # Keep players and other maids out of the maid's damage line (and out of her flying blades).
    protectAllies = true
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.250 or newer
- The Zenith Sword 2.0.0 or newer

Touhou Little Maid 1.5.3 or newer is optional: without it the maid work mode is simply not registered,
everything else keeps working.

## Building

```
./gradlew build
```

Third-party jars are not committed. Drop the Zenith Sword jar (and, for the maid work mode, the
Touhou Little Maid jar) into `libs/` before building: every jar there is picked up as a
`compileOnly` dependency and for the `runClient`/`runServer` development runs.

## License

MIT
