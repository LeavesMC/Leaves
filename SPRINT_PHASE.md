# Leaves 26.1.2 升级 — Sprint Phase（冲刺阶段）

> **更新时间**：2026-04-20（batch 38：Paper upstream `0c79f00b` → `66d3bbed`，跟进 5 个 commit，零代码改动）
> **文档角色**：这是当前状态的**唯一权威来源（single source of truth）**。
> 需要历史脉络看 [`UPGRADE_26.1.2_PROGRESS.md`](./UPGRADE_26.1.2_PROGRESS.md)，
> 需要操作细节看 [`PATCH_REBASE_PLAYBOOK.md`](./PATCH_REBASE_PLAYBOOK.md)，
> 需要了解**当前迁移的不完美之处和技术债**看 [`LIMITATIONS.md`](./LIMITATIONS.md)。

---

## 一、最新状态快照

| 指标 | 值 |
|---|---|
| 当前分支 | `upgrade-26.1`（HyacinthHaru/Leaves，开发/测试） + `master`（发布分支，不含开发文档） |
| Paper pin | `66d3bbed3878387649d4b9f72e78780e33b2d85d`（batch 38 升级到 upstream HEAD，新 5 commit：blockstate validation / compression-format 删除 / skip inactive AI / FrostedIce BlockFadeEvent） |
| 最新 push commit | `upgrade-26.1`: `7c760ad`（batch 38 0004 patch rebase）/ `master`: `d8b5f75`（batch 37+38 已同步） |
| `./gradlew applyAllPatches` | ✅ 通过（本地 + CI 双绿） |
| `./gradlew :leaves-server:createLeavesclipJar` | ✅ `leaves-leavesclip-26.1.2-R0.1-SNAPSHOT.jar` 62.5 MB |
| `./gradlew :leaves-server:compileJava` | ✅ **BUILD SUCCESSFUL（0 errors）** |
| GitHub Actions `Leaves Test CI` | ✅ 绿，artifact `leaves-26.1.2.jar` 可下载 |
| 已 rebase 的 patch 总数 | **171 / 171（100%）** |
| 剩余 minecraft patches | **0 个** ✅ |
| 剩余 paper patches | **0 个** ✅ |
| 已知的小瑕疵 | 见 §七（全部 🚨 阻塞项已清空） |

### 1.1 三个根目录的 patch 目录状态

| 目录 | 已 rebase | features-todo | obsoleted | 合计原 patch |
|---|---|---|---|---|
| `leaves-api/paper-patches/features/` | 9 | 0 | 0 | 9 |
| `leaves-server/paper-patches/files/` | 1 | 0 | 0 | 1 |
| `leaves-server/paper-patches/features/` | 16 | 0 | 0 | 16 |
| `leaves-server/minecraft-patches/features/` | 145（编号 0001–0145，无重复） | 0 | 5 | 147 |
| **总计** | **171** | **0** ✅ | **5** | **176** |

注：batch 34 做 Paper upstream 升级时自动修复了原来"两个 0035 编号"的瑕疵，重新导出后所有 patch 编号唯一。

---

## 二、所有 Leaves patch 已全部 rebase ✅

所有 features-todo（minecraft + paper）均已完成。阶段 3 全部结束，进入阶段 4（编译错误清零）。

### 已完成的 features-todo

