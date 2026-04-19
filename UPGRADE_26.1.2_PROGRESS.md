# Leaves 升级至 Paper 26.1.2 — 进度与移交文档

> **更新时间**：2026-04-19（batch 37：Paper upstream `02ec8e958` → `0c79f00b`，跟进 5 commit 零冲突）
> **文档角色**：**历史脉络 + 背景知识**。当前状态以 [`SPRINT_PHASE.md`](./SPRINT_PHASE.md) 为准。
>
> 配套文档：
> - [`SPRINT_PHASE.md`](./SPRINT_PHASE.md) — 当前状态（single source of truth）
> - [`PATCH_REBASE_PLAYBOOK.md`](./PATCH_REBASE_PLAYBOOK.md) — 操作手册
> - [`PROTOCOL_MOD_AUDIT.md`](./PROTOCOL_MOD_AUDIT.md) — 协议 mod 兼容性审计
> - [`SESSION_REPORT_2026-04-15.md`](./SESSION_REPORT_2026-04-15.md) — batch 1-10 详细记录
>
> **本文档不再追踪即时状态**（每完成一个 batch 要改 N 处文字会失去同步）。只记录：
> - 升级目标、起点、终点
> - 各阶段里程碑完成情况
> - obsoleted patches 的完整账本
> - 贯穿整个升级的"关键发现"

---

## 一、升级目标

- **起点**：Leaves 1.21.10（Paper commit `af06383`），JDK 21，Spigot/reobf 映射体系
- **终点**：Leaves 26.1.2-R0.1-SNAPSHOT（基于 Paper commit `0c79f00b`，channel=ALPHA），JDK 25，Mojang 去混淆单 jar
  - 起点 pin 为 `8987f91c`（build #5），batch 34 跟到 `02ec8e958`，batch 37 跟到 `0c79f00b`（upstream HEAD）

---

## 二、阶段完成情况

### ✅ 阶段 1：leavesweight 升级（完成）

`HyacinthHaru/leavesweight@upgrade-26.1` commit `bd023b6`

| 项目 | 结果 |
|---|---|
| 删除 Spigot/reobf 相关文件 | 11 个 |
| 新增 upstream 新文件 | 26 个（checkstyle/patchroulette/DevBundleV7） |
| 同步修改文件 | ~40 个 |
| 保留 Leaves 定制 | Plugin ID、`PAPERCLIP_CONFIG="leavesclip"`、CreateLeavesclipJar、发布到 repo.leavesmc.org |
| `./gradlew build` | ✅ 通过 |
| `./gradlew publishToMavenLocal` | ✅ 通过（2.1.0-SNAPSHOT） |

### ✅ 阶段 2：Leaves 构建配置升级（完成）

`HyacinthHaru/Leaves@upgrade-26.1` commits `2880d30` + `f96cebd`

| 文件 | 改动 |
|---|---|
| `gradle.properties` | mcVersion 26.1.2, apiVersion 26.1.2, paperRef（起点 `8987f91c`，batch 34 升级到 `02ec8e958`），channel=ALPHA, version 26.1.2-R0.1-SNAPSHOT |
| `build.gradle.kts` | JDK 21 → 25，添加 `-Xlint:-deprecation -Xlint:-removal`，`-Xmaxerrs 500`（从默认 100 提升） |
| `settings.gradle.kts` | 添加 `mavenLocal()` 以使用本地 leavesweight 2.1.0-SNAPSHOT |
| `leaves-server/build.gradle.kts.patch` | 基于 Paper 26.1.2 重写：删除 spigot/reobf/fill，保留 leavesclip/linear/configurate-gson，移除冲突的 `org.lz4:lz4-java`（由 mache 的 `at.yawk.lz4:lz4-java` 提供）；`Xmaxerrs=500` |
| `leaves-server/paper-patches/files/.../PaperVersionFetcher.java.patch` | 签名更新（`fetchDistanceFromGitHub` 去掉了 `repo` 参数） |
| `leaves-api/build.gradle.kts.patch` | 基于 Paper 26.1.2 重写，保留 Leaves 所有定制 |

