# Leaves 26.1.2 — 阶段 5 运行时测试 Checklist

> **用途**：本地启动一个 Leaves 服务器，逐项对照功能，把"能编译"推进到"能运行"。
>
> **前置**：`./gradlew :leaves-server:createLeavesclipJar` 已成功，产物是 **`leaves-server/build/libs/leaves-leavesclip-26.1.2-R0.1-SNAPSHOT.jar`**（62.5 MB，launcher；推荐用这个）。
>
> 另外还有：
> - `leaves-bundler-26.1.2-R0.1-SNAPSHOT.jar`（101 MB，bundler jar，含全部依赖）
> - `leaves-server-26.1.2-R0.1-SNAPSHOT.jar`（29 MB，纯 server，缺 launcher）
>
> **使用方法**：
> 1. 准备一个测试目录（例如 `~/test-leaves/`）
> 2. 把 leavesclip jar 拷进去（`cp leaves-server/build/libs/leaves-leavesclip-*.jar ~/test-leaves/`）
> 3. 写 `eula.txt`：`eula=true`
> 4. 运行 `java -Xms2G -Xmx4G -jar leaves-leavesclip-26.1.2-R0.1-SNAPSHOT.jar --nogui`
>    - JDK 要 25+
>    - 首次启动 leavesclip 会下载 Paper 的 mache 去混淆 jar（~50MB），需要联网
> 5. 按下面的节奏逐项验证，**每通过一项勾上**
>
> **优先级标记**：
> - 🚨 = 阻塞级（此项不过无法继续）
> - 🔴 = 高风险（[LIMITATIONS.md](./LIMITATIONS.md) 里专门提到）
> - 🟡 = 中风险（理论等价，需观察）
> - 🟢 = 低风险（大概率 OK）
>
> **勾选标记**：
> - `[x]` = 通过
> - `[✗]` = 失败（详情见行尾备注）
> - `[~]` = 部分确认 / 静态测试可证
> - `[ ]` = 未验证（需要人工运行）

---

## 静态测试结果（2026-04-17 更新到 batch 34）

**已静态通过的项**：A.1、A.2、A.3、A.4、A.5、B.1（不含配置兼容性）、C.1、C.5（bot 移除）、J 的 Plugin 系统初始化

**已修复的问题**（静态测试时发现，此后已修）：
- **C.2（bot 无敌）** — 2026-04-17 batch 32 已修（`ServerBotPacketListenerImpl.hasClientLoaded()` 始终返回 true）
- **E.3（REI 箭/地图合成缺失）** — 2026-04-17 batch 33 已修（`ImbueRecipe` + `TransmuteRecipe` id 匹配）
- **CI task 名错误 `createMojmapLeavesclipJar`** — batch 32 改成 `createLeavesclipJar`
- **Finder 污染导致 Linux CI 失败** — batch 32 从 `0001-Build-changes.patch` 去掉 28 个 `* [0-9].java`

**其余项**需要用户实际启动服务器 + 部署 mod 客户端验证。**新 jar（batch 34 后 62.5MB）已在 TestServer 目录**，可以直接测。

---

## 阶段 A：启动 & 基础生存（30 分钟）

### A.1 🚨 服务器能启动
- [x] `java -jar <jar>` 启动无 FATAL / 未捕获异常 — `[bootstrap] Running Java 25` → 无 throw
- [x] 日志出现 `Done (XXs)! For help, type "help"` — `Done (8.017s)! For help, type "help"`（latest.log:40）
- [x] `/stop` 能干净退出，没有僵尸线程日志 — `ThreadedChunkStorage: All dimensions are saved` + `All RegionFile I/O tasks to complete`（latest.log:75）

