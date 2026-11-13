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

## Configuration

`config/true_copper-common.toml`:

```toml
[deferred_damage]
    # Delay the instantaneous swing damage until a Zenith blade actually hits the target.
    deferInstantDamage = true
    # A target hit by a Zenith blade within this many ticks counts as sustained damage.
    sustainedWindowTicks = 10
    # How long a stored hit waits for its blade before it is applied anyway.
    pendingTimeoutTicks = 25
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.250 or newer
- The Zenith Sword 2.0.0 or newer

## Building

```
./gradlew build
```

The Zenith Sword jar is not committed. Copy `the_zenith_sword-2.0.0-1.21.1.jar` into `libs/`
before building (it is used as a `compileOnly` dependency and for the `runClient`/`runServer`
development runs).

## License

MIT