验证：`./gradlew applyPaperApiPatches applyPaperSingleFilePatches applyPaperServerFilePatches` 全部成功。

### ✅ 阶段 3：Patch rebase（完成，batch 30）

截至 batch 34（Paper upstream 升级后重导）：

| 类别 | 源数 | 已 rebase | 跳过 | dropped（upstream 已吸收） | 完成率 |
|---|---|---|---|---|---|
| `leaves-api/paper-patches/features/` | 9 | 9 | 0 | 0 | **100%** ✓ |
| `leaves-server/paper-patches/files/` | 1 | 1 | 0 | 0 | **100%** ✓ |
| `leaves-server/paper-patches/features/` | 16 | 16 | 0 | 0 | **100%** ✓ |
| `leaves-server/minecraft-patches/features/` | 147 | 145（0001–0145 编号唯一） | 0 | 5 | **100%** ✓ |
| **总计** | **173** | **171** | **0** | **5** | **100%** ✓ |

`./gradlew applyAllPatches` ✅ 通过（本地 + GitHub Actions CI 双绿）。

**阶段 3 关键 batch 摘要**：
- **batch 27**（0142/0143 Lithium-Sleeping-Block-Entity）：29 个 minecraft 文件，2480 行原始 patch，1634 行插入。全部 hunk 手动应用。
- **batch 28**（0143/0144 Replay-Mod-API）：13 个 minecraft 文件，537 行原始 patch，196 行插入。提供 `realPlayers` + `placeNewPhotographer`/`removePhotographer`。
- **batch 29**（0144/0145 Old-Block-remove-behaviour）：33 个 minecraft 文件，808 行原始 patch，416 行插入。30+ 种方块添加 `onRemove` 恢复 1.21.1- 移除行为。
- **batch 30**（paper 0016 Leaves-Plugin）：8 Paper 文件 3-way merge，仅删除 `PluginRemapper.java` hunk（Paper 26.1 已移除该类）。**阶段 3 完结**。

> 批次号的斜杠表示 batch 34 升级 Paper upstream 时重导致的编号偏移（原 0142 → 0143 等）。

### ✅ 阶段 4：编译错误清零（完成，batch 31）

一次 batch 把 180 个 "Leaves 自有源码 API 迁移" 错误扫到 0。`./gradlew :leaves-server:compileJava` BUILD SUCCESSFUL，经过 batch 32/33/34 多次重编仍绿。

### 🟢 阶段 5：jar 构建 & 运行时准备（进行中）

- ✅ `./gradlew :leaves-server:createLeavesclipJar` 成功（leavesclip 62.5MB、bundler 101MB、server 29MB）
- ✅ GitHub Actions `Leaves Test CI` 绿，artifact `leaves-26.1.2.jar` 自动上传
- ✅ Bot 无敌已修（[`LIMITATIONS.md`](./LIMITATIONS.md) §2.1）
- ✅ REI filler displays 已修（[`REI_RECIPE_MIGRATION.md`](./REI_RECIPE_MIGRATION.md)）
- ✅ Paper upstream 跟进到 HEAD（batch 34）
- 🟡 **待做**：本地 TestServer 启动 + mod 客户端对接测试（见 [`STAGE5_TEST_CHECKLIST.md`](./STAGE5_TEST_CHECKLIST.md)）

---

## 三、🗑️ Obsoleted patches 完整账本

这些 patch **不在 `features/` 也不在 `features-todo/`**，已从仓库彻底删除。对应的 Leaves 配置字段**暂时保留在 `LeavesConfig.java`**（为了兼容老的 `leaves.yml`），将在正式提交 PR 给 `LeavesMC/Leaves` 主分支前统一清理。

遇到新的 obsoleted patch 时，**直接追加一行**（不要放进 `features-todo/`，那里表示"待 rebase"）。