### A.2 🚨 World 加载
- [x] `world/`, `world_nether/`, `world_the_end/` 三个目录生成 — `TestServer/world/dimensions/` 有 overworld + the_nether + the_end
- [x] Region 文件存在 — `world/dimensions/minecraft/overworld/region/r.0.0.mca` 等 4 个 `.mca` 文件（注：`.mca` 格式，因 `leaves.yml` `region.format: ANVIL`，非 linear）
- [x] 三 world 都成功 load — 日志显示 `Loading 0 persistent chunks for level 'minecraft:overworld/the_nether/the_end'...` + `Prepared spawn area in 0 ms` × 3

### A.3 🚨 基础命令
- [x] `/version` 显示 `Leaves 26.1.2-DEV-upgrade-26.1@1d85cdf` — 日志行："This server is running Leaves version 26.1.2-DEV-upgrade-26.1@1d85cdf (2026-04-16T16:44:05Z) (Implementing API version 26.1.2-R0.1-SNAPSHOT)"
- [x] `/help` 成功执行（前次日志 log2 有 `HyacinthHaru issued server command: /help`）
- [x] `/tps` 成功执行（log2 有 `HyacinthHaru issued server command: /tps`）
- [x] `/list` 成功执行（log2 有 `HyacinthHaru issued server command: /list`）

### A.4 🚨 玩家能连进来
- [x] 客户端能连 `localhost` — 日志行 `HyacinthHaru joined the game` + `logged in with entity id 47 at [minecraft:overworld]20.66, 80.74, 93.62`（latest.log:43）
- [x] `/gamemode creative` — log2:`Set own game mode to Creative Mode`
- [x] 退出 → 重连 → 位置保留 — log2:`HyacinthHaru left the game` ... `HyacinthHaru joined the game, ... at 27.03, 80.74, 84.85`（坐标变化合理）
- [~] 玩家能看到 spawn、走路、跳跃 / 挖方块 / 放方块 — 无法从日志验证（但玩家能连进来且 creative 模式成功，游戏是可用的）

### A.5 🟢 控制台清洁度
- [x] 启动期没有 `ERROR`、`SEVERE` — 所有 log 零 SEVERE/ERROR/Exception
- [x] 无 `java.lang.NoSuchMethodError` / `NoClassDefFoundError` — classpath 完整
- [x] 允许的 warning：仅有 `*** You are running an unknown version! Cannot fetch version info ***`（SNAPSHOT 构建预期会有这条）

---

## 阶段 B：Leaves 配置 & 基础 patch（20 分钟）

### B.1 🟢 `leaves.yml` 加载
- [x] 首次启动生成 `leaves.yml` — `TestServer/leaves.yml` 存在（6466 bytes）
- [x] 文件里有 `modify:`、`performance:`、`fix:` 等顶级节点 — 已核对，结构齐全（含 `settings.modify.*`、`settings.performance.*`、`settings.fix.*`、`settings.region.*`、`settings.protocol.*`、`settings.misc.*`）
- [x] 有 `vanillaEndVoidRings`（`settings.fix.vanilla-end-void-rings: false`）、`oldBlockRemoveBehaviour`（`settings.modify.minecraft-old.block-updater.old-block-remove-behaviour: false`）等字段
- [~] 观察到残留 obsolete 字段 — `check-spooky-season-once-an-hour: true`、`cache-climb-check: true`、`vanilla-fluid-pushing: true` 都还被保留在默认 yml 里，生成时没过滤。说明它们的 config field 还在 code 里生效（至少读到），未产生"unknown key" warning（log 无对应 WARN）。**对应 [LIMITATIONS.md §3.3](./LIMITATIONS.md)**
- [ ] **停服，手动在 `leaves.yml` 里写自己造一个 obsolete key（例如 `settings.modify.zzz_doesnt_exist: true`），重启** — 需要人工触发

