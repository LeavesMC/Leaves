# Session Report — 2026-04-15

> 按 [`PATCH_REBASE_PLAYBOOK.md`](./PATCH_REBASE_PLAYBOOK.md) 节奏推进的第一个记录型 session，最终跨越多个 batch 达到 **50% 里程碑**。

## 本次工作总览

| 阶段 | 完成 |
|---|---|
| 建立文档基础（Playbook） | ✅ `PATCH_REBASE_PLAYBOOK.md` |
| CI 修复（`.DS_Store` 污染） | ✅ `git rm --cached` + per-file `git add` 工作流修订 |
| 10 个 batch 的 patch rebase | ✅ 47 apply + 3 drop/defer = **50 个 patch 减少** |
| 类重命名批量修复（第一轮） | ✅ ~90 个 Leaves 自有文件 |
| **累计 patch 进度** | **87 / 173 (50%)** |

## 10 个 batch 明细

| Batch | Patch 编号范围 | 成功 | 失败/跳过 | 关键适配 |
|---|---|---|---|---|
| 1 | 0016-0020 | 5 | — | `item`→`dispensed`、`randomItems`→`items`、`AbstractArrow` 包迁移 |
| 2 | 0021-0025 | 5 | — | `vec3`→`distance`、`RULE_SPECTATORSGENERATECHUNKS`→`SPECTATORS_GENERATE_CHUNKS`、`isBlocked` 拆分 |
| 3 | 0026-0030 | 5 | — | `fromLevel`→`from`、`_boolean`→`mobGriefingEnabled`、`(long)` cast 引入 |
| 4 | 0031-0034 | 4 | 1 dropped | `Modify-end-void-rings-generation` 已被 Paper 26.1 内置修复 |
| 5 | 0035-0038 | 4 | 1 deferred | `CCE-update-suppression` 依赖尚未 rebase 的 Catch-update-suppression-crash |
| 6 | 0039-0043 | 5 | — | `flags & 1`→`Block.UPDATE_NEIGHBORS`、`FarmBlock`→`FarmlandBlock`、`attribute`→`speed` |
| 7 | 0044-0048 | 5 | — | `MinecartHopper` 包迁移、`itemInHand`→`itemStack`、`floor`→`fx` |
| 8 | 0049-0052 | 4 | 1 dropped | `Fix-Paper-config-preventMovingIntoUnloadedChunks` 已被 Paper 26.1 吸收（Leaves 曾贡献此修复） |
| 9 | 0053-0057 | 5 | — | `profilerFiller`→`profiler`、big rename in TripWireHookBlock (`blockStates`→`wireStates` 等) |
| 10 | 0058-0062 | 5 | — | lambda param rename `(key, value)`→`(k, count)` |

## 逐个 patch 清单（本次 session rebase 完成）

### 已 apply 到 `features/` (47 个)

0016 Make shears in dispenser can unlimited use
0017 Spectator dont get Advancement
0018 Disable check out-of-order command
0019 RNG Fishing
0020 Do not tick Arrow life regardless
0021 Disable distance check for UseItemOnPacket
0022 Despawn enderman with block
0023 Avoid anvil too expensive
0024 Old hopper suckin behavior
0025 Fix FallingBlockEntity Duplicate
0026 Configurable MC-67
0027 Old wet tnt explode behavior
0028 No TNT place update
0029 Allow anvil destroy item entities
0030 Fix chunk reload detector
0031 Old zombie reinforcement
0032 Old leash behavior when use firework
0033 Old projectile explosion behavior
0034 Bring back LivingEntity effect CME
0035 Skip cloning advancement criteria
0036 Old zombie piglin drop behavior
0037 Modify merge ItemEntity logic
0038 Movable Budding Amethyst
0039 Placing locked hopper no longer send NC updates
0040 Fix CraftPortalEvent logic
0041 Stick can change ArmorStand arm status
0042 No feather falling trample
0043 Check frozen ticks before landing block
0044 Fix minecraft hopper not work without player
0045 Bytebuf API
0046 Configurable item damage check
0047 Fix falling block's block location
0048 Add isShrink to EntityResurrectEvent
0049 Shared villager discounts
0050 Skip secondary POI sensor if absent
0051 Disable end gateway portal entity ticking
0052 Fix stacked container destroyed drop
0053 Do not prevent block entity and entity crash at LevelChunk
0054 Configurable MC-59471
0055 Skip negligible planar movement multiplication
0056 Fast exp orb absorb
0057 Force minecraft command
0058 Renewable Elytra
0059 Remove lambda from ticking guard
0060 Shave snow layers
0061 Renewable sponges
0062 Store mob counts in an array

### Dropped / obsoleted by upstream (2 个)

- `Modify-end-void-rings-generation` — Paper 26.1 重写了 `DensityFunctions` 里的 end island 生成代码，不再使用 `(long)` cast（Leaves 原修复已过时）
- `Fix-Paper-config-preventMovingIntoUnloadedChunks` — Leaves 作者（Lumine1909）曾将此修复提交到 Paper，26.1 已内置相同的 flags 逻辑

### Deferred (1 个)

- `CCE-update-suppression` — 依赖 `Catch-update-suppression-crash` patch 提供的 try/catch wrapper。留给后续 batch 处理。

## 关键流程发现（已写入 Playbook）