| 原 patch | 批次 | 废弃原因 | 残留 config 字段 |
|---|---|---|---|
| `Modify-end-void-rings-generation` | batch 4，**batch 20 已恢复** | Paper 26.1 把 `(long)` cast 放在 `ca.spottedleaf.moonrise.common.PlatformHooks.get().configFixMC159283()` 开关下，配置默认关闭时 Leaves 的 vanilla overflow 行为仍生效。batch 20 重写 patch 复用 moonrise 三元运算符，加入 `!LeavesConfig.fix.vanillaEndVoidRings &&` 守卫。**已恢复为普通 patch（当前 0035）** | `LeavesConfig.fix.vanillaEndVoidRings`（保留并仍生效） |
| `Fix-Paper-config-preventMovingIntoUnloadedChunks` | batch 8 | Leaves 作者 Lumine1909 将此修复贡献给了 Paper，26.1 已内置相同的 `flags` 逻辑 | 无 config 字段（纯 bug fix） |
| `Only-check-for-spooky-season-once-an-hour` | batch 11 | Paper 26.1 把 `isHalloween()` 移到 `net.minecraft.util.SpecialDates` 并把 15 天窗口简化为单天 (`MonthDay.of(10, 31)`) 检查，原 hot-path 优化不再必要 | `LeavesConfig.performance.checkSpookySeasonOnceAnHour`（LeavesConfig.java:821） |
| `Cache-climbing-check-for-activation` | batch 15 | Paper 26.1 在 `ActivationRange` 里改用 `living.blockPosition().equals(living.getLastClimbablePos().orElse(null))` 做 O(1) 快速路径，效果等价且更快；Leaves 的 `onClimableCached()` 优化不再必要 | `LeavesConfig.performance.cacheClimbCheck`（LeavesConfig.java:830） |
| `Vanilla-Fluid-Pushing` | batch 17 | Paper 26.1 用新的 `EntityFluidInteraction` 类（`fluidInteraction.update()` / `applyCurrentTo()`）完全重写了流体推送逻辑，旧 `updateFluidHeightAndDoFluidPushing` hook 点不存在 | `LeavesConfig.fix.vanillaFluidPushing`（LeavesConfig.java:1316） |
| `TEMP-Merge-Paper-11831` | batch 17 | Paper 26.1 已吸收该 PR 的大部分改动：`GiveCommand` 的 `displayName` 缓存、`Entity.dropItem`/`LivingEntity.drop` 的 `stack.setCount(0)` 复制逻辑都已合入 | 无（纯 temp-merge patch） |

**处理策略**：
- **现在**：保留相关 config 字段，对 3 个失效字段加 `@Deprecated(forRemoval = true, since = "26.1.2")`（batch 35）。`vanillaEndVoidRings` 仍生效，未标 deprecated
- **PR 前**：由 maintainer 决定是否真的删除（下个大版本 27.0+），或针对 `vanillaFluidPushing` 基于 `EntityFluidInteraction` 重写为新 patch

---

## 四、我们主动新增的 Fix 补丁（rebase 过程中加的）

这些不是 Leaves 原有 patch，而是 rebase 过程中**为了解决编译错误新开**的补丁。合并回上游时应该折叠进对应的原 patch。

注：batch 34 升级 Paper upstream 时重导出所有 patch，编号从原来的 `0122/0136/0138/0140` 整体 +1 到 `0122/0137/0139/0141`（因原"重复 0035"自动修复让后续 patch 向后挪一位）。

