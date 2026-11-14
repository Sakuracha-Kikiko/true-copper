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

### Swing and hit sounds

The Zenith Sword has no sound of its own. True Copper adds a swing sound (`true_copper:zenith_slash`)
and makes both the swing and the target's hurt sound audible across the sword's real reach.

- **Swing**: every swing plays `true_copper:zenith_slash` at the attacker's position. The attacker's
  own client plays a direct, non-positional copy, because a world sound at zero distance still goes
  through 3D processing and can feel detached from the swing. Everyone else hears it from the
  attacker's position, fading out to `swingSoundRange` (default 200 blocks).
- **Hit**: when a Zenith hit lands (instant pass or flying blade), the target's own hurt or death
  sound is replayed to players within `hitSoundRange` (default 200 blocks), fading with distance.
  Players inside the vanilla 16 block radius already hear the normal sound and are skipped, so it
  never doubles up. The volume is the target's own sound volume times `hitSoundVolume`.
- Vanilla sound packets stop at the sound event's range (16 blocks for most sounds), so the extended
  sounds travel in a small custom packet and are faded on the client instead. Turning the feature
  off restores the completely vanilla behaviour.

The shipped `zenith_slash.ogg` is mono on purpose: Minecraft only spatialises mono sounds, so keep
it mono if you replace the file.

### Maid work mode

Adds a **Zenith Sword** work mode for Touhou Little Maid: a maid holding a Zenith Sword fights like a
player would.

- She faces the target before every swing, because the sword builds its ray from the attacker's
  rotation.
- She uses her own cooldown instead of the vanilla attack speed, which for a maid is meaningless with
  this sword.
- While swinging she receives the same extra damage a player gets in Zenith mode, so the numbers match.
- Deferred damage applies to her hits too, so the blades still arrive before the health bar moves.
- Her swings respect the friendly-fire rules below: by default they never hurt players or other maids,
  because the sword hits everything on a long line and a maid could otherwise cut her own owner in half
  while aiming at a zombie.

The instant damage, the blade damage and the damage filter of the Zenith Sword itself are untouched:
a maid simply calls the same attack entry point a player does.

### Friendly fire

Four independent options decide which directions the Zenith Sword may hurt:

- **Maid → Player**: allowMaidDamagePlayer
- **Maid → Maid**: allowMaidDamageMaid
- **Player → Maid**: allowPlayerDamageMaid
- **Player → Player**: allowPlayerDamagePlayer

All four are off by default, so allies never damage each other. The check runs before both damage
entry points, so it covers the instant pass, the flying blades and damage replayed by deferred
damage. Turning one option on opens only that direction; the other three stay protected. Zenith
Sword PvP needs Player → Player to be on.

### Cosmetic variants

Two swords reuse the Zenith Sword's combat code without copying it:

- **True Copper Shortsword** (`true_copper:true_copper_shortsword`)
- **True Wooden Sword** (`true_copper:true_wooden_sword`)

They deal the same damage, have the same reach, cooldown and enchantment behaviour, and take part
in deferred damage exactly like the Zenith Sword. Only the item texture, the flying blade model and
the trail colour differ. Both are in the Combat creative tab.

### Shimmering transmutation

The **Appearance → Enable Shimmering Transmutation** option (on by default) adds a single-item
shapeless crafting cycle:

- Zenith Sword → True Copper Shortsword
- True Copper Shortsword → True Wooden Sword
- True Wooden Sword → Zenith Sword

The result keeps every data component of the input, so enchantments, custom names, lore and damage
carry across all three forms. The option only controls recipe matching and can be changed at runtime;
because crafting runs on the server, the server's config is authoritative.

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
    # How far other players hear the swing (the attacker always hears it at full volume).
    swingSoundRange = 200
    # Replay a hit target's hurt/death sound to nearby players, beyond the vanilla 16 blocks.
    playZenithHitSound = true
    # How far that hurt/death sound travels.
    hitSoundRange = 200
    # Volume multiplier on top of the target's own sound volume.
    hitSoundVolume = 0.8
    # Players closer than this keep only the vanilla sound (0 = also send to close players).
    hitSoundMinDistance = 16

[appearance]
    # Single-item shapeless cycle: Zenith -> Copper -> Wooden -> Zenith, keeping data components.
    enableShimmeringTransmutation = true

[friendly_fire]
    # All four directions are off by default: allies never hurt each other.
    allowMaidDamagePlayer = false
    allowMaidDamageMaid = false
    allowPlayerDamageMaid = false
    allowPlayerDamagePlayer = false

[maid]
    # Register the "Zenith Sword" maid work mode.
    enableZenithMaidTask = true
    # How far a maid looks for targets while using the sword.
    zenithSearchRange = 32
    # Fixed number of ticks between two swings (not scaled by attack speed, which is meaningless here).
    zenithAttackCooldownTicks = 20
    # Extra damage while swinging, as a multiple of the sword's own damage value (1.0 = like a player).
    zenithDamageBonus = 1.0
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.250 or newer
- The Zenith Sword 2.0.2 or newer

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