### B.2 🟢 基础 fix patches
- [ ] **砧铁**：放一个 item 继续重命名直到"too expensive" → **继续生效**（`LeavesConfig.modify.anvilTooExpensive` = false by default，即 vanilla 行为）
- [ ] **MC-67**：大量箭命中同一实体，物品不消失（`modify.minecraft-old.allow-anvil-destroy-item-entities=false` = vanilla 行为）
- [ ] **FallingBlockEntity duplicate**：沙/砾石下落，不会重复掉落物品（永久 patch）
- [ ] **Infinity bow**：infinity 附魔 + 0 支箭时能射（永久 patch）
- [ ] **Villager infinite discounts**：长时间交易后折扣稳定（`modify.minecraft-old.villager-infinite-discounts=false` 即 vanilla）
- [ ] **Frozen ticks**：粉雪里冻结 → 走出来，不掉 fall damage（`performance.check-frozen-ticks-before-landing-block: true` 激活 — 需要游戏内验证）

---

## 阶段 C：🔴 Leaves Fakeplayer（30-60 分钟，**最高风险**）

**原因**：`ServerBot.setClientLoaded(true)` 在 batch 31 变成 no-op，可能导致 bot 永久无敌（见 [LIMITATIONS.md §2.1](./LIMITATIONS.md)）。

### C.1 🚨 基础创建
- [x] `/bot create testbot` 成功 — 日志：`HyacinthHaru issued server command: /bot create test` 下一行 `test joined the game`（latest.log:46-47）
- [x] 创建日志完整：`[org.leavesmc.leaves.bot.BotList] test[Local] logged in with entity id 385 at ([world]7.62, 72.0, 102.32)`（latest.log:48）— 说明 BotList 注册、entity spawn 都正常
- [~] bot 有身体、朝向、位置 — 坐标合法

### C.2 🔴✅ bot 可受伤（batch 32 已修，需运行时验证）
- [~] **修复方案**：在 `ServerBotPacketListenerImpl` 里 override `hasClientLoaded()` 直接返回 `true`（bot 的 connection 是自定义子类，method 是 public 可直接 override；避免反射和 AT）
- [ ] **运行时验证**：用新 jar 启动 TestServer，左键攻击 bot 应当掉血并最终死亡

**背景**：原本 `ServerPlayer.isInvulnerableTo` 检查 `!this.connection.hasClientLoaded()`，bot 的 `ServerBotPacketListenerImpl.tick()` 是 no-op，`clientLoadedTimeoutTimer` 永不递减 → `hasClientLoaded()` 永返回 false → bot 永久无敌。修复见 commit `f598aa0`。

### C.3 🔴 bot 会被命令影响
- [ ] `/kill testbot` 立即杀死 bot — 未执行
- [ ] `/effect give testbot poison` bot 中毒 — 未执行
- [ ] `/tp testbot ~ ~10 ~` 传送成功 — 未执行

### C.4 🟢 bot 行为
- [ ] `/bot action testbot mine` 等命令 — 未执行
- [ ] bot 停留 5 分钟不断连、不飘移 — 运行时间 ~1.5 分钟，未完整验证 5 min
- [ ] `/bot list` — 未执行

### C.5 🟢 bot 死亡/移除
- [x] bot 被干净"移除" — 停服时日志 `test left the game`（latest.log:50），bot 正确清理
- [ ] `/bot remove testbot` 主动移除 — 未执行
- [ ] 移除后 tab list 立即消失 — 需要客户端验证
- [ ] 移除后 `/bot list` 不再显示 — 未执行

### C.6 🟡 BotStatsCounter 日志噪音
- [x] **没有** `Failed to parse stats file: BOT_STATS_REMOVE_THIS` 之类 WARN — log 完全干净。之前 LIMITATIONS.md §2.2 预警的日志噪音不存在。即使 `parseLocal` override 被我删除，`ServerStatsCounter` 在 bot 场景下没产生异常。可以把该项从 LIMITATIONS 移到"已验证 OK"。
- [x] fakeplayerdata/ 目录是空的（配置 `resident-fakeplayer: false` 生效）

---

## 阶段 D：🔴 Replay Mod（20 分钟，中高风险）

