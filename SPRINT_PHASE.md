# Leaves 26.1.2 升级 — Sprint Phase（冲刺阶段）

> **更新时间**：2026-04-16（batch 30 完成，所有 patch 已 rebase，阶段 3 完成 🎉）
> **文档角色**：这是当前状态的**唯一权威来源（single source of truth）**。
> 需要历史脉络看 [`UPGRADE_26.1.2_PROGRESS.md`](./UPGRADE_26.1.2_PROGRESS.md)，
> 需要操作细节看 [`PATCH_REBASE_PLAYBOOK.md`](./PATCH_REBASE_PLAYBOOK.md)。

---

## 一、最新状态快照

| 指标 | 值 |
|---|---|
| 当前分支 | `upgrade-26.1`（HyacinthHaru/Leaves） |
| 上次 push commit | `a8aefc4`（docs: Sprint Phase 更新） |
| 最近一次 patch commit | TBD（batch 30: 0013 Leaves-Plugin） |
| `./gradlew applyAllPatches` | ✅ 通过 |
| `./gradlew :leaves-server:compileJava` | ❌ **180 个 error**（详见四、五节，已比 batch 29 减少 4 个） |
| 已 rebase 的 patch 总数 | **170 / 173（98%）** |
| 剩余 minecraft patches | **0 个** ✅ |
| 剩余 paper patch | **0 个** ✅（0013 Leaves-Plugin 已完成，比预估的 4-8 小时大大提前） |
| 已知的 patch 目录瑕疵 | 见 §七 |

### 1.1 三个根目录的 patch 目录状态

| 目录 | 已 rebase | features-todo | obsoleted | 合计原 patch |
|---|---|---|---|---|
| `leaves-api/paper-patches/features/` | 9 | 0 | 0 | 9 |
| `leaves-server/paper-patches/files/` | 1 | 0 | 0 | 1 |
| `leaves-server/paper-patches/features/` | 16 | 0 | 0 | 16 |
| `leaves-server/minecraft-patches/features/` | 144（+1 重复编号 = 145 个文件） | 0 | 5 | 147 |
| **总计** | **170** | **0** ✅ | **5** | **173** |

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

**0013-Leaves-Plugin 已于 batch 30 完成**（作为 `0016-Leaves-Plugin.patch` 进入 `paper-patches/features/`）。

**重要纠正**：batch 29 之前文档里说"Paper 26.1 完全重构了 plugin provider 架构 → 需完全重写"——**此判断错误**。调研后发现：
- 原 patch 涉及的 7 个 Paper 文件中，6 个在 26.1 仍然存在且签名几乎没变
- 唯一真正被删除的是 `io.papermc.paper.pluginremap.PluginRemapper`（因为 26.1 全栈 Mojang-mapped 不再需要运行时 remap）
- 所以实际只是轻度 3-way merge + 删除 PluginRemapper hunk，实际耗时约 30 分钟（原估 4-8 小时）

**实际修改**（见 commit `TBD`）：
- 7 个 Paper 文件的 3-way merge（PaperPluginsCommand、PluginInitializerManager、LegacyPaperMeta、PaperPluginMeta、PluginFileType、PaperPluginParent、SimpleProviderStorage、CraftServer）
- `PluginInitializerManager` 在 26.1 多了 `add-plugin-dir` 支持，手工合并（保留两者）
- **整块删除** 对 `PluginRemapper.java` 的 hunk
- 原作者 `MC_XiaoHei` 保留在 commit history 中

剩余编译错误不再来自 patch 依赖，全部属于"Leaves 自有源码 API 迁移"类。

---

## 四、当前 180 个编译错误分布

只来自 `./gradlew :leaves-server:compileJava`（核心源码编译）。batch 29 的 184 → batch 30 的 180（-4，是 `plugin/provider/` 4 个错误随 0016-Leaves-Plugin 完成而消失）。

从此所有错误都属于"Leaves 自有源码 API 迁移"类，不再依赖任何未 rebase 的 patch。

| 分类 | 错误数 | 来源 patch | 何时消除 |
|---|---|---|---|
| `org.leavesmc.leaves.protocol.servux/` | ~42 | 多为 API 重命名（`ResourceLocation` → `Identifier` 等） | 第二轮 Leaves 自有源码修复 |
| `org.leavesmc.leaves.lithium/` | ~25 | 依赖 lithium 内部 API 适配 | 第二轮源码修复（大部分是 API 迁移类） |
| `org.leavesmc.leaves.protocol.jade/` | ~18 | API 重命名 | 第二轮源码修复 |
| `org.leavesmc.leaves.protocol.rei/` | ~20 | API 重命名（含 `SmithingRecipe` 新 API） | 第二轮源码修复 |
| `org.leavesmc.leaves.region.linear/` | ~26 | 已部分解锁（0139 More-Region-Format 提供 `IRegionFile`）；剩余为 `ChunkPos.x/z` 变 private + `RegionFile` 内部 API 迁移 | 第二轮源码修复 |
| `org.leavesmc.leaves.protocol/` (PCA, BBOR, AppleSkin 等) | ~11 | API 重命名 | 第二轮源码修复 |
| `paper-server/.../craftbukkit/` | ~12 | bukkit 侧 API 迁移 | 第二轮源码修复 |
| `org.leavesmc.leaves.replay/` | ~2 | Recorder API + CraftPhotographer 缺失方法 | 第二轮源码修复 |
| `LeavesMinecraftSessionService.java` | 1 | `YggdrasilMinecraftSessionService.client` 变 private | 第二轮源码修复 |
| `ListCommand.java` | 1 | `ResourceKey<Level>.identifier()` 重命名漏网 | 第二轮源码修复 |
| 其他零散 | ~22 | 逐个修复 | 第二轮源码修复 |