| Patch（batch 34 后编号）| 折叠目标 | 作用 |
|---|---|---|
| `0122-Fix-latent-compile-errors-in-rebased-patches.patch` | 分散到各原 patch | 一次性清理 15+ 个 Paper 26.1 API 改名（如 `EnderDragonPart` 包迁移、`GameRules.RAIDS`、`level.random` → `level.getRandom()` 等） |
| `0137-Fix-PCA-addListener-and-REI-display-API-mismatch.patch` | `0130-PCA-sync-protocol`、`0134-Support-REI-protocol` | `SimpleContainer.addListener()` 已删，改用匿名子类 `setChanged()` override；`ItemStackTemplate.display()` 已删，改用 `new SlotDisplay.ItemStackSlotDisplay` |
| `0139-Fix-Fakeplayer-getGameProfile-API.patch` | `0138-Leaves-Fakeplayer` | `Player.getGameProfile()` 方法在 26.1 不存在，改用 `gameProfile.name()` 公共字段 |
| `0141-Fix-Nullable-annotation-on-IRegionFile.patch` | `0140-More-Region-Format-Support` | jspecify 要求 `@Nullable` 标在最内层类型前（`Type.@Nullable InnerType` 形式） |

---

## 五、关键发现（贯穿整个升级）

### 5.1 Mojang 去混淆的副作用：类名 / 包名重命名

Paper 26.1 的最大变化不只是删除 Spigot 映射，**Mojang 也改了很多类的"官方名"**。

#### 类重命名

| 旧名 | 新名 | 涉及文件数（Leaves 自有） | 方式 |
|---|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | 82 | 类名 + 字段名全局替换 |
| `FriendlyByteBuf.writeResourceLocation` / `readResourceLocation` | `writeIdentifier` / `readIdentifier` | 6+ | 方法重命名 |
| `ResourceKey.location()` | `ResourceKey.identifier()` | 5 | 点位精确替换 |
| `net.minecraft.Util` | `net.minecraft.util.Util` | 5 | import 路径 |
| `TippedArrowRecipe` / `MapCloningRecipe` | `CustomRecipe`（泛型临时替代） | 少量 | **TODO**：recipe 系统改了，REI display 实际行为需重写 |

#### 包路径迁移（实体/游戏规则）

| 类 | 旧包 | 新包 |
|---|---|---|
| `ThrownEnderpearl` | `world.entity.projectile` | `world.entity.projectile.throwableitemprojectile` |
| `AbstractVillager` | `world.entity.npc` | `world.entity.npc.villager` |
| `AbstractBoat` | `world.entity.vehicle` | `world.entity.vehicle.boat` |
| `ZombieVillager` | `world.entity.monster` | `world.entity.monster.zombie` |
| `Chicken` | `world.entity.animal` | `world.entity.animal.chicken` |
| `GameRules` | `world.level` | `world.level.gamerules` |
| `EnderDragonPart` | `world.entity.boss` | `world.entity.boss.enderdragon` |
| `AbstractHorse` | `world.entity.animal.horse` | `world.entity.animal.equine` |
| `critereon` | `advancements.critereon` | `advancements.criterion` |

#### 方法参数 / 局部变量重命名

| Paper 1.21.10 | Paper 26.1 |
|---|---|
| `Entity.move(MoverType type, Vec3 movement)` | `Entity.move(MoverType moverType, Vec3 delta)` |
| `FireworkRocketItem.use(... ItemStack itemInHand ...)` | `... ItemStack itemStack ...` |
| `ServerPlayer.hurtServer(... DamageSource damageSource ...)` | `... DamageSource source ...` |
| `Entity#snapTo`（单一） | 拆成 `#snapTo` + `#absSnapTo`；chunk touch 只在 `absSnapTo` |
| `profilerFiller` | `profiler` |
| `level.random` | `level.getRandom()`（字段变 protected） |
| `context`（BlockItem 方法参数） | `placeContext` |
| `clickedPos`（ShovelItem） | `pos` |
| `blockState`（ShearsItem） | `state` |
| `shootable`（Player）| `heldWeapon` |
| `GameRules.RULE_DISABLE_RAIDS` + `!getBoolean()` | `GameRules.RAIDS` + `get()`（逻辑反转） |

### 5.2 Paper 插件系统重构

Paper 26.1 重构了插件 provider 架构。Leaves 的 "Leaves Plugin" patch（含自己的插件加载器、`PluginFileType` 注册、`PaperPluginMeta` 扩展）需要基于新 `PluginProvider` / `PluginProviderFactory` 架构**完全重写**。