| Batch | 原编号 | 新编号 | Patch | 行数 | 概要 |
|---|---|---|---|---|---|
| 27 | 0136 | **0142** | `Lithium-Sleeping-Block-Entity` | 2480 | 29 个 minecraft 文件，1634 行插入。全部 hunk 手动应用（git am 完全失败）。修改 NonNullList、PatchedDataComponentMap、ItemStack、LevelChunk、Level、ServerLevel、Entity、ItemEntity、BlockBehaviour、BlockEntity、BaseContainerBlockEntity、HopperBlockEntity 等核心及 11 个 BlockEntity 子类 + 7 个杂项文件。API 差异适配：`tickingBlockEntity`→`ticker`、`runsNormally`→`tickBlockEntities`、参数名映射、`final` 修饰符、indexed for-loop 等 |
| 28 | 0072 | **0143** | `Replay-Mod-API` | 537 | 13 个 minecraft 文件，196 行插入。核心改动：PlayerList 新增 `realPlayers`（CopyOnWriteArrayList）+ `placeNewPhotographer`/`removePhotographer` 方法。13 处 `getPlayers()` → `realPlayers`。EntitySelector 6 处 ServerPhotographer 跳过。修正 placeNewPhotographer 中旧 API：`getBoolean` → `get`、`RULE_DO_IMMEDIATE_RESPAWN` → `IMMEDIATE_RESPAWN`、`getWorld().getSendViewDistance()` → `FeatureHooks.getViewDistance()` |
| 29 | 0102 | **0144** | `Old-Block-remove-behaviour` | 808 | 33 个 minecraft 文件，416 行插入。为 30+ 种方块添加 `onRemove` 方法恢复 1.21.1- 的方块移除行为。核心改动：BlockBehaviour 新增 `onRemove` 基方法 + BlockStateBase 委托；LevelChunk.setBlockState 新增条件分支（`oldBlockRemoveBehaviour` 配置）；BlockEntityType.isValid 添加配置旁路。各方块类（AbstractFurnace、Barrel、Chest、Hopper、Dispenser 等容器类用 `dropContentsOnDestroy`，BasePressurePlate、Button、Lever、RedStoneWire 等红石类用信号更新，Piston/Observer/TripWire 等用各自特殊逻辑） |

---

## 三、paper-patch 状态 ✅

`0013-Leaves-Plugin` 已于 batch 30 完成（作为 `0016-Leaves-Plugin.patch` 进入 `paper-patches/features/`，commit `3482b3f`）。

**重要纠正**：batch 30 之前的文档说过"Paper 26.1 完全重构了 plugin provider 架构 → 需完全重写"——**此判断错误**。调研后发现：
- 原 patch 涉及的 7 个 Paper 文件中，6 个在 26.1 仍然存在且签名几乎没变
- 唯一真正被删除的是 `io.papermc.paper.pluginremap.PluginRemapper`（因为 26.1 全栈 Mojang-mapped 不再需要运行时 remap）
- 所以实际只是轻度 3-way merge + 删除 PluginRemapper hunk，实际耗时约 30 分钟（原估 4-8 小时）

**实际修改**：
- 7 个 Paper 文件的 3-way merge（PaperPluginsCommand、PluginInitializerManager、LegacyPaperMeta、PaperPluginMeta、PluginFileType、PaperPluginParent、SimpleProviderStorage、CraftServer）
- `PluginInitializerManager` 在 26.1 多了 `add-plugin-dir` 支持，手工合并（保留两者）
- **整块删除** 对 `PluginRemapper.java` 的 hunk
- 原作者 `MC_XiaoHei` 保留在 commit history 中

---

## 四、编译错误已清零 ✅

`./gradlew :leaves-server:compileJava` → **BUILD SUCCESSFUL（0 errors）** — 从 batch 31 起保持，经过 batch 32/33/34 多次 rebuild 仍然绿。

数字演变：`batch 29: 184` → `batch 30: 180` → `batch 31: 0`。batch 31 一次扫掉 180 个 Leaves 自有源码 API 迁移错误，见 §六 batch 31 摘要。

### 4.1 batch 31 清零了哪些错误

~30 个源文件被改，修复模式统计：

