# Changelog

## 2.0.0 — 2026-11-27

### 变更

- 升级到 **Evernight 1.2.5**（`evernight_version` / `depends.evernight` 同步）
- 删除 `AutoSM`（自动社交）
- 删除 `Printer`（投影打印机）及 `LitematicaBridge`
- 删除全部 Slimefun/SLM 相关模块：`TotemLog`、`NoQDrop`、`EnderEyeLog`、`Disabler`、`AttackEnhance`、
  `MovExtra`、`ElytraFlight`、`ElytraBot`、`ElytraExtra`、`SimpleElytraFlight`，以及 `ElytraPhysics`
- 模块数 32 → **20**（Miku 8 / 建设 4 / 特调 6 / HUD 1）

### 说明

删除后语言表、注册表、README 同步清理，无残留键。

## 2.0.0 — 2026-11-27

### 变更

- 升级到 **Evernight 1.2.5**（`evernight_version` / `depends.evernight` 同步）
- 删除 `AutoSM`（自动社交）
- 删除 `Printer`（投影打印机）及 `LitematicaBridge`
- 删除全部 Slimefun/SLM 相关模块：`TotemLog`、`NoQDrop`、`EnderEyeLog`、`Disabler`、`AttackEnhance`、
  `MovExtra`、`ElytraFlight`、`ElytraBot`、`ElytraExtra`、`SimpleElytraFlight`，以及 `ElytraPhysics`
- 模块数 32 → **20**（Miku 8 / 建设 4 / 特调 6 / HUD 1）

### 说明

删除后语言表、注册表、README 同步清理，无残留键。

## 1.10.0 — 2026-11-27

### 新增

- `SimpleElytraFlight`（简易鞘翅飞行）：从 SlimefunHelper 的 `ElytraFlight`「simple-flight-control」组
  单独拆出——长按跳跃起飞、前进时自动保持巡航俯仰、速度不足自动烟花、落地自动关闭

### 修复

- 枚举下拉框显示原始键（如 `names.values.MaceAura`）：补 `evernight.names.values.*` 与
  `addons.mikumiku.names.values.*` 两套标签，覆盖 Follow/MaceAura/SpearAura、Vanilla/Packet、
  Control/Rotation/None、Void/Fireworks、Grim/Matrix 等 17 个枚举值

## 1.9.2 — 2026-11-27

### 修复

- `ElytraFlight` 与本体自带 `ElytraFly` **显示名冲突**（都叫「鞘翅飞行」），用户点到的很可能是本体模块。
  改名：`Miku 鞘翅飞行` / `Miku 鞘翅追击`，介绍同步更新
- 两个鞘翅模块现在会把俯仰/偏航用 `PacketSender.sendRotation` 同步给服务端，之前只改了客户端视角

## 1.9.1 — 2026-11-26

### 修复

- 文字显示：ClickGUI 的分类标题与模块介绍之前会退回到原始键（`evernight.names.categories.*` /
  `evernight.descriptions.modules.*`），现在把分类名和模块介绍同时写进客户端命名空间
- `fabric.mod.json` 的 description 改成中文

## 1.9.0 — 2026-11-26

### 新增

- `ElytraBot` 自动攻击：`AutoAttack`（默认开）+ `AttackRange`（默认 3.0），三种模式共用
- `ElytraBot` 发包攻击：`AttackMode` = `Vanilla`（走 `MultiPlayerGameMode`）/ `Packet`
  （直接发 `ServerboundInteractPacket.createAttackPacket` + `ServerboundSwingPacket`，
  用本体的 `PacketSender.sendAttack(entityId)`）

## 1.8.4 — 2026-11-26

### 变更

`ElytraBot` 按原版结构重建：

- 新增 `TargetAction` 状态机（`Escaping` / `Towards` / `Circling` / `SlowSpeed` / `Afk`），
  每个 tick 根据目标相对高度、水平距离和自身速度重新判定
- 新增 `util/ElytraPhysics`：照搬原版 `EntityUtils.calculateGlidingVelocity` 的原版滑翔物理
  （重力、抬升、俯冲加速、视线修正、0.99/0.98 阻力），以及 `ElytraOptimizeUtils` 的拉升/俯冲扫描
  （遍历 -80°~-1° / 1°~80°，模拟一 tick 后取最优）
- `Follow`：Towards 直飞、Circling 切向绕圈、SlowSpeed 用物理扫描选最佳拉升角、Escaping 俯冲脱离
- `MaceAura`：低于 `MaceHeight` 用扫描出的最佳拉升角爬升，够了再俯冲，下落距离够才切锤
- `SpearAura`：远距离直飞，进入 `CombatSpearRange` 保持拉升角
- 新增 `CircleExtraRange`、`CombatSmoothFlight`、`PredictTicks`（目标移动预测，原版 0.5 tick）

## 1.8.3 — 2026-11-26

### 变更

- `ElytraBot` 按 SlimefunHelper 原版重写：
  - 模式 `Mode`：`Follow` / `MaceAura` / `SpearAura`（默认 Follow，与原版一致）
  - 设置对齐原版 key 与默认值：`Range`(80)、`PlayerOnly`(true)、`AutoControlElytra`、
    `DynamicTarget`、`OnlyWhenNoWasd`、`CombatRange`(10.0)、`CombatSpearRange`(11.0)、
    `MaceMinFallDistance`(1.5)、`MaceHeight`(10.0)、`MaceSwapMinDelay`(6)
  - 行为：Follow 追到 `CombatRange` 后绕圈；MaceAura 先爬到 `MaceHeight` 再俯冲，下落距离够才切锤并攻击；
    SpearAura 保持 `CombatSpearRange` 距离并戳；速度不足自动补烟花