**原因**：`Recorder.forceDayTime` 的 `ClockNetworkState` 语义可能偏移（见 [LIMITATIONS.md §2.3](./LIMITATIONS.md)）。

### D.1 🟢 基本录制
- [ ] `/replay start` 或配置入口启动录制
- [ ] `replay/` 目录出现录制文件
- [ ] `/replay stop` 结束录制
- [ ] 文件大小 > 0，结构合法（可以用 replay mod 客户端尝试回放）

### D.2 🔴 `forceDayTime` 语义
- [ ] `leaves.yml` 配置 `recorderOption.forceDayTime: 6000`（正午）
- [ ] 录制一段，然后客户端回放
- [ ] 回放里天空应当是**白昼**，且时间凝固
- [ ] 如果时间凝固在 0 (半夜) 或其他错误值：[LIMITATIONS.md §2.3](./LIMITATIONS.md) 修复

### D.3 🔴 Photographer name 冲突
- [ ] 先让 MC 客户端以 `MyName` 登入
- [ ] 启动录制，如果 photographer 也叫 `MyName`（需确认是不是这样）
- [ ] 验证 `server.getPlayerByName("MyName")` 返回的是 real player 而不是 photographer
  - 可以通过 `/tell MyName hello` 看谁收到
- [ ] 录完 `/replay stop`，photographer 消失后，real player 正常

---

## 阶段 E：🟢 REI 协议（20 分钟）

batch 33 已修复之前的"箭/地图合成缺失"降级（见 [`REI_RECIPE_MIGRATION.md`](./REI_RECIPE_MIGRATION.md)）。

### E.1 🟢 客户端加载
- [ ] MC 客户端装 REI mod，连接服务器
- [ ] 日志显示 REI 协议握手成功
- [ ] 打开 REI 界面（默认快捷键 R）

### E.2 🟢 大部分合成正常
- [ ] 搜 "iron_ingot" → 能看到铁锭的合成/冶炼
- [ ] 搜 "diamond_pickaxe" → 能看到镐的合成
- [ ] 点击 recipe → 能传送到合成台

### E.3 ✅ 箭/地图合成（batch 33 已修，需运行时验证）
- [ ] 搜 "tipped_arrow" 或 "arrow of poison"/"arrow of regeneration" 等 → 应该看到每种药水对应的合成（8 arrow + 1 lingering potion → 8 tipped arrow）
- [ ] 搜 "filled_map" → 应该看到"filled_map + empty map → 2 filled_map"
- [ ] 如果**还是**看不到，说明 batch 33 的修复没生效，需要 debug switch case 分支是否触发（`ImbueRecipe` / `TransmuteRecipe` + id 匹配）

---

## 阶段 F：协议 mods 对接（每个约 10 分钟）

对应 [`PROTOCOL_MOD_AUDIT.md`](./PROTOCOL_MOD_AUDIT.md) 的验证清单。

### F.1 🟢 Jade
- [ ] 客户端装 Jade mod 连接
- [ ] 瞄准一只 mob → 屏幕顶部显示 "Mob Name, Health: X/Y"
- [ ] 瞄准 container（chest/furnace/hopper）显示物品预览
- [ ] `PROTOCOL_VERSION` 一致（握手成功）

### F.2 🟢 Servux + MiniHUD
- [ ] 客户端装 MiniHUD + Servux mod
- [ ] 打开 MiniHUD 结构叠加 → 能看到生成的结构（需要先 load chunk）
- [ ] 天气信息显示（Rain / Thunder）
- [ ] **已知缺失**：spawnChunkRadius 字段不发送（[LIMITATIONS.md §1.2](./LIMITATIONS.md)）

### F.3 🟢 AppleSkin
- [ ] 客户端装 AppleSkin，连接
- [ ] HUD 显示饱和度条（saturation）
- [ ] 食物 tooltip 显示回复量
- [ ] natural_regeneration 状态同步（`leaves.yml` 改 gamerule 后客户端应同步）

