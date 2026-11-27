# Miku Addon for Evernight

[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.11-green.svg)](https://www.minecraft.net/)
[![Evernight](https://img.shields.io/badge/evernight-%E2%89%A51.0.3-purple.svg)](https://github.com/Evernight-Client/Evernight)

把 Meteor Client 插件 **meteor-miku** 里 **Evernight 本体没有的功能**移植到 Evernight 的插件框架。

仓库：<https://github.com/zhehua1814686727-bit/evernight-miku> · 变更记录：[CHANGELOG.md](CHANGELOG.md)

- 目标环境：Minecraft 1.21.11 · Fabric · **Evernight 1.2.5+** · Java 21
- 插件 id：`mikumiku`，入口 `evernight-addon`
- 当前 **20 个模块**：建造、移动特调、实用工具与 HUD
- 使用 1.0.3 的 `AddonContext.registerCategory` 注册 3 个专属分类：**Miku / Miku 建设 / Miku 特调**

## 安装

1. 安装 Fabric Loader 0.19.5+、Fabric API、Evernight 1.2.5（1.21.11）。
2. 拿到插件 jar（二选一）：
   - 从 [Releases](https://github.com/zhehua1814686727-bit/evernight-miku/releases) 下载 `evernight-miku-1.2.0.jar`
   - 或按下面的「从源码构建」自己编译
3. 把 jar 放进 `.minecraft/mods/`。
4. 启动游戏，日志出现 `Loaded addon Miku Addon 1.2.0 (mikumiku)`；游戏内 `;addons` 可查看插件与模块数。

模块出现在 ClickGUI 的 Miku / Miku 建设 / Miku 特调 分类下（排在原生分类之后），中文名来自
`assets/mikumiku/lang/zh_cn.json`（英文客户端显示英文名）。

## 从源码构建

克隆后先跑一次准备脚本（生成映射表、下载并重映射本体 jar，仓库里不提交这些大文件）：

```bash
python tools/prepare.py   # 需要本机已有 Loom 缓存的 1.21.11 映射
```

工程是标准 Loom 工程（`build.gradle`），Loom 1.18.4 需要 **JDK 25**：

```bash
./gradlew build        # 产物 build/libs/evernight-miku-1.2.0.jar
```

如果本机只有 JDK 21（Loom 1.18.4 无法启动），用离线脚本，效果等价：

```bash
python tools/build.py  # javac -> tiny-remapper(named→intermediary) -> 打包
```

脚本依赖 Loom 已缓存的 named Minecraft jar、`libs/evernight-named.jar`（本体 jar 的 named 重映射版）
和 Gradle 缓存里的 tiny-remapper / ASM / brigadier / gson / netty。

改语言表后运行 `python tools/gen_lang.py` 重新生成两份 JSON（键集合保持一致，词条表在 `tools/lang_seed.json`）。

### 版本约定

版本号只有一个来源：`gradle.properties` 的 `mod_version`。`tools/build.py` 会读取它并写进
`fabric.mod.json` 与 jar 文件名，**不要再在脚本或文档里硬编码版本号**。每次功能变更（新增/删除模块、
行为改动）都要同步提升 `mod_version`，并补一条 `CHANGELOG.md`。

### 构建后自检（重要）

`tools/verify/` 下有三个检查器，改动后建议都跑一遍：

```bash
# 1) 所有 Minecraft 成员引用都能在 intermediary 目标 jar 里解析
javac -cp "<asm>;<asm-tree>" -d build/verify tools/verify/VerifyRemap.java
java -cp "build/verify;<asm>;<asm-tree>" VerifyRemap build/libs/evernight-miku-1.2.0.jar <minecraft-merged-intermediary.jar>
# 期望输出：checked=409 missing=0

# 2) 所有 class 能在 MC + 本体 jar 上加载
javac -d build/verify tools/verify/CheckJar.java
java -cp build/verify CheckJar build/libs/evernight-miku-1.2.0.jar <mc.jar> <evernight.jar> <brigadier> <gson> <slf4j>
# 期望输出：ok=27 fail=0

# 3) 模块构造函数（需要真实依赖 classpath）
javac @javac.args && java @java.args
# 期望输出：ok=30 fail=0
```

`VerifyRemap` 是必须的：`named2int.tiny` 的成员列顺序一旦写反，字段/方法名不会重映射，
编译能过、类能加载，但进游戏第一次访问 MC 成员就会 `NoSuchMethodError`。

## 模块（20）

### Miku（8）

| 模块 | 原模块 | 说明 |
| --- | --- | --- |
| `FishingRodFace` | 鱼竿糊脸 | 对最近敌人甩鱼竿 |
| `DeathCommand` | 死亡自动指令 | 死亡后自动发送自定义指令 |
| `AutoLoginPlus` | 自动登录Plus | 进服后按模板发送 `/login` |
| `PlayerAlertPlus` | 看门狗 | 指定玩家进入范围时提醒 |
| `HandsomeSpin` | 靓仔转圈 | 角色持续旋转 |
| `InventoryPrinter` | 库存打印 | 把背包内容打到聊天栏 |
| `ChestAuraPlus` | 翻箱倒柜 | 自动打开附近箱子（不拿东西） |
| `ThreeCCheck` | 3C萌新识别（Tanghulu） | 查 3c3u.info 总在线时长，低于阈值标萌新；可自动查范围内玩家 |

### Miku 建设（4）

| 模块 | 原模块 | 说明 |
| --- | --- | --- |
| `AutoSlabPlus` | 铺半砖+ | 地面自动铺半砖防刷怪 |
| `TreeAuraPlus` | 自动种树 | 在周围放树苗 |
| `AutoDigFeet` | 脚下挖掘 | 挖掉挡路的脚前方块 |
| `AutoTouchFire` | 自动消防 | 扑灭周围火焰 |
| `AutoTreeChop` | 自动砍树（Tanghulu） | 自动砍周围整棵树，可选从上往下、自动换斧、等掉落 |

### Miku 特调（6）

| 模块 | 原模块 | 说明 |
| --- | --- | --- |
| `HoverPlus` | 悬停 | 空中保持位置（按跳跃键触发） |
| `ElytraArmorSwitch` | 自动鞘翅切换 | 地面穿胸甲、空中换鞘翅 |
| `ElytraUnbreak` | 无限耐久鞘翅 | 低耐久自动换鞘翅，可选自动烟花 |
| `AutoFollowPlayer` | 跟随玩家 | 朝指定玩家移动 |
| `AutoBlockCenter` | 自动居中 | 自动站到方块中心 |
| `VanityESP` | 珍品ESP（Tanghulu） | 高亮放地图的物品展示框与旗帜，方便找藏宝点 |

### HUD（1）

| 模块 | 原模块 | 说明 |
| --- | --- | --- |
| `PotCountHud` | 药水计数 | 溅射药水数量 + 抗性剩余时间 |

## 目录结构

```
evernight-miku/
├── build.gradle / settings.gradle / gradle.properties   Loom 工程
├── libs/evernight-1.0.3.jar                             本体发行 jar
├── libs/evernight-named.jar                             本体 named 重映射（离线构建用）
├── src/main/java/com/github/mikumiku/evernight/
│   ├── MikuAddon.java        入口（EvernightAddon）
│   ├── MikuCategories.java   Miku / Miku 建设 / Miku 特调 分类注册
│   ├── MikuModule.java       模块基类（AddonModule）
│   ├── MikuHudModule.java    HUD 基类（HudWidgetModule + addons 命名空间）
│   ├── MikuModules.java      注册表（一行一个模块）
│   ├── MikuCommand.java      ;miku 指令
│   ├── modules/              30 个模块
│   └── util/MikuUtils.java   放置/旋转/搜索等公共工具
├── src/main/resources/
│   ├── fabric.mod.json
│   └── assets/mikumiku/lang/{en_us,zh_cn}.json
└── tools/
    ├── build.py              离线构建（javac + tiny-remapper + 打包）
    ├── gen_lang.py           语言表生成
    ├── int2named.tiny        intermediary → named
    ├── named2int.tiny        named → intermediary
    └── verify/               VerifyRemap / CheckJar / BaritoneProbe / CtorTest
```

## 与 1.2.5 的适配

- 依赖 `evernight-1.2.5.jar`（`depends.evernight >= 1.2.5`），编译与运行都用同一个版本。
- `MikuCategories` 在 `onInitialize` 里先 `registerCategory` 再注册模块（模块会记住构造时的分类实例），注册失败回退内置分类。
- `AddonModule` 不使用 Java 里的 description 兜底，所有显示文本都来自语言表。
- **设置提示文字的命名空间限制**：ClickGUI 的设置 tooltip 走 `Translations.settingDescription(module, setting)`，
  内部硬编码 `evernight.descriptions.settings.<模块>.<设置>`。为了不让 tooltip 显示原始键，`gen_lang.py`
  会额外生成这组键（按本插件唯一的模块名限定）；分类标题、模块介绍和枚举值也同时写进 `evernight.*` 与
  `addons.mikumiku.*` 两套命名空间。
- 1.2.x 新增 `Module#requiresMod`，未安装前置模组时模块会自动隐藏，本插件暂未使用。

## 许可

原插件 `meteor-miku` 版权归 mikumiku 所有；Evernight 本体为 GPL-3.0-only。本移植工程仅链接本体公开 API，
按 GPL-3.0 处理。