已删除的类（必须替换）：
- `PluginInitializerManager.java`
- `LegacyPaperMeta.java`, `PaperPluginMeta.java`
- `PluginFileType.java`
- `PaperPluginParent.java`
- `SimpleProviderStorage.java`
- `PluginRemapper.java`

### 5.3 Paper 26.1.2 是 ALPHA

- channel=ALPHA，`updatingMinecraft=false`
- 构建号 #5（截至评估时）
- Paper 可能还会有破坏性变更
- Paper main 持续漂移，有些 patch 的 `index <SHA>..<SHA>` 行会因底层文件被改而无法 3-way merge（如 batch 20 的 0035 遇到 `DensityFunctions` moonrise 重写）

### 5.4 mache 依赖冲突（已解决）

mache 26.1.2+build.1 引入 `at.yawk.lz4:lz4-java:1.10.1`，与 Leaves 原 `org.lz4:lz4-java:1.8.0` 有 capability 冲突。已从 `leaves-server/build.gradle.kts.patch` 移除 Leaves 侧的 lz4-java 依赖。

### 5.5 .DS_Store 污染 patch（已解决，但需永久警戒）

在 macOS 上 paperweight 的内部 git 仓库会把 `.DS_Store` 的变化写进 patch。三个污染源：

1. **全局 gitignore**：`git config --global core.excludesfile ~/.gitignore_global`，含 `.DS_Store`
2. **paperweight 缓存**：`leaves-server/.gradle/caches/paperweight/taskCache/runLeavesSetup/` 内部 git 仓库已 track 了 `.DS_Store`，需要 `git rm` 清理
3. **运行时**：每次 push 前跑 `find paper-server leaves-server -name ".DS_Store" -delete; find paper-server leaves-server -name "* [0-9].java" -delete; find . -name "* [0-9].patch" -delete`

详细见 [`PATCH_REBASE_PLAYBOOK.md` § 九](./PATCH_REBASE_PLAYBOOK.md)。

### 5.6 `applyAllPatches` 会把工作区擦掉

每次 `./gradlew applyAllPatches` 都会**从头重建** `leaves-server/src/minecraft/java` 目录。这意味着：

- 手动改的文件如果没 `git format-patch` 保存回 `features/` 会丢失
- 正确工作流：改文件 → `git add <具体文件>` → `git am --continue` → `git format-patch -N ... -o /tmp/out/` → `cp /tmp/out/*.patch leaves-server/minecraft-patches/features/`
- **不要用** `./gradlew :leaves-server:rebuildMinecraftFeaturePatches`，它会先 reset 工作区重新 apply，刚手动修好的 patch 会被丢弃

### 5.7 macheRemapJar 不在 compileClasspath 是设计意图

`macheRemapJar`（unsigned-server.jar）包含编译的 vanilla classes，但**不**在 `compileJava` 的 classpath 上。原因：`leaves-server/src/minecraft/java` 目录在 `applyAllPatches` 后包含**所有 5256 个** vanilla 源文件，编译时直接从源码走。

这解决了早期"为什么找不到 `Entity.java`"的困惑 — 只要 `applyAllPatches` 成功，所有 vanilla 源都在。

### 5.8 javac `-Xmaxerrs` 默认只显示 100 条

`build.gradle.kts` 已加 `-Xmaxerrs 500`。这个**极其关键** — 否则真实错误数被截断，你会错以为"只剩 100 个错误要修"，实际可能还有几百个。

---

## 六、给接手者的工作指南

### 6.1 环境准备

- macOS/Linux，JDK 25 LTS，Gradle 9.2+
- `gh` 已登录 HyacinthHaru 账号（HTTPS token，不用 SSH）
- `git config --global core.excludesfile ~/.gitignore_global`（含 `.DS_Store`）
- leavesweight 2.1.0-SNAPSHOT 已 publishToMavenLocal