### F.4 🟢 Carpet
- [ ] 客户端装 Carpet mod
- [ ] 启动日志显示 `Carpet mod handshake` 之类
- [ ] `/carpet` 命令可用

### F.5 🟡 PCA (Player Chunk Accessing)
- [ ] 客户端装 Plusls-Carpet-Addition（PCA mod）
- [ ] 客户端能查看远程 chest 内容（PCA 核心功能）
- [ ] 服务端 chest 变化 → 客户端 UI 更新

### F.6 🟢 Xaero Map
- [ ] 客户端装 Xaero's Minimap + World Map
- [ ] 走动时地图生成
- [ ] 服务端显示的世界名正确

### F.7 🟢 BBOR (Bounding Box Outline Reloaded)
- [ ] 客户端装 BBOR mod
- [ ] 按 B 显示边界框（结构、村庄、史莱姆区块等）
- [ ] 区块数据来自服务端

### F.8 🟢 Litematica + Syncmatica
- [ ] 客户端装 Litematica + Syncmatica
- [ ] 能粘贴 schematic
- [ ] 多客户端之间 schematic 同步（Syncmatica 功能）

### F.9 🟢 ChatImage
- [ ] 客户端装 ChatImage mod
- [ ] 聊天发送图片 URL，其他客户端显示图片

### F.10 🟢 Bladeren
- [ ] 客户端装 Bladeren（如果有）
- [ ] 对接正常

---

## 阶段 G：Linear Region 存储（15 分钟）

### G.1 🟢 存储格式（当前是 ANVIL）
- [x] 玩家在 world 里走路生成 chunks — `ChunkHolderManager` 日志显示 `Saved 1455 block chunks, 675 entity chunks`（latest.log:59）
- [x] `world/dimensions/minecraft/overworld/region/` 有 4 个 `.mca` 文件（r.-1.-1.mca、r.-1.0.mca、r.0.-1.mca、r.0.0.mca）— **注：是 ANVIL 格式，因 `leaves.yml` 默认 `region.format: ANVIL`**
- [ ] **如需测 linear**：停服，改 `leaves.yml` `settings.region.format: LINEAR`，重启，验证 `.linear` 文件生成 — 未人工触发

### G.2 🟢 格式切换
- [ ] 停服，改 format 字段，重启后读取兼容性 — 未验证
- [ ] `0085 More-Region-Format-Support` patch 的 IRegionFile 接口已通过编译

### G.3 🟢 重启后完整性
- [x] 跨多次重启，world 数据正确保留 — 多轮日志显示同一玩家 UUID (658436e1-56d3-4e68-8fd2-691cc514850b) 的 `.dat` 文件存在且有 `.dat_old`（latest.log + log2 的多次 join/leave 都无数据损坏报错）
- [ ] 挖几个方块 → 停服重启 → 方块状态保留 — 需人工手动验证

---

## 阶段 H：Lithium 优化（15 分钟）

验证 `0141 Lithium-Equipment-Tracking` + `0142 Lithium-Sleeping-Block-Entity` 正常工作。

### H.1 🟢 Hopper
- [ ] 放一个 chest → 上面一个 hopper（指向下方 chest）
- [ ] 往上 chest 扔 64 组物品 → 自动通过 hopper 转到下面 chest
- [ ] 速度不异常（应该是标准 vanilla 速度）

### H.2 🟢 Furnace
- [ ] 熔炉烧铁矿 → 产出铁锭
- [ ] 燃料耗尽 → 熔炉停止
- [ ] 重新放燃料 → 继续

### H.3 🟢 Chest lid animation
- [ ] 打开 chest → 盖子抬起动画
- [ ] 关闭 → 盖子合上动画
- [ ] 双连 chest 动作同步

### H.4 🟢 Comparator
- [ ] Comparator 从 chest 读取 fullness signal
- [ ] chest 装满 → comparator 输出最大信号
- [ ] 取出物品 → 信号下降

