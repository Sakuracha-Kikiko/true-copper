# True Copper

**中文** | [English](README.md)

为 [TheZenithSword](https://github.com/OceanScenery/MinecraftZenithMod) 提供了一些体验优化功能与 [Touhou Little Maid](https://github.com/TartaricAcid/TouhouLittleMaid) 兼容性支持。

> 不要与 [Zenith](https://github.com/gjdbg148525/Zenith), [The Zenith Sword](https://github.com/LeoMinecraftModding/the-zenith-sword), [The Zenith](https://github.com/CHUJIAN-afk/Zenith) 混淆

## 功能特性

> 以下提到的“天顶剑”均特指该模组 (TheZenithSword) 的天顶剑

### 挥击伤害延迟结算

天顶剑的一次挥击会结算两次伤害：

1. **瞬时结算**：攻击被触发的那一刻，挥击射线 200 格范围内的所有实体立刻受到伤害。
2. **按飞剑结算**：当某把飞剑弹体真正扫过某个实体时再结算一次——视距离而定，可能发生在同一 tick，也可能晚至 21 tick 之后。

正是第一段结算让目标在飞剑还没离开玩家手中时就已经掉血，这看起来有些反直觉。True Copper 会把这一段暂缓，等飞剑抵达时再补上，从而让伤害数字与动画表现保持一致。

行为规则：

- **孤立挥击**（近期没有任何天顶剑射弹命中过目标）：瞬时伤害先被暂存，由第一把触及目标的飞剑重新施加。
- **持续输出**（目标在 `sustainedWindowTicks` 内被任意天顶剑刃命中过，或已有一笔暂存伤害在等待）：伤害立即生效，连续压制不会被延迟。
- **飞剑始终没有抵达**（目标移动、剑刃被地形挡住……~~不过它真的会不命中吗？~~）：超过 `pendingTimeoutTicks` 后，暂存的伤害仍会被施加，不会丢失。

选项默认开启。只有生物实体会受到影响；掉落物、经验球等非生物目标仍保持原版的瞬时结算行为。

### 挥击与命中音效

天顶剑本身没有任何音效。True Copper 新增了挥击音效（`true_copper:zenith_slash`），并让挥击声与目标的受伤声能在更远的距离上被听到，以增强打击感。

- **挥击**：每次挥击都会在攻击者所在位置播放 `true_copper:zenith_slash`。攻击者自己的客户端会额外播放一份直接、非定位的副本，因为零距离的世界音效仍会经过 3D 处理，听起来可能和挥击动作脱节。其他玩家则从攻击者所在位置听到该音效，并在超出 `swingSoundRange`（默认 64 格）后逐渐衰减。
- **命中**：当天顶剑命中生效时（无论是瞬时结算还是飞剑命中），目标自身的受伤/死亡音效会重放给 `hitSoundRange`（默认 64 格）范围内的玩家，并随距离衰减。原版 16 格半径内的玩家本就能听到普通音效，因此会被跳过，避免重复叠加。音量等于目标自身音效的音量乘以 `hitSoundVolume`。
- 原版音效包会在音效事件的作用范围（大多数音效为 16 格）处停止传输，因此扩展范围的音效通过一个小型自定义数据包发送，并在客户端进行衰减。关闭该功能即可完全恢复原版行为。

选项默认开启。随包提供的 `zenith_slash.ogg` 是且必须是单声道。这是作者踩的一个小坑：Minecraft (OpenAL) 只会对单声道音效做空间化处理，双声道会表现为类似客户端直接播放。所以如果你要替换这个文件，请保持单声道。

### 女仆工作模式

为 Touhou Little Maid（车万女仆）新增 **天顶剑** 工作模式：手持天顶剑的女仆会像玩家一样战斗。

- 每次挥击前她都会先面朝目标，因为这把剑的射线是基于攻击者的朝向构建的。
- 她使用自己的冷却时间（默认为玩家使用天顶剑速度的一半，即 8 刻），而不是原版攻击速度——攻击速度属性对天顶剑的攻击模式没有意义，而且这是某种平衡性调整（确信）。
- 挥击时她获得与玩家在天顶剑模式下相同的额外伤害，因此数值一致。
- 伤害延迟结算同样作用于她的攻击（若启用），所以飞剑仍会在血条变化之前抵达。
- 她的挥击遵守下方的友伤规则：默认情况下她绝不会伤害玩家或其他女仆，因为这把剑会在一条长直线上命中一切，否则女仆在瞄准僵尸时可能把自己的主人劈成两半。

选项默认开启，变更选项需要重启游戏。天顶剑本身的瞬时伤害、剑刃伤害和伤害过滤器都没有改动：女仆只是调用了与玩家相同的攻击入口。

### 友军伤害

四个独立选项决定天顶剑可以伤害哪些方向：

- **女仆 → 玩家**：allowMaidDamagePlayer
- **女仆 → 女仆**：allowMaidDamageMaid
- **玩家 → 女仆**：allowPlayerDamageMaid
- **玩家 → 玩家**：allowPlayerDamagePlayer

四个选项默认全部关闭，因此友方之间永远不会互相伤害。该检查在两个伤害入口之前执行，因此同时覆盖瞬时结算、飞剑命中以及延迟结算补发的伤害。开启某个选项只会放开对应方向，其余三个方向仍受保护。

如果整合包的数值支持你这么做，你也可以开启「玩家 → 玩家」然后进行天顶剑 PvP。~~但我说实话，这把剑的数值对于 PvP 来说还是太超前了。~~

### 外观变体

模组添加了两把新的天顶剑外观变体：

- **真铜短剑**（True Copper Shortsword，`true_copper:true_copper_shortsword`），原汁原味的 Terraria 微光转化体验
- **真木剑**（True Wooden Sword，`true_copper:true_wooden_sword`），更有 Minecraft 特色的版本

正如 Terraria 中的真铜短剑之于天顶剑，它们的伤害、攻击距离、冷却与附魔行为等完全相同，并且和天顶剑一样参与延迟伤害结算（若开启）。区别只在于物品贴图、飞剑模型和拖尾颜色等外观。

#### 微光转化

**外观 → 启用微光转化** 选项（默认开启）会添加一个单物品无序合成循环：

- 天顶剑 → 真铜短剑
- 真铜短剑 → 真木剑
- 真木剑 → 天顶剑

产物会保留输入物品的所有数据组件，因此附魔、自定义名称、lore 描述和耐久度都能在三种形态间完整继承。该选项只控制配方匹配，并且可以在运行时修改。

## 配置

以下所有内容都可以在游戏内编辑：**模组 → True Copper → 配置**。游戏内配置界面是 NeoForge 自带的配置 UI，无需额外的配置库。TOML 文件仍然保留，方便整合包作者使用。

`config/true_copper-common.toml`：

```toml
[deferred_damage]
    # 把瞬时挥击伤害推迟到天顶剑刃真正命中目标时再结算。
    deferInstantDamage = true
    # 目标在这么多 tick 内被天顶剑刃命中过，即视为持续输出。
    sustainedWindowTicks = 10
    # 暂存的伤害最多等待多久（tick），超时后仍会施加。
    pendingTimeoutTicks = 25

[sounds]
    # 每次天顶剑挥击开始时播放斩击音效。
    playZenithSlashSound = true
    zenithSlashVolume = 1.0
    # 其他玩家能听到挥击声的距离（攻击者始终以满音量听到）。
    swingSoundRange = 64
    # 把命中目标的受伤/死亡音效重放给附近玩家，突破原版 16 格限制。
    playZenithHitSound = true
    # 该受伤/死亡音效的传播距离。
    hitSoundRange = 64
    # 在目标自身音效音量基础上的倍率。
    hitSoundVolume = 0.8
    # 距离小于该值的玩家只保留原版音效（0 = 也发送给近处玩家）。
    hitSoundMinDistance = 16

[appearance]
    # 单物品无序合成循环：天顶剑 -> 真铜短剑 -> 真木剑 -> 天顶剑，并保留数据组件。
    enableShimmeringTransmutation = true

[friendly_fire]
    # 四个方向默认全部关闭：友方之间永不互相伤害。
    allowMaidDamagePlayer = false
    allowMaidDamageMaid = false
    allowPlayerDamageMaid = false
    allowPlayerDamagePlayer = false

[maid]
    # 注册「天顶剑」女仆工作模式。
    enableZenithMaidTask = true
    # 女仆使用该剑时搜索目标的范围。
    zenithSearchRange = 64
    # 两次挥击之间固定的 tick 数（不随攻击速度缩放，因为在这里攻击速度没有意义）。
    zenithAttackCooldownTicks = 8
    # 挥击时的额外伤害，以剑自身伤害值为倍率（1.0 = 与玩家一致）。
    zenithDamageBonus = 1.0
```

## 环境要求

- Minecraft 1.21.1
- NeoForge 21.1.250 或更高版本
- TheZenithSword 2.0.2 或更高版本

Touhou Little Maid 1.5.3 或更高版本为可选依赖：未安装时只是不会注册女仆工作模式，其余功能均正常工作。

> TheZenithSword 的更新可能会破坏某些签名导致本模组的 Mixin 注入失败。这不是 TheZenithSword 的问题，请向本模组作者反馈

## 一些问题

Q：看起来像 AI 写的？

A：你猜对了，就是 AI 写的。作者本人的 Java 知识比较匮乏，尤其是 Mixin 根本不会写。如果你对 Vibe Coding 有意见，大可不必理会我的模组。

Q：我能使用它或者把它加进整合包吗？

A：本模组使用 MIT 许可证。MIT 允许你做什么，你就能做什么。当然，作者很乐意你在制作整合包之类的时候和他说一声。

Q：会有其他版本吗？

A：作者精力有限，可能只能维护 1.21.1 一个版本。将来可能会跟进 NeoForge LTS 版本（目前是26.1.2），当然如果你想的话，你可以自己做移植，这是 MIT 允许你做的。

Q：为什么是 TheZenithSword 而不是别的天顶剑模组？

A：其实很大程度上和它的作者有关。就作者个人看来，OceanScenery 是个挺友善的人，和作者进行过一些交流，而 TheZenithSword 也是还原度很不错的天顶剑模组，且同样以 MIT 开源。作者很喜欢这种 Minecraft 开源社区的氛围。

> 我必须承认的是，TheZenith 是个更好的模组，它的美术和实现比其他三个天顶剑都要更好。但很遗憾（也可能很庆幸）的是，它是个 All Rights Reserved 的模组，虽然它的源码可见，但作者明确禁止其他人创作附属作品。他的这个模组并不属于开源社区，而且法律实际上也禁止我这么做。平心而论，我并不是对 DMCA 有意见，你的版权，怎样处置是你的自由，但既然你没有选择社区，那社区自然也不会选择你。

## 构建

```
./gradlew build
```

构建前请把天顶剑的 jar（如需女仆工作模式，还要放入 Touhou Little Maid 的 jar）放进 `libs/`：该目录下的每个 jar 都会作为 `compileOnly` 依赖，并用于 `runClient`/`runServer` 开发运行。

## 许可证

MIT

## 致谢

感谢 [OceanScenery](https://github.com/OceanScenery) 和他的 [TheZenithSword](https://github.com/OceanScenery/MinecraftZenithMod) 模组

感谢[车万女仆](https://github.com/TartaricAcid/TouhouLittleMaid)模组及其作者[酒石酸菌](https://github.com/TartaricAcid)和模组贡献者

感谢[杭州深度求索人工智能基础技术研究有限公司](https://www.deepseek.com/)为本模组提供代码支持

**感谢每一位Minecraft开源社区的创作者们**
