# Too Many Furnaces

> A Minecraft 26.2 mod with ten tiers of furnaces, ten tiers of three-lane forges and a full upgrade system.
> 适用于 Minecraft 26.2 的模组：十级熔炉、十级三通道锻造炉，以及完整的升级体系。

受 [Better Furnaces Reforged](https://github.com/Wilyicaro/BetterFurnacesReforged) 启发，基于未混淆的 Minecraft 26.2 API 重新实现。同时支持 **Fabric**、**Forge** 和 **NeoForge** 三个加载器。

---

## 熔炉 Furnaces

每档都可以用等级升级原地升到更高档，并支持液态燃料、燃料效率、矿石增产等功能升级。

| 档位 | 烧炼耗时 | 相对原版熔炉 |
|---|---|---|
| 铜熔炉 Copper | 175 tick | 1.14× |
| 铁熔炉 Iron | 150 tick | 1.33× |
| 钢熔炉 Steel | 125 tick | 1.60× |
| 金熔炉 Gold | 100 tick | 2× |
| 紫水晶熔炉 Amethyst | 75 tick | 2.67× |
| 钻石熔炉 Diamond | 50 tick | 4× |
| 铂金熔炉 Platinum | 25 tick | 8× |
| 下界热熔炉 Netherhot | 8 tick | 25× |
| 极限熔炉 Extreme | 4 tick | 50× |
| 终极熔炉 Ultimate | 1 tick | 200× |

> 原版熔炉为 200 tick，倍速即 200 ÷ 该档耗时。

## 锻造炉 Forges

与熔炉一一对应，但**可同时冶炼 3 种不同物品**（三条独立进度、共享燃料、三个独立输出），因此吞吐量是同档熔炉的 3 倍。同样支持全部升级。

## 升级 Upgrades

手持升级右键熔炉或锻造炉即可装入（机器有 3 个隐藏升级槽）。

| 升级 | 效果 |
|---|---|
| 燃料效率 / 高级燃料效率 | 燃料燃烧时长 ×2 / 再 ×2 |
| 矿石处理 / 粗矿处理 | 矿石与矿 block 产出 ×2 / 粗矿 ×2 |
| 高级 / 终极矿石处理 | 最高 ×4（终极版对矿石与粗矿同时生效） |
| 高炉 / 烟熏 | 切换为高炉或烟熏炉配方 |
| 存储 | 各槽位容量翻倍（上限 99） |
| 自动输入 / 自动输出 | 每 8 tick 与相邻容器交换物品 |
| 工厂 | 全方向自动输入 + 输出 |
| 红石控制 | 右键循环三种红石模式 |
| 液态燃料 | 允许用熔岩桶作燃料，烧完归还空桶 |
| 等级升级 | 原地升级档位，保留物品栏与已装升级 |

**尚未实装（占位）**：能源、液态经验罐、发电机、染色、管道。它们可以合成，但装上去没有实际效果，物品提示中已明确标注。

## 其它方块

- **导体方块**（铁 / 金 / 下界热）：锻造炉的合成材料
- **圆石生成器**：消耗岩浆与水，持续产出圆石
- **燃料检测器**：查看燃料可烧炼多少物品

## 性能

- 所有机器使用**烘焙方块模型**渲染，与原版熔炉一致，仅靠 `lit` 状态切换亮/灭
- **不使用**方块实体渲染器，也没有逐帧客户端渲染
- 熔炼逻辑只在服务端每 tick 执行一次
- 配方查询结果带缓存
- 自动输入/输出每 8 tick 才扫描一次相邻容器

## 安装

需要 **Java 25**，并按平台安装对应加载器：

| 平台 | 要求 |
|---|---|
| Fabric | Loader 0.19.5+，Fabric API 0.161.0+ |
| Forge | Forge 26.2-65.0.0+ |
| NeoForge | NeoForge 26.2.0.1-beta+ |

把对应平台的 JAR 放进 `mods/` 文件夹即可。存档中已放置的旧版本方块在升级时会消失（Minecraft 按 mod id 查找注册项），请提前备份。

## 构建

三个平台共用一份源码（`src/` 只依赖原版 API），各自有独立的 Gradle 子项目：

```bash
# Fabric（根目录）
./gradlew build

# NeoForge
cd neoforge && ../gradlew build

# Forge
cd forge && ../gradlew build
```

需要 JDK 25，并且 Gradle 下载依赖时要信任 Windows 证书库：

```bash
export JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
```

若你的网络走 HTTP 代理，Forge 侧的下载器不会读取环境变量，需要额外传入：

```bash
-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port> \
-Dhttp.proxyHost=<host>  -Dhttp.proxyPort=<port>
```

## 致谢

- 概念源自 **Better Furnaces**（TheFrogMC）与 **Iron Furnaces**（Qelifern）
- 本项目沿用 [Better Furnaces Reforged](https://github.com/Wilyicaro/BetterFurnacesReforged) 的材质，原作者 Icaro K. Bomfim（MIT 授权）
- 多加载器结构参考 [Mouse Tweaks](https://github.com/YaLTeR/MouseTweaks)

## 许可证

[MIT](LICENSE)。上游 Better Furnaces Reforged 的版权声明与致谢完整保留。