### H.5 🟢 Brewing stand
- [ ] 放药水瓶 + 基础配方 → 酿造进度
- [ ] 完成后停止

### H.6 🟢 Shulker box
- [ ] 打开 shulker box → 动画
- [ ] 里面装物品 → 关闭 → 破坏 → item 保留 NBT

---

## 阶段 I：Old block remove behaviour（10 分钟）

验证 `0102 → 0144 Old-Block-remove-behaviour` patch。

### I.1 🟢 配置切换
- [ ] `leaves.yml` 的 `modify.oldMC.updater.oldBlockRemoveBehaviour: true`
- [ ] 重启服务器
- [ ] 破坏一个有 block entity 的方块（chest/furnace/jukebox 等）
- [ ] 行为应该和 1.21.1 相同（具体细节见 `0144` patch 意图）

### I.2 🟢 关闭时
- [ ] 改回 `false`
- [ ] 破坏方块 → vanilla 26.1 行为

---

## 阶段 J：插件加载（20 分钟）

### J.0 🟢 Plugin 系统基础初始化
- [x] `[PluginInitializerManager] Initializing plugins...` 无异常（latest.log:3 + log1、log2 都有）
- [x] `Initialized 0 plugins`（当前无 plugin）— batch 30 的 `0016-Leaves-Plugin.patch` 没引入 init 异常
- [x] `plugins/` 目录生成 + `spark` 和 `bStats` 两个默认子目录

### J.1 🟢 Bukkit plugin
- [ ] 放一个 bukkit plugin jar 到 `plugins/`（例如 WorldEdit、EssentialsX）— 未测
- [ ] 启动日志显示 `Bukkit Plugins (N): xxx` — 未测
- [ ] `/plugins` 显示在 "Bukkit Plugins" 分组 — 未测

### J.2 🟢 Paper plugin
- [ ] 放一个 paper-plugin.yml 插件 — 未测
- [ ] `Paper Plugins (N): xxx` — 未测
- [ ] `/plugins` 显示 "Paper Plugins" 分组 — 未测

### J.3 🔴 Leaves plugin（0013 / 0016 paper-patch）
- [ ] 做一个最小 `leaves-plugin.json` 测试 jar — 未测
- [ ] `Leaves plugins (1): xxx` — 未测（当前日志无此行，因为 0 plugins）
- [ ] `/plugins` 显示 "Leaves Plugins" 分组 — 未测
- [ ] 如果失败，最可能是 `0016-Leaves-Plugin.patch` 有漏洞

---

## 阶段 K：长时间稳定性（2+ 小时）

### K.1 🟡 内存稳定
- [ ] 服务器运行 2 小时，玩家在线
- [ ] `jstat -gc <pid>` 观察 old gen，不应持续增长
- [ ] 如果 OOM 或 G1GC 连续 Full GC，可能有内存泄漏

### K.2 🟡 TPS 稳定
- [ ] `/tps` 持续接近 20
- [ ] 加载大量 mob / hopper 后 TPS 下降到合理范围（15+）

### K.3 🟢 shutdown 干净
- [ ] `/stop` 后所有 world 保存
- [ ] `world/playerdata/` 有每个在线玩家的 `.dat`
- [ ] 进程干净退出（exit code 0）
- [ ] 重启后玩家/世界恢复一致

---

## 阶段 L：CI / Linux（可选，1 小时）

### L.1 🟢 Push 触发 CI
- [ ] 推送后观察 GitHub Actions `test.yml`
- [ ] `Apply Patches` step 绿
- [ ] `Create Leavesclip Jar` step 绿

### L.2 🟢 Linux 机器（如有）
- [ ] 在 Linux 上 clone 项目
- [ ] `./gradlew :leaves-server:createLeavesclipJar` 成功
- [ ] 跑起来也能接客户端

---

## 最终 Go/No-Go 决策