### 4.1 进入阶段 4

所有 patch 已 rebase，接下来 180 个错误都是"Leaves 自有源码 API 迁移"类（`ResourceLocation` → `Identifier`、`SmithingRecipe` 新签名、`ChunkPos.x/z` 变 private 等）。需要在 `leaves-server/src/main/java/org/leavesmc/leaves/` 和 `paper-server/src/main/java/` 里人工扫一轮。

---

## 五、下一步路径图

```
[batch 27]  0136 Lithium-Sleeping-Block-Entity  ✅ 完成（→ 0142，29 文件，1634 行）
[batch 28]  0072 Replay-Mod-API                  ✅ 完成（→ 0143，13 文件，196 行）
[batch 29]  0102 Old-Block-remove-behaviour       ✅ 完成（→ 0144，33 文件，416 行）
[batch 30]  0013 Leaves-Plugin (paper-patch)      ✅ 完成（→ 0016，8 文件，97 行；删除 PluginRemapper hunk）
    ↓
阶段 3 完成：所有 Leaves patch 已 rebase ✅

[batch 31+] 第二轮 Leaves 自有源码修复（~180 个编译错误）
    ↓
阶段 4：compileJava 错误数归零
    ↓
阶段 5：./gradlew createMojmapLeavesclipJar 成功 → runtime 启动测试
```

---

## 六、批次历史（11 → 29，摘要）

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

---

## 六 bis、阶段 3 完结概览

**所有 173 个 patch**：169 rebased + 5 obsoleted = 174 文件（含重复 0035）✓

**阶段 3 用时**：从起点 patch 0 → 终点 170/173（98%）共跨 30 个 batch，2026-04-15 到 2026-04-16 两天完成（前 10 个 batch 见 SESSION_REPORT_2026-04-15.md，后 20 个 batch 见本文件 §六）。

---

## 七、已知的小瑕疵（不阻塞但需登记）

| 问题 | 表现 | 处理建议 |
|---|---|---|
| `features/` 有两个 patch 编号都是 `0035` | `0035-Modify-end-void-rings-generation.patch` 与 `0035-Skip-cloning-advancement-criteria.patch` 并存 | `applyAllPatches` 按字母序执行未报错。清理时机：阶段 3 完全结束、准备 push 上游前，统一重编号（把后者改成最大编号 +1，或相反） |
| 自己新增的 4 个 "Fix-" 补丁 | `0122`、`0136`（PCA/REI）、`0138`、`0140` 四个都是我们 rebase 过程中加的适配 fixup | 合并回上游时应该折叠进各自对应的原 patch（例如 `0138-Fix-Fakeplayer-getGameProfile` 要合进 `0137-Leaves-Fakeplayer`） |
| 残留 obsoleted config 字段 | `LeavesConfig.java` 还保留 `vanillaEndVoidRings`、`checkSpookySeasonOnceAnHour`、`cacheClimbCheck`、`vanillaFluidPushing` 等 | 保留；等 Leaves maintainer 决定统一清理或重写成新 patch |
| 0142/0143/0144 保留原 Leaves patch 里的笔误/不规范（rebase 忠实复刻） | 0143 有 `// Leaves stop - replay mod api` typo（L511）、`new CopyOnWriteArrayList()` 缺泛型；0142 `Level.lithium$getLoadedExistingBlockEntity` 缺 `@Nullable` 注解；`PatchedDataComponentMap.ensureMapOwnership` 每次都 notify（忽略 `copyOnWrite`） | 不阻塞。合并回上游时可以顺手清理 |
| 0143 `placeNewPhotographer` 语义可疑点 | photographer 会被加到 `playersByName`/`playersByUUID` 可能覆盖同名 real player；且会踢掉同名 bot | 原 Leaves 1.21.10 设计，rebase 未改。运行时验证阶段（阶段 5）需要观察 |

---

## 八、接手者应该做的第一件事

```bash
cd /Users/haru/Desktop/LeavesMC/Leaves
git status                                    # 应该干净，在 upgrade-26.1
git log --oneline -5                          # 确认最新 commit 是 batch 30
./gradlew applyAllPatches --no-daemon         # 应该 BUILD SUCCESSFUL
./gradlew :leaves-server:compileJava \
    --no-daemon --console=plain 2>&1 | \
    grep -c "error:"                          # 应该看到 180（阶段 4 起点）
```

如果上面都 OK，所有 patch 已完成，下一步是**阶段 4**：集中修复 Leaves 自有源码 180 个编译错误（第二轮 API 迁移）。建议按 §四 表格分块修，每块 1 个 commit。

如果数字对不上，**先**去读 [`PATCH_REBASE_PLAYBOOK.md` § 九](./PATCH_REBASE_PLAYBOOK.md) 的协作约定（DS_Store 污染、git add -A 禁用、每次 push 前清理脚本），再去查 `git log` 对比我们最后一个 commit。

---

_本文档随每个 batch 更新。每完成一个 batch：_
_① 更新第一节 "最新状态快照"，_
_② 更新第二节剩余 patch 表，_
_③ 在第六节追加一行批次摘要。_