| 模式 | 修复量 | 例子 |
|---|---|---|
| `ResourceKey.location()` → `.identifier()` | ~15 处 | BBOR、PCA、Servux、REI 各处 dimension().location() |
| `ChunkPos.x/z` 字段私有 → 记录访问器 `x()`/`z()` | ~40 处 | LinearRegionFile (26)、ServuxStructuresProtocol (18)、SchematicPlacingUtils (10) |
| `new ChunkPos(long)` → `ChunkPos.unpack(long)` | 多处 | Servux |
| GameRule 常量 `RULE_*` 前缀去除 + `getBoolean` → `get` | 4 处 | ServerBot (`SHOW_DEATH_MESSAGES`、`FORGIVE_DEAD_PLAYERS`)、AppleSkin (`NATURAL_HEALTH_REGENERATION`)、ServuxHud (`ADVANCE_WEATHER`) |
| `ItemStack.getItemHolder()` → `.typeHolder()` | 2 处 | ItemRecipeFinder、EntryIngredient |
| `BlockState.getBlockHolder()` → `.typeHolder()` | 1 处 | FertilizableCoral |
| `Level.random` protected → `.getRandom()` | 4 处 | FertilizableCoral、HopperBlockEntity (minecraft patch 0142) |
| 包路径迁移：`boss.EnderDragonPart`→`boss.enderdragon.EnderDragonPart`、`critereon`→`criterion`、`horse`→`equine` | 3 处 | jade 的 CommonUtil/LootTable/ItemStorage |
| Entity.interact 签名加 Vec3 | 3 处 | ServerBot、ServerUseItemToAction |
| DyeItem.getDyeColor 移除 → DataComponents.DYE | 1 处 | HopperCounter |
| ServerStatsCounter File→Path | 1 处 | BotStatsCounter |
| recipe.assemble 不再接受 RegistryAccess | 4 处 | CookingDisplay、ShapedDisplay、ShapelessDisplay、StoneCuttingDisplay |
| SetTimePacket(long,long,bool) → (gameTime, Map<Holder<WorldClock>, ClockNetworkState>) | 1 处 | Recorder |
| Finder 重复文件 `* [0-9].java` | 32 处 | paper-server tracked 内 |
| 其他零散（MinecraftClient、ItemContainerContents.stream、repo 参数、setClientLoaded、BlockEntityType.getKey 等） | ~15 处 | util/*、profile/*、bot/* |
| 0142 patch 本身的 latent 错误 | 8 处 | `litTimeRemaining`、Blocks FQN、@Nullable 位置、world.getRandom() |
| **总计** | **≈ 180** | — |

### 4.2 已构建 jar 产物

`./gradlew :leaves-server:createLeavesclipJar` 成功，产物位于 `leaves-server/build/libs/`：

| 文件 | 大小 | 用途 |
|---|---|---|
| `leaves-leavesclip-26.1.2-R0.1-SNAPSHOT.jar` | **62.5 MB** | **推荐**：launcher，用户应该用这个 |
| `leaves-bundler-26.1.2-R0.1-SNAPSHOT.jar` | 101 MB | 全依赖打包版 |
| `leaves-server-26.1.2-R0.1-SNAPSHOT.jar` | 29 MB | 纯 server，缺 launcher |

GitHub Actions CI 也已通过，上传 `leaves-26.1.2.jar` artifact 可下载。

---

## 五、下一步路径图

```
[batch 27]  0136 Lithium-Sleeping-Block-Entity  ✅ 完成（→ 0142→0143，29 文件，1634 行）
[batch 28]  0072 Replay-Mod-API                  ✅ 完成（→ 0143→0144，13 文件，196 行）
[batch 29]  0102 Old-Block-remove-behaviour       ✅ 完成（→ 0144→0145，33 文件，416 行）
[batch 30]  0013 Leaves-Plugin (paper-patch)      ✅ 完成（→ 0016，8 文件，97 行；删除 PluginRemapper hunk）
    ↓
阶段 3 完成：所有 Leaves patch 已 rebase ✅

[batch 31]  180 个 API 迁移错误一次扫清  ✅ 完成（~30 文件）
    ↓
阶段 4 完成：compileJava 错误归零 ✅

[batch 32]  Bot 无敌修复 + CI 修复（task 改名+去 -mojmap）+ 4 个 patch 小修  ✅
[batch 33]  REI tipped-arrow / map-cloning filler 修复 + 深度调研文档       ✅
[batch 34]  Paper upstream 8987f91c → 02ec8e958 升级，重导所有 171 patch   ✅
    ↓
阶段 5 准备完成：jar 可构建、CI 绿、已修清所有 🚨 阻塞项

[下一步] 用户本地启动 TestServer 跑 STAGE5_TEST_CHECKLIST 剩余项（主要是 D/E/F 需客户端对接 mod）
```

注：batch 编号中原先的"0142"（Lithium-Sleeping）等已在 batch 34 重导出时集体 +1，见 §一 §1.1 注释。

---

## 六、批次历史（11 → 34，摘要）

前 10 个 batch 的详细记录见 [`SESSION_REPORT_2026-04-15.md`](./SESSION_REPORT_2026-04-15.md)。

| Batch | 日期 | 关键 patch | 关键适配 |
|---|---|---|---|
| 11 | 04-15 | 0061-0065 | `isHalloween` 移到 `SpecialDates`，drop `spooky-season` patch |
| 12 | 04-15 | 0066-0070 | `elytra-aeronautics`、`SlimeHelper` |
| 13 | 04-15 | 0071-0074 | `deathTime` / `dropAllEquipment` |
| 14 | 04-15 | 0075-0080 | Hopper 优化三件套起步 |
| 15 | 04-15 | 0081-0091 (-1 obsolete) | `Cache-climbing-check` obsolete；10 个小 patch |
| 16 | 04-15 | 0092-0101 | 各种超小 patch 批量推进 |
| 17 | 04-15 | 0102-0108 (-2 obsolete) | `Vanilla-Fluid-Pushing`、`TEMP-Merge-Paper-11831` obsolete |
| 18 | 04-15 | 0109-0115 | 继续小 patch |
| 19 | 04-15 | 0116-0118 | Hopper 三件套收尾 |
| 20 | 04-15 | 0119-0120 + 0035 恢复 | `grindstone-overstacking`；`end-void-rings` 改写为 moonrise hook |
| 21 | 04-16 | 0121 | Leaves-Protocol-Core（解锁协议栈） |
| 22 | 04-16 | 0122-0133 | Leaves-Utils、Catch-update-suppression-crash + 6 个 protocol patch |
| 23 | 04-16 | 0134-0136 | Async-keepalive、Optimize-noise、PCA/REI fixup |
| 24 | 04-16 | 0137-0138 | Leaves-Fakeplayer（26 文件）+ getGameProfile API 修复；**附带 Protocol Mod 审计** |
| 25 | 04-16 | 0139-0140 | More-Region-Format-Support + `@Nullable` 修复 |
| 26 | 04-16 | 0141 | Lithium-Equipment-Tracking |
| 27 | 04-16 | 0142 | Lithium-Sleeping-Block-Entity（2480 行，29 文件，最大单 patch）。全部 hunk 手动应用。适配 `ticker`/`tickBlockEntities` 命名、indexed for-loop、`final` 修饰符等 |
| 28 | 04-16 | 0143 | Replay-Mod-API（537 行，13 文件）。`realPlayers` CopyOnWriteArrayList、`placeNewPhotographer`/`removePhotographer`。修正 GameRules API（`getBoolean`→`get`、`RULE_*`→新常量名）、FeatureHooks view/sim distance |
| 29 | 04-16 | 0144 | Old-Block-remove-behaviour（808 行，33 文件）。30+ 种方块 `onRemove` 恢复 1.21.1- 移除行为。BlockBehaviour/BlockStateBase 基方法；LevelChunk 条件分支；BlockEntityType 配置旁路。容器类 `dropContentsOnDestroy`，红石类信号更新，Piston/Observer/TripWire 各自特殊逻辑 |
| 30 | 04-16 | paper 0016 | **Leaves-Plugin**（280→260 行，8 Paper 文件）。**阶段 3 完成**。实际远比预估简单：9 hunk 中 8 个 3-way merge 通过，仅 `PluginRemapper.java` hunk 整块删除（文件已从 26.1 删除）。`PluginInitializerManager` 需手工合并 `add-plugin-dir`（26.1 新增）与 `leavesPluginNames` 声明。作者 `MC_XiaoHei` 保留 |
| 31 | 04-17 | — (源码修复) | **阶段 4 完成，编译错误归零**。batch 29→30 的 184→180 之后，一次 batch 把 180 错误扫到 0。策略：主线程处理零散小目标，3 个并行 Agent 分别处理 (1) `.location()`→`.identifier()` 跨协议文件、(2) `LinearRegionFile` ChunkPos 访问器、(3) 0142 patch 的 latent 错误。~30 个源文件修改。见 §四 修复模式表 |
| 32 | 04-17 | commit `f598aa0` | **Bot 无敌修复 + CI 修复 + 4 个 patch 小修**。`ServerBotPacketListenerImpl.hasClientLoaded()` override 返回 true（解决 `isInvulnerableTo` 误判）；CI `test.yml` 更新 `createMojmapLeavesclipJar`→`createLeavesclipJar`、产物去 `-mojmap` 后缀；paper-server `0001-Build-changes.patch` 去除 28 个 Finder 重复文件（patch 从 1.3MB 缩到 99KB）；0143 typo + 泛型、0142 `@Nullable` 和 `ensureMapOwnership` 过度通知修复 |
| 33 | 04-17 | commit `27c74df` | **REI 合成缺失修复**。调研发现 Paper 26.1 的 `tipped_arrow` 变成 `ImbueRecipe`、`map_cloning` 变成 `TransmuteRecipe`（与 dye-shulker-box 共类但由 id 区分），**都不是** CustomRecipe。`REIServerProtocol` switch 加两个 case；`Display.java` 方法签名放宽。生成独立调研文档 [`REI_RECIPE_MIGRATION.md`](./REI_RECIPE_MIGRATION.md) |
| 34 | 04-17 | commit `556a6bd` | **Paper upstream `8987f91c` → `02ec8e958` 升级（build #7+）**。3 个 upstream commit 整合。手工解决 2 个冲突：(1) `Delete Timings` patch 在 `TimingHistory.java` 上游改行 vs Leaves 删整文件（用 `git rm` 确认删除）；(2) `Do not prevent block entity crash` 0053 的 `getWorld().getName()` → `MCUtil.getLevelName(...)` 上下文对齐。所有 171 个 patch (9 api + 16 paper + 145 mc + 1 files) 重导出；`git format-patch` 自动修复 §七 的"重复 0035 编号"瑕疵（原 0036 起全部 +1 重编号） |

---

## 六 bis、阶段 3 完结概览

**所有 176 个原 patch 去向**：171 rebased + 5 obsoleted = 齐。145 unique minecraft + 16 paper + 9 api + 1 files。

**阶段 3 用时**：起点 0/176 → batch 30 终点 171/176（阶段 3 结束）共跨 30 个 batch，2026-04-15 到 2026-04-16 两天完成（前 10 个 batch 见 SESSION_REPORT_2026-04-15.md，后 20 个 batch 见本文件 §六）。阶段 4（编译清零）batch 31 独立完成。阶段 5 准备在 batch 32-34 完成（bot 无敌修复、REI filler 修复、Paper upstream 升级）。

---

## 七、已知的小瑕疵（不阻塞，PR 前处理）

完整账本见 [`LIMITATIONS.md`](./LIMITATIONS.md)。以下是当前**未修复**的条目快照（🚨 阻塞级已全清空）：

| 问题 | 类别 | 处理建议 |
|---|---|---|
| 自己新增的 4 个 "Fix-" 补丁（`0122`、`0137` PCA/REI、`0139`、`0141`，编号为 batch 34 重导后的新编号）| PR 前决策 | batch 35 实验：squash 会破坏 blob-hash 连续性导致 applyAllPatches 级联失败，决定保留独立形态。详见 LIMITATIONS.md §3.1 |
| 残留 obsoleted config 字段（`LeavesConfig.java` 的 `vanillaFluidPushing`、`checkSpookySeasonOnceAnHour`、`cacheClimbCheck`）| ✅ batch 35 完成 | 加 `@Deprecated(forRemoval = true, since = "26.1.2")` + Javadoc 说明失效原因。下个大版本可删除 |
| Leaves 自有源码 2 处用 `world.getName()`（`ListCommand.java:69`、`LeavesServerConfigProvider.java:99`）| 可忽略 | Paper 26.1 仅加 `@ApiStatus.Obsolete` 软弃用警告，语义未变（用于显示/文件名而非身份）。无需修改；batch 36 核对 grep 后数量从"4 处"更正为 2 处 |
| `Paper-PluginMeta.authors` 从 private 改 protected 的 hack | PR 前决策 | 把改动提交给 Paper 上游做 public API，或改用 AT 机制 |
| 0143 `placeNewPhotographer` 的 name 覆盖可疑点 | 剩余"在位覆盖"运行时验证 | batch 36 已修"移除时殃及真实玩家"子问题（加 `==` 守卫）；"photographer 在位期间覆盖真人名字查找"仍存（原 Leaves 1.21.10 设计） |
| Recorder `forceDayTime` 语义偏移 | 运行时验证 | `ClockNetworkState.totalTicks` 的值域和旧 `dayTime()` 不一致，回放时时间戳可能错。需 Replay mod 客户端测 |
| Servux HUD `spawnChunkRadius` 字段被注释 | 运行时验证 | 1.21.9 删了 `RULE_SPAWN_CHUNK_RADIUS`。batch 35 已清理 TODO 注释，剩余决策待客户端 mod 测试后决定 |

---

## 八、接手者应该做的第一件事

```bash
cd /Users/haru/Desktop/LeavesMC/Leaves
git status                                    # 应该干净，在 upgrade-26.1
git log --oneline -5                          # 最新 commit 应该是 batch 36 (725f5a3)
./gradlew applyAllPatches --no-daemon         # ~30-60s，BUILD SUCCESSFUL
# Paper 已从 upstream Build-changes patch 里去掉 Finder 污染，Linux 上不必再清理
./gradlew :leaves-server:compileJava --no-daemon --console=plain 2>&1 | grep -c "error:"
                                              # 应该是 0 ✅
./gradlew :leaves-server:createLeavesclipJar --no-daemon
                                              # ~40-60s，产物 leaves-server/build/libs/leaves-leavesclip-*.jar 约 62.5MB
```

如果以上都绿，**阶段 5 剩余工作**：

1. **本地启动 TestServer 做功能测试** — 把 jar 拷到一个空目录，放 `eula.txt`，`java -jar`，按 [`STAGE5_TEST_CHECKLIST.md`](./STAGE5_TEST_CHECKLIST.md) 逐项打勾。已用静态日志测过的见 checklist 里的 `[x]` 标记
2. **客户端 mod 对接** — 用真实 Minecraft 客户端 + mod 连接服务器，按 [`PROTOCOL_MOD_AUDIT.md` §四](./PROTOCOL_MOD_AUDIT.md) 的验证清单走
3. **PR 前清理** — [`LIMITATIONS.md`](./LIMITATIONS.md) §三的技术债（Fix- 补丁折叠、obsoleted config 决策、authors hack 等）

如果数字对不上，**先**去读 [`PATCH_REBASE_PLAYBOOK.md` §九](./PATCH_REBASE_PLAYBOOK.md) 的协作约定（gradle task 名变化、Paper upstream 升级的冲突解决、git add -A 禁用、每次 push 前清理脚本）。

---

_本文档随每个 batch 更新。每完成一个 batch：_
_① 更新第一节 "最新状态快照"，_
_② 更新第二节剩余 patch 表，_
_③ 在第六节追加一行批次摘要。_
