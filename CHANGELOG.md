# Changelog

## 1.5.0 — 2026-11-25

### 变更

- `Printer` 改为依赖 **Litematica**：直接读取当前加载的投影世界，原点/旋转/镜像沿用 Litematica 的放置设置，
  不再自己解析 `.litematic` 文件
- 未安装 Litematica 或未加载投影时给出聊天提示并自动关闭模块，不会影响插件加载
- 移除文件解析相关设置（File / Reload / Anchor / Origin / Rotation / Mirror），新增 `ScanInterval`

## 1.4.0 — 2026-11-25

### 新增

- `ThreeCCheck`（3C萌新识别）：调用 3c3u.info 公开接口查玩家总在线时长，低于阈值判定为萌新；
  支持手动查询、范围内自动查询（默认 200 米）、结果缓存与 2 秒请求间隔

## 1.3.0 — 2026-11-25

从 Meteor 插件 [Tanghulu](https://github.com/Tanghulu2019-a/Tanghulu) 移植了本体没有的两个功能。

### 新增

- `AutoTreeChop`（自动砍树）：扫描周围整棵树，默认从最高处往下砍，可自动换斧、砍完等待掉落物
- `VanityESP`（珍品ESP）：高亮放着地图的物品展示框与附近的旗帜，用本体 `FeatureBoxRenderer` 绘制
- 版本号提升到 1.3.0（`gradle.properties`）

### 说明

Tanghulu 里其余模块与本体或本插件重复（AutoLog / AutoMiner / AutoSlab / AutoTrash / AutoXP /
Nuker / Scaffold / KillAura / ElytraFly 系 / PlayerAlert / InventoryPrinter / NoJumpDelay 等），
或依赖本插件无法提供的运行时（seedfinding 库、Meteor 的 Notebot / Waypoints / ElytraFly 内部、
mixin 注入），因此没有移植。

## 1.2.0 — 2026-11-25

### 新增

- `Printer`（投影打印机）：直接读取 Litematica 的 `.litematic` 文件并在身边重建，无需安装 Litematica。
  支持玩家/固定锚点、旋转、镜像、每 tick 放置上限，以及楼梯/半砖的基础朝向处理
- 版本号改为单一来源：`tools/build.py` 从 `gradle.properties` 读取 `mod_version`

### 修复

- `tools/prepare.py` 生成的 `named2int.tiny` 描述符仍是 official 命名空间，导致干净克隆构建出的 jar
  运行时 `NoSuchMethodError`；现在改用 official→named 表转换，并加了生成后校验

## 1.1.0 — 2026-11-24

适配 Evernight 1.0.3。

### 新增

- 3 个插件分类：`Miku` / `Miku 建设` / `Miku 特调`（`AddonContext.registerCategory`）
- `PotCountHud`：溅射药水数量 + 抗性剩余时间
- 设置项 tooltip 的中英描述，修复显示原始语言键的问题

### 变更

- 依赖升级到 `evernight-1.0.3.jar`（`depends.evernight >= 1.0.3`）
- 模块集合：18 个，覆盖建造、移动特调、实用工具与 HUD
- 源码树不再提交第三方 jar 与映射表，改由 `tools/prepare.py` 生成

### 修复

- 重映射脚本 `named2int.tiny` 的成员列顺序错误导致运行时 `NoSuchMethodError`
- `tools/build.py` 的重映射 classpath 改用源命名空间 jar

## 1.0.0 — 2026-11-23

首个版本：对接 Evernight 1.0.1。
