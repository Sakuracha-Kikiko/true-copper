# True Copper

[中文](README-zh_CN.md) | **English**

Quality-of-life features and compatibility support for [TheZenithSword](https://github.com/OceanScenery/MinecraftZenithMod) and [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid).

> Not to be confused with [Zenith](https://github.com/gjdbg148525/Zenith), [The Zenith Sword](https://github.com/LeoMinecraftModding/the-zenith-sword) or [The Zenith](https://github.com/CHUJIAN-afk/Zenith).

## Features

> Every mention of "the Zenith Sword" below refers specifically to the Zenith Sword from that mod (TheZenithSword).

### Deferred swing damage

A Zenith Sword swing deals damage twice:

1. **Instantly**, the moment the attack is triggered - every entity inside the 200 block ray of the swing is damaged at once.
2. **Per blade**, when one of the flying sword projectiles actually sweeps over an entity - which is anywhere from the same tick up to 21 ticks later, depending on distance.

The first pass is what makes a target lose health before the blade has even left the player's hand, which can feel a little counter-intuitive. True Copper holds that pass back and replays it when a blade arrives, so the numbers match the animation.

Behaviour:

- **Isolated swing** (no Zenith Sword projectile has hit the target recently): the instantaneous damage is stored and replayed by the first blade that touches the target.
- **Sustained damage** (the target was hit by any Zenith blade within `sustainedWindowTicks`, or a stored hit is already waiting): the damage is applied immediately, so continuous pressure is not delayed.
- **Blades never arrive** (target moved, blade was blocked by terrain, ... ~~though would it really miss?~~): after `pendingTimeoutTicks` the stored damage is applied anyway. Damage is never lost.

The option is on by default. Only living entities are affected; item entities, experience orbs and similar non-living targets keep the vanilla instant behaviour.

### Swing and hit sounds

The Zenith Sword has no sound of its own. True Copper adds a swing sound (`true_copper:zenith_slash`) and makes both the swing and the target's hurt sound audible from further away, for a stronger sense of impact.

- **Swing**: every swing plays `true_copper:zenith_slash` at the attacker's position. The attacker's own client plays a direct, non-positional copy, because a world sound at zero distance still goes through 3D processing and can feel detached from the swing. Everyone else hears it from the attacker's position, fading out to `swingSoundRange` (64 blocks by default).
- **Hit**: when a Zenith hit lands (instant pass or flying blade), the target's own hurt or death sound is replayed to players within `hitSoundRange` (64 blocks by default), fading with distance. Players inside the vanilla 16 block radius already hear the normal sound and are skipped, so it never doubles up. The volume is the target's own sound volume times `hitSoundVolume`.
- Vanilla sound packets stop at the sound event's range (16 blocks for most sounds), so the extended sounds travel in a small custom packet and are faded on the client instead. Turning the feature off restores the completely vanilla behaviour.

The option is on by default. The shipped `zenith_slash.ogg` is mono, and has to be. It is a little pitfall the author ran into: Minecraft (OpenAL) only spatialises mono sounds, while stereo comes out as something like direct client-side playback. So keep it mono if you replace the file.

### Maid work mode

Adds a **Zenith Sword** work mode for Touhou Little Maid: a maid holding a Zenith Sword fights like a player would.

- She faces the target before every swing, because the sword builds its ray from the attacker's rotation.
- She uses her own cooldown (by default half the speed at which a player swings a Zenith Sword, i.e. 8 ticks) instead of the vanilla attack speed - the attack speed attribute is meaningless for the Zenith Sword's attack pattern, and this is some kind of balance adjustment (trust me).
- While swinging she receives the same extra damage a player gets in Zenith mode, so the numbers match.
- Deferred damage applies to her hits too (when enabled), so the blades still arrive before the health bar moves.
- Her swings respect the friendly-fire rules below: by default they never hurt players or other maids, because the sword hits everything on a long line and a maid could otherwise cut her own owner in half while aiming at a zombie.

The option is on by default, and changing it requires a game restart. The instant damage, the blade damage and the damage filter of the Zenith Sword itself are untouched: a maid simply calls the same attack entry point a player does.

### Friendly fire

Four independent options decide which directions the Zenith Sword may hurt:

- **Maid → Player**: allowMaidDamagePlayer
- **Maid → Maid**: allowMaidDamageMaid
- **Player → Maid**: allowPlayerDamageMaid
- **Player → Player**: allowPlayerDamagePlayer

All four are off by default, so allies never damage each other. The check runs before both damage entry points, so it covers the instant pass, the flying blades and damage replayed by deferred damage. Turning one option on opens only that direction; the other three stay protected.

If your pack's numbers let you do it, you can also turn on Player → Player and have Zenith Sword PvP. ~~But honestly, this sword's numbers are still far too ahead of their time for PvP.~~

### Cosmetic variants

The mod adds two new cosmetic variants of the Zenith Sword:

- **True Copper Shortsword** (`true_copper:true_copper_shortsword`), the authentic Terraria shimmer transmutation experience
- **True Wooden Sword** (`true_copper:true_wooden_sword`), a version with more of a Minecraft flavour

Just as the True Copper Shortsword is to the Zenith Sword in Terraria, they deal the same damage, have the same reach, cooldown and enchantment behaviour, and take part in deferred damage (when enabled) exactly like the Zenith Sword. Only cosmetic aspects differ: the item texture, the flying blade model, the trail colour and so on.

#### Shimmering transmutation

The **Appearance → Enable Shimmering Transmutation** option (on by default) adds a single-item shapeless crafting cycle:

- Zenith Sword → True Copper Shortsword
- True Copper Shortsword → True Wooden Sword
- True Wooden Sword → Zenith Sword

The result keeps every data component of the input, so enchantments, custom names, lore and damage carry across all three forms. The option only controls recipe matching and can be changed at runtime.

## Configuration

Everything below can be edited in game: **Mods → True Copper → Config**. The in-game config screen is NeoForge's own config UI, no extra config library is required. The TOML file is still there for pack makers.

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
    swingSoundRange = 64
    # Replay a hit target's hurt/death sound to nearby players, beyond the vanilla 16 blocks.
    playZenithHitSound = true
    # How far that hurt/death sound travels.
    hitSoundRange = 64
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
    zenithSearchRange = 64
    # Fixed number of ticks between two swings (not scaled by attack speed, which is meaningless here).
    zenithAttackCooldownTicks = 8
    # Extra damage while swinging, as a multiple of the sword's own damage value (1.0 = like a player).
    zenithDamageBonus = 1.0
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.250 or newer
- TheZenithSword 2.0.2 or newer

Touhou Little Maid 1.5.3 or newer is optional: without it the maid work mode is simply not registered, everything else keeps working.

> Updates to TheZenithSword may break certain signatures and cause this mod's Mixin injections to fail. This is not TheZenithSword's fault; please report it to this mod's author.

## Some Questions

Q: This looks like it was written by AI?
A: You guessed right - it was. The author's own Java knowledge is rather limited; Mixin in particular is something they simply cannot write. If you have a problem with vibe coding, feel free to ignore my mod.
Q: Can I use it, or add it to a modpack?
A: This mod is licensed under MIT. Whatever MIT allows you to do, you can do. That said, the author would be glad if you dropped them a line when making a modpack or the like.
Q: Will there be other versions?
A: The author's time is limited, so they may only be able to maintain 1.21.1. They might follow the NeoForge LTS version in the future (currently 26.1.2). Of course, if you want, you can port it yourself - that is what MIT allows you to do.
Q: Why TheZenithSword and not one of the other Zenith Sword mods?
A: Largely because of its author, actually. As far as the author can tell, OceanScenery is a pretty friendly person, and they have had some exchanges; TheZenithSword is also a very faithful Zenith Sword mod, and it is open-sourced under MIT as well. The author really likes this kind of atmosphere in the Minecraft open-source community.

> I have to admit that TheZenith is a better mod: its art and implementation are better than all three other Zenith Swords. But unfortunately (or perhaps fortunately), it is an All Rights Reserved mod: its source is visible, but the author explicitly forbids others from making derivative works. That mod of his does not belong to the open-source community, and the law actually forbids me from doing so as well. Honestly, I don't have a problem with the DMCA - your copyright, your call - but since you did not choose the community, the community will not choose you either.

## Building

```
./gradlew build
```

Before building, drop the Zenith Sword jar (and, for the maid work mode, the Touhou Little Maid jar) into `libs/`: every jar there is picked up as a `compileOnly` dependency and for the `runClient`/`runServer` development runs.

## License

MIT

## Credits

Thanks to [OceanScenery](https://github.com/OceanScenery) and his [TheZenithSword](https://github.com/OceanScenery/MinecraftZenithMod) mod

Thanks to the [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) mod, its author [TartaricAcid](https://github.com/TartaricAcid) and the mod's contributors

Thanks to [Hangzhou DeepSeek Artificial Intelligence Basic Technology Research Co., Ltd.](https://www.deepseek.com/) for providing code support for this mod

**Thanks to every creator in the Minecraft open-source community**