## 1.8.2 — 2026-11-26

按上游 **SlimefunHelper**（`me.matl114`，1.21.11 分支）源码重写上一批模块，不再参考工作区里的
SlimefunMeteorAddon jar。

### 变更

- `ElytraFlight`：按原版输入映射重写——**跳跃=拉升、潜行=俯冲、移动键=方向**（之前错用 W/S 控制俯仰）；
  补齐 `FlightMode`(Control/Rotation/None)、`MotionMode`(Void/Fireworks)、`FireworksRotationTicks`、
  拉升/俯冲/平飞角度覆盖、`HoldJumpTakeoffTicks`、`AutoFly`、`LandAutoClose`
- `Disabler`：补齐 `CurrentAC`(None/Grim/Matrix)、`GrimSelfCheck`、`GrimMultiPlace`、
  `AutoFlushPlaceQueue`、`AutoFlushPlaceBreakQueue`、`InventorySprintFix`、`InventoryWalkFix`、`ElytraInputFix`
- `MovExtra`：按原版拆成 `ToggleFlying`（切换鞘翅/创造飞行）+ 背包疾跑/行走修复
- `AttackEnhance`：设置名对齐原版——`AttackSelectBestWeapon`、`AutoAntiShield`、`MaceSwap`、
  `TryCriticalSprint`、`SwingHand`、`OnlyFocus`、`OnlyFront`，新增准星/身前过滤与暴击冲刺处理
- `TotemLog`：按原版改成 `LogTotalCount` + `LogDeath` + 可配置播报格式（`{name}` / `{count}`）
- `ElytraExtra`：补 `NoFallOnLanding`、`AutoTakeoffFireworksFromGround`

### 说明

原版依赖的 `LegalMovementManager`（逐轴速度裁剪）、`PacketManager`（发包队列）、
`ElytraBot` 的寻路状态机无法在独立插件里复现，这部分仍以本体事件 + 原版参数实现。

## 1.8.1 — 2026-11-26

对照上游 [SlimefunHelper](https://github.com/SlimefunHelperDev/SlimefunHelper)（1.21.11 分支）修正上一批模块。

### 修复

- `ElytraFlight`：不再每 tick 强行把视角压平（无输入时不动视角）；自动起飞要求下落距离 > 1.5、垂直速度 < -0.3 且不在水中
- `ElytraBot`：只有背包里有烟花时才自动起飞，避免滑翔后无法加速直接坠地
- `ElytraExtra`：速度上限只在烟花加速后的 30 tick 内生效（原来每 tick 裁剪会橡皮筋）；水中烟花加 10 tick 冷却并检查烟花是否存在
- `AttackEnhance`：最佳武器不再把锤子算进去（锤子只走 `MaceSwap`）；只在目标 6 格内换武器
- `MovExtra`：只有真的在移动/疾跑时才发包，避免背包操作时无意义刷包
- `Disabler`：多点重发改为当前帧末尾 `mc().execute` 发送（原版是 post-schedule 回调，不是下一 tick）
- `EnderEyeLog`：补上要塞三角定位——按飞行方向与 16 格区块对齐推算候选坐标，并支持只输出方向
- `TotemLog`：补上死亡播报（爆了 N 个图腾后死亡）

## 1.8.0 — 2026-11-26

### 新增

- `ElytraExtra`（鞘翅扩展）：进服自动起飞、水中烟花加速、蛛网水平限速、滑翔总速度上限

### 说明

原版 `ElytraExtra` 还有 auto-rescale（按原版滑翔物理逐轴裁剪速度向量）与 armor-fly 事务切换，
这两块依赖它自己的移动管线/背包事务状态机，没有移植；自动起飞、烟花、限速这类独立开关已覆盖。

## 1.7.0 — 2026-11-26

### 新增

Slimefun 第二批（用户点名）：

- `Disabler`（反作弊禁用器）：Grim 同 tick 多点放置重发、切换快捷栏强制刷新放置队列
- `AttackEnhance`（攻击增强）：攻击前切最佳武器、目标举盾时用斧、下落时切锤
- `MovExtra`（移动扩展）：背包操作前停疾跑并发送空输入包，规避 Grim 多动作检测
- `ElytraFlight`（鞘翅飞行）：俯仰角控制、自动起飞、速度不足自动放烟花
- `ElytraBot`（鞘翅追击）：追最近玩家，自动转向与烟花加速

### 说明

`ElytraExtra`（鞘翅扩展，83 KB）尚未移植：它是十几个鞘翅子功能的合集（自动缩放、动作模式、
俯仰覆盖等），与本体 `ElytraFly` 大面积重叠，需要单独拆解后再做。

## 1.6.0 — 2026-11-26

### 新增

从 Meteor 插件 **SlimefunMeteorAddon 3.0.20** 移植第一批功能：

- `TotemLog`（图腾日志）：记录图腾触发者、坐标与血量，可限定范围
- `NoQDrop`（防误丢弃）：拦截 Q 键丢弃数据包，可设成「仅潜行时允许」
- `EnderEyeLog`（末影之眼日志）：跟踪末影之眼实体并报告落点

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