### 6.2 Checkout & 验证现状

见 [`SPRINT_PHASE.md` §八](./SPRINT_PHASE.md) "接手者应该做的第一件事"。

### 6.3 继续的推荐路径

当前阶段：阶段 3/4 已完成，阶段 5 进行中（jar 已构建、CI 绿、bot/REI/upstream 修复完成，剩运行时 mod 对接测试）。

每一步的顺序和优先级见 [`SPRINT_PHASE.md` §五](./SPRINT_PHASE.md) 的路径图。

### 6.4 提交约定（**已存入 auto memory**）

- commit author 保持 `HyacinthHaru <122684177+HyacinthHaru@users.noreply.github.com>`
- 推送用 `gh` 的 HTTPS token，不用 SSH（本地私钥 `~/.ssh/id_ed25519` 不在 HyacinthHaru 的 GitHub 绑定列表中）
- **不要在 commit message 里加 `Co-Authored-By: Claude ...`**
- 每 1-2 个 patch（或 1 个 batch）做一次 commit + push，commit message 格式：`Patch rebase batch NN: <patch 名> (XX→YY, A%→B%)`

---

## 七、文件索引

| 文件 | 作用 |
|---|---|
| `SPRINT_PHASE.md` | **当前状态**：权威的、实时的状态快照（single source of truth） |
| `UPGRADE_26.1.2_PROGRESS.md` | **本文档**：历史脉络 + 背景知识 + obsoleted 账本 |
| `PATCH_REBASE_PLAYBOOK.md` | **操作手册**：每个 patch 的标准工作流、Paper 26.1 适配速查表、Paper upstream 升级流程、协作约定 |
| `LIMITATIONS.md` | 当前迁移的不完美之处清单（已修/未修账本）|
| `STAGE5_TEST_CHECKLIST.md` | 阶段 5 运行时测试 checklist（12 个阶段 A-L） |
| `REI_RECIPE_MIGRATION.md` | REI 协议在 Paper 26.1 下 tipped_arrow/map_cloning recipe 迁移的深度调研（batch 33 产物） |
| `PROTOCOL_MOD_AUDIT.md` | 上游 protocol mod 兼容性审计（batch 24 完成，结论：无需返工） |
| `SESSION_REPORT_2026-04-15.md` | batch 1-10 详细 session 记录（历史快照） |
| `UPGRADE_TO_26.1.2.md` | 升级前的上游调研（起点研究） |
| `leaves-server/minecraft-patches/features/` | 145 个 minecraft patch（编号 0001-0145 唯一） |
| `leaves-server/minecraft-patches/features-todo/` | 空（0 个待办） |
| `leaves-server/paper-patches/features/` | 16 个 paper-server patch |
| `leaves-server/paper-patches/features-todo/` | 空（0 个待办） |
| `leaves-api/paper-patches/features/` | 9 个 paper-api patch |

---

## 八、关联仓库

| 仓库 | 分支 | 最新 commit |
|---|---|---|
| [HyacinthHaru/leavesweight](https://github.com/HyacinthHaru/leavesweight) | `upgrade-26.1` | `bd023b6` |
| [HyacinthHaru/Leaves](https://github.com/HyacinthHaru/Leaves) | `upgrade-26.1` (开发) + `master` (发布) | upgrade-26.1: batch 37（Paper upstream 升级）/ master: `ca8cbf1`（batch 36，尚未同步新 batch） |
| [PaperMC/Paper](https://github.com/PaperMC/Paper) | `main` (pinned to `0c79f00b`) | upstream（batch 37 跟进到 HEAD）|
| [PaperMC/paperweight](https://github.com/PaperMC/paperweight) | `main` | upstream (leavesweight rebase target) |

---

_本文档只在"非即时状态"改变时更新（如新增 obsoleted patch、阶段转换、发现新的 Paper 26.1 适配点）。即时 patch 进度见 `SPRINT_PHASE.md`。_