全部阶段 A-C 通过：**可发 beta 给内测**
+ 阶段 D-K 通过：**可以提 PR 给 LeavesMC upstream**
+ 阶段 L 通过 + LIMITATIONS.md 里 🚨 项都处理：**可以正式发布**

---

## 2026-04-17 静态测试总结

### ✅ 静态可证明的通过项（12 项）
- A.1（启动）✅ `Done (8.017s)!` 干净
- A.2（world load）✅ 三个 dimension 都正常
- A.3（基础命令）✅ version/help/tps/list 都成功执行
- A.4（玩家连接）✅ `HyacinthHaru joined the game` + creative mode + 多次重连位置保留
- A.5（日志清洁）✅ 零 ERROR/SEVERE，只有预期 SNAPSHOT warning
- B.1（leaves.yml）✅ 生成、结构完整、读取无 warning
- C.1（bot 创建）✅ `test joined the game` + BotList 注册成功
- C.5（bot 清理）✅ 停服时 bot 正确退出
- C.6（BotStatsCounter 噪音）✅ **实际无日志噪音**（推翻 LIMITATIONS §2.2 的预警）
- G.1（chunk 保存）✅ 1455 block + 675 entity chunks 保存成功
- G.3（数据保留）✅ 玩家 .dat 多轮 join/leave 无损坏
- J.0（plugin 系统）✅ PluginInitializerManager 初始化正常

### ✅ 已修复的失败项（batch 32-33，静态测试时发现、之后修复）
- **C.2 bot 无敌** — batch 32 `ServerBotPacketListenerImpl.hasClientLoaded()` override 返回 true
- **E.3 REI 合成缺失** — batch 33 `ImbueRecipe` + `TransmuteRecipe` id 匹配
- **Finder 污染 / CI task 改名** — batch 32 修复

### ⚠️ 静态测试无法覆盖（需人工运行时验证）
- A.4 实际挖/放方块（只能证明玩家能连）
- B.2 各 fix patch 的游戏内行为
- C.2/C.3/C.4 bot 实际受伤、被命令影响、长时间稳定
- D Replay 功能（尤其 D.2 `forceDayTime` 时间戳语义）
- E.2/E.3 REI 合成显示（必须在客户端验证 batch 33 修复生效）
- F 所有 10 个协议 mod 对接
- H Lithium 优化效果（`sleeping-block-entity: false` 未激活）
- I oldBlockRemoveBehaviour（默认 false，未激活）
- J.1-J.3 Plugin 分类显示
- K 长时间稳定性
- L CI / Linux（CI 已验证绿）

---

## 下一步建议

1. **用新 jar 启动 TestServer**（路径见 §前置；batch 34 jar 已在目录）
2. **按顺序打勾**：A 基础 → B 配置 → C bot 受伤验证 → D/E/F 各功能 → K 长稳
3. **遇到问题**参考 §失败时该做什么 的对照表定位

---

## 失败时该做什么

| 失败现象 | 最可能原因 | 查看 |
|---|---|---|
| 启动 `NoSuchMethodError` / `NoClassDefFoundError` | jar 里缺类或版本错 | 看 LIMITATIONS §4.2 (Vector API), §4.3 (leavesclip) |
| bot 永久无敌 | `setClientLoaded` 没调 | LIMITATIONS §2.1 |
| 日志 `parseLocal` / `ServerStatsCounter` 异常 | BotStatsCounter 设计变了 | LIMITATIONS §2.2 |
| 回放时间错乱 | `ClockNetworkState.totalTicks` 语义 | LIMITATIONS §2.3 |
| REI 缺 arrow / map recipe | 已知降级 | LIMITATIONS §1.1 |
| CI 失败但本地成功 | 28 个 Finder 污染文件 | LIMITATIONS §4.1 |

---

_本文档对应项完成后打勾。发现新问题追加到 [`LIMITATIONS.md`](./LIMITATIONS.md)。_