1. **⚠️ `rebuildMinecraftFeaturePatches` 不可用**：它依赖 `applyMinecraftFeaturePatches`，会先 reset 工作区重新 apply，刚手动 am 的 commit 会丢失。改用 `git format-patch -N --no-signature --zero-commit -o /tmp/out/` 从 git HEAD 导出。

2. **⚠️ `git add -A` 会污染 patch**：paperweight 内部 git 仓库初始就 track 了 `.DS_Store` 文件，`git add -A` 会把 macOS Finder 新增的 `.DS_Store` 一起 stage 进 commit，然后 `format-patch` 把 binary diff 导进 patch，CI 的 clean runner 上 `git am` 3-way merge 找不到对应 blob 直接 bail。**必须用 `git add <具体文件路径>`**。

3. **CI macOS 专属问题**：`paper-server/` 目录下会积累 macOS Finder 创建的 `* 2.java`/`* 3.java`/`* 2.patch` 重复文件。每次 apply 前 `find paper-server leaves-server -name "* [0-9].java" -delete; find . -name ".DS_Store" -delete`。

4. **"Repository lacks necessary blobs to fall back on 3-way merge" 是常态**：Paper 26.1 重写了大量文件的内容，git am 的 `index XXX..YYY` 行里的 blob hash 基本都不匹配当前状态。必须手动 apply 每个失败的 patch。

## 工作量实测

| 指标 | 数值 |
|---|---|
| 本次 session patch 数 | 50（47 applied + 3 dropped/deferred） |
| 总耗时（估计） | ~6-7 小时（含流程试错） |
| 平均单 patch 耗时 | ~7-8 分钟（超小 patch） |
| 最复杂的单 patch | `TripWireHookBlock`（7 个局部变量重命名） |
| 最简单的单 patch | `Spectator-dont-get-Advancement`、`Despawn-enderman-with-block`（直接 context 匹配） |

## 测试结果

### `./gradlew applyAllPatches`

✅ **本地 + CI 均绿**。所有 60 个当前 `features/` 下的 minecraft feature patches 可独立 apply。

### `./gradlew :leaves-server:compileJava`

❌ **仍 101 个 error**（javac `-Xmaxerrs` 上限）。

**关键观察**：错误数稳定在 101，但**错误内容**每 batch 都在变化：

- 本次 session 之初（43 个 patch 后）：占比最大的是 `realPlayers()` (7)、`elytraAeronauticsNoChunk` (4)、`spawnInvulnerableTime` (2)、`lithium$*` 等
- 本次 session 之末（87 个 patch 后）：上述 symbol 全部已解决。新暴露的错误来自更深的代码路径（`tellNeutralMobsThatIDied`、`placeNewPhotographer`、`getLeavesData`、`setClientLoaded` 等，都是尚未 rebase 的 patch 提供的）

**结论**：每 rebase 一个 patch 实际减少了错误，但上限没变，因为 javac 只显示最先遇到的 101 个。**只有把 ~80 个剩余 patch（至少覆盖 Entity.java、LivingEntity.java、PlayerList.java 相关）都搞定，错误数才会开始可见地下降**。

## 推荐的下一 session 起点

### 候选 batch 11（继续超小规模）

```
0061 Optimize-noise-generation (39 行)
0063 Reduce-lambda-and-Optional-allocation (35 行)
0064 Avoid-Class-isAssignableFrom-call-in-ClassInstanceMu (待查)
0065 Optimized-CubePointRange (待查)
0067 Skip-entity-move-if-movement-is-zero (待查)
```

### 中期：解锁 compile 错误的关键 patch

- `Leaves-Utils` (0003) — 最大影响：`Entity.getLeavesData()` 等
- `Leaves-Fakeplayer` (0007) — `ServerBot`、`BotList.realPlayers()`
- `Leaves-Protocol-Core` (0004) — 解锁后 BBOR/PCA/Jade/REI 等协议 patch
- `Replay-Mod-API` — `placeNewPhotographer`/`removePhotographer`

### 后期（硬骨头）

- `Leaves-Plugin` (paper-patches 0013) — 基于 Paper 26.1 新 PluginProvider 架构完全重写
- `Async-keepalive` — 依赖当前 Paper 最新网络层状态

## 进度

- **累计 patch rebase**：40 → **87** / 173 (+47，累计 **50%**，达到半程里程碑 🎉)
- **Minecraft feature patches**：15 → **62** / 147 (+47, 42%)
- **已 drop（obsoleted）**：2 个（patches 已被 Paper 26.1 吸收或内置修复）
- **Deferred**：1 个（等依赖 patch）
- **`applyAllPatches`**：✅ 持续绿
- **`compileJava`**：❌ 停在 101 error（javac cap），但实际 symbol 缺失持续减少

## Commits 本次 session 推送

- `0cb3ff0` — chore: remove macOS Finder 'src 2' leftover
- `4eaea4b` — batch 1（初版，有 DS_Store 污染）
- `f4a6048` — docs: warn against `git add -A`
- `d379bc9` — fix: strip .DS_Store pollution from 5 rebased patches (CI fix)
- `52bd964` — batch 2
- `6ac43c3` — batch 3
- `7336c8b` — batch 4 (+1 dropped)
- `2d7f764` — batch 5 (+1 deferred)
- `c8d0878` — batch 6
- `a240faa` — batch 7
- `81c0782` — batch 8 (+1 dropped)
- `8741be6` — batch 9
- `c9ec8c3` — batch 10 **(50% milestone)**

---

_session 结束于 commit `c9ec8c3`_
