# Too Many Furnaces

> Ten tiers of furnaces, ten tiers of three-lane forges and a full upgrade system for Minecraft 26.2.

简体中文文档：[README_cn.md](README_cn.md)

Inspired by [Better Furnaces Reforged](https://github.com/Wilyicaro/BetterFurnacesReforged) and rebuilt from scratch against the unobfuscated Minecraft 26.2 API. Runs on **Fabric**, **Forge** and **NeoForge**.

---

## Furnaces

Every tier can be upgraded in place to the next one and accepts upgrades such as liquid fuel, fuel efficiency and ore processing.

| Tier | Cook time | Compared to a vanilla furnace |
|---|---|---|
| Copper | 175 ticks | 1.14× |
| Iron | 150 ticks | 1.33× |
| Steel | 125 ticks | 1.60× |
| Gold | 100 ticks | 2× |
| Amethyst | 75 ticks | 2.67× |
| Diamond | 50 ticks | 4× |
| Platinum | 25 ticks | 8× |
| Netherhot | 8 ticks | 25× |
| Extreme | 4 ticks | 50× |
| Ultimate | 1 tick | 200× |

> A vanilla furnace takes 200 ticks, so the multiplier is 200 divided by the tier's cook time.

## Forges

One forge per furnace tier, but each one **smelts three different items at the same time** — three independent progress bars, one shared fuel slot and three separate outputs — giving three times the throughput of the matching furnace. All upgrades work here too.

## Upgrades

Right click a furnace or forge while holding an upgrade to install it. Every machine has three hidden upgrade slots.

| Upgrade | Effect |
|---|---|
| Fuel Efficiency / Advanced Fuel Efficiency | Fuel lasts ×2 / another ×2 |
| Ore Processing / Raw Ore Processing | ×2 output for ores and ore blocks / ×2 for raw ores |
| Advanced / Ultimate Ore Processing | Up to ×4 (the ultimate version covers ores and raw ores alike) |
| Blasting / Smoking | Switches the machine to blast furnace or smoker recipes |
| Storage | Doubles every slot's capacity, up to 99 items |
| Auto Input / Auto Output | Exchanges items with adjacent inventories every 8 ticks |
| Factory | Auto input and auto output on every side |
| Redstone Signal | Right click to cycle through three redstone modes |
| Liquid Fuel | Allows lava buckets as fuel and returns the empty bucket |
| Tier Upgrade | Upgrades the machine in place, keeping its inventory and upgrades |

**Not implemented yet (placeholders):** Energy, XP Tank, Generator, Color and Piping. They can be crafted but have no effect, and their tooltips say so.

## Other blocks

- **Conductor blocks** (iron / gold / netherhot): crafting ingredients for forges
- **Cobblestone Generator**: consumes lava and water to produce cobblestone
- **Fuel Verifier**: shows how many items a fuel can smelt

## Performance

- Every machine renders with a **baked block model**, exactly like the vanilla furnace, switching only on the `lit` block state
- **No** block entity renderer and no per-frame client rendering
- Smelting logic runs once per tick, on the server only
- Recipe lookups are cached
- Auto input/output scans neighbouring containers every 8 ticks

## Installation

Requires **Java 25** and one of the following loaders:

| Platform | Requirement |
|---|---|
| Fabric | Loader 0.19.5+, Fabric API 0.161.0+ |
| Forge | Forge 26.2-65.0.0+ |
| NeoForge | NeoForge 26.2.0.1-beta+ |

Drop the matching jar into your `mods/` folder. Blocks placed by an older version disappear after the mod id changed, because Minecraft resolves registry entries by id — back up your worlds first.

## Building

All three platforms share a single source tree (`src/` only uses vanilla APIs) and each has its own Gradle subproject:

```bash
# Fabric (repo root)
./gradlew build

# NeoForge
cd neoforge && ../gradlew build

# Forge
cd forge && ../gradlew build
```

JDK 25 is required, and Gradle needs to trust the Windows certificate store when downloading dependencies:

```bash
export JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
```

Behind an HTTP proxy, the Forge downloader does not read environment variables, so pass them explicitly:

```bash
-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port> \
-Dhttp.proxyHost=<host>  -Dhttp.proxyPort=<port>
```

## Credits

- Concept based on **Better Furnaces** (TheFrogMC) and **Iron Furnaces** (Qelifern)
- Textures are reused from [Better Furnaces Reforged](https://github.com/Wilyicaro/BetterFurnacesReforged) by Icaro K. Bomfim, MIT licensed
- The multi-loader layout follows [Mouse Tweaks](https://github.com/YaLTeR/MouseTweaks)

## License

[MIT](LICENSE). The copyright notice and credits of Better Furnaces Reforged are kept intact.
