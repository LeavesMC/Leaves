# Leaves 升级至 Paper 26.1.2 — 进度与移交文档

> **更新时间**：2026-04-15（batch 20 完成）
> **当前分支**：`upgrade-26.1` (HyacinthHaru/Leaves + HyacinthHaru/leavesweight)
> **当前状态**：
> - 阶段 1、2 完成
> - 阶段 3 达到 **85% 里程碑**：147/173 patch 已 rebase（含 batch 20+21 的 0096+0004），5 个 obsolete，1 个 deferred
> - **Hopper 三件套** + grindstone overstacking 全部啃完
> - 90 个 Leaves 自有 Java 文件的"类重命名/包迁移"第一轮修复完成
> - CI：`applyAllPatches` 持续绿；`compileJava` 100 errors（与 batch 19 持平，0120 未引入新错误）
> - 详见 [`SESSION_REPORT_2026-04-15.md`](./SESSION_REPORT_2026-04-15.md)

---

## 一、升级目标

- **起点**：Leaves 1.21.10（Paper commit `af06383`），JDK 21，Spigot/reobf 映射体系
- **终点**：Leaves 26.1.2-R0.1-SNAPSHOT（基于 Paper commit `8987f91c`，channel=ALPHA），JDK 25，去混淆单 jar

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
| `gradle.properties` | mcVersion 26.1.2, paperRef `8987f91c`, channel=ALPHA, version 26.1.2-R0.1-SNAPSHOT |
| `build.gradle.kts` | JDK 21 → 25，添加 `-Xlint:-deprecation -Xlint:-removal` |
| `settings.gradle.kts` | 添加 `mavenLocal()` 以使用本地 leavesweight 2.1.0-SNAPSHOT |
| `leaves-server/build.gradle.kts.patch` | 基于 Paper 26.1.2 重写：删除 spigot/reobf/fill，保留 leavesclip/linear/configurate-gson，移除冲突的 `org.lz4:lz4-java`（由 mache 的 `at.yawk.lz4:lz4-java` 提供） |
| `leaves-server/paper-patches/files/.../PaperVersionFetcher.java.patch` | 签名更新（`fetchDistanceFromGitHub` 去掉了 `repo` 参数） |
| `leaves-api/build.gradle.kts.patch` | 基于 Paper 26.1.2 重写，保留 Leaves 所有定制 |

验证：`./gradlew applyPaperApiPatches applyPaperSingleFilePatches applyPaperServerFilePatches` 全部成功。

### 🟡 阶段 3：Patch rebase（部分完成）

#### 3.1 成果统计

| 类别 | 源数 | 已 rebase | 跳过（待办） | dropped（upstream已吸收） | 完成率 |
|---|---|---|---|---|---|
| `leaves-api/paper-patches/features/` | 9 | 9 | 0 | 0 | 100% |
| `leaves-server/paper-patches/features/` | 16 | 15 | 1 (Leaves Plugin) | 0 | 94% |
| `leaves-server/minecraft-patches/features/` | 147 | 122 | 20 | 5 | 83% |
| `leaves-server/paper-patches/files/` | 1 | 1 | 0 | 0 | 100% |
| **总计** | **173** | **147** | **21** | **5** | **85%** |

`./gradlew applyAllPatches` 在当前 patch 集合下 **完整通过**，没有冲突。

---

## 🗑️ Obsoleted patches（被 Paper 上游吸收 / 过时）

这些 patch **不在 `features/` 也不在 `features-todo/`**，已从仓库彻底删除。对应的 Leaves 配置字段**暂时保留在 `LeavesConfig.java`**（为了兼容老的 `leaves.yml`），将在正式提交 PR 给 `LeavesMC/Leaves` 主分支前统一清理。

遇到新的 obsoleted patch 时，请按同样方式在此表追加一行。

| 原 patch | 废弃原因 | 残留 config 字段 | 处理时机 |
|---|---|---|---|
| ~~`Modify-end-void-rings-generation`~~ (batch 4，**batch 20 已恢复**) | ~~Paper 26.1 已修复~~ — 实际上 Paper 只是把 `(long)` cast 放在 `ca.spottedleaf.moonrise.common.PlatformHooks.get().configFixMC159283()` 开关下，配置默认关闭时 Leaves 的 vanilla overflow 行为仍生效。batch 20 重写 patch 复用 moonrise 三元运算符，加入 `!LeavesConfig.fix.vanillaEndVoidRings &&` 守卫 | `LeavesConfig.fix.vanillaEndVoidRings`（保留并仍生效） | — |
| `Fix-Paper-config-preventMovingIntoUnloadedChunks` (batch 8) | Leaves 作者 Lumine1909 将此修复贡献给了 Paper，26.1 已内置相同的 `flags` 逻辑 | 无 config 字段（纯 bug fix） | — |
| `Only-check-for-spooky-season-once-an-hour` (batch 11) | Paper 26.1 把 `isHalloween()` 移到 `net.minecraft.util.SpecialDates` 并把 15 天窗口简化为单天 (`MonthDay.of(10, 31)`) 检查，原 hot-path 优化不再必要 | `LeavesConfig.performance.checkSpookySeasonOnceAnHour`（LeavesConfig.java:821） | 提交 PR 前清理；如想对 `SpecialDates.dayNow()` 做缓存，新开 patch |
| `Cache-climbing-check-for-activation` (batch 15) | Paper 26.1 在 `ActivationRange` 里改用 `living.blockPosition().equals(living.getLastClimbablePos().orElse(null))` 做 O(1) 快速路径，效果等价且更快；Leaves 的 `onClimableCached()` 优化不再必要 | `LeavesConfig.performance.cacheClimbCheck`（LeavesConfig.java:830） | 提交 PR 前清理 |
| `Vanilla-Fluid-Pushing` (batch 17) | Paper 26.1 用新的 `EntityFluidInteraction` 类（`fluidInteraction.update()` / `applyCurrentTo()`）完全重写了流体推送逻辑，旧 `updateFluidHeightAndDoFluidPushing` hook 点不存在。如需保留 vanilla 模式需按新 API 重写 | `LeavesConfig.fix.vanillaFluidPushing`（LeavesConfig.java:1316） | 提交 PR 前清理；或基于 `EntityFluidInteraction` 新开 patch |
| `TEMP-Merge-Paper-11831` (batch 17) | Paper 26.1 已吸收该 PR 的大部分改动：`GiveCommand` 的 `displayName` 缓存、`Entity.dropItem`/`LivingEntity.drop` 的 `stack.setCount(0)` 复制逻辑都已合入；剩余 `AbstractContainerMenu` 的 SPIGOT-8010 break-loop 上下文已重构 | 无（纯 temp-merge patch） | — |

**处理策略**：
- **现在**：保留所有相关 config 字段（避免用户 `leaves.yml` 报 "unknown key"），仅在本表记录
- **PR 前**：根据上游 maintainer 反馈统一处理：删字段、加 `@Deprecated`、或基于 26.1 新 API 重写成新 patch
- **未来遇到类似情况**：**不 drop 到 features-todo/**（容易遗忘），直接删除 patch 文件并在本表追加一行

#### 3.2 13 个已 rebase 的 minecraft-patches

这些 patch 都可以自动应用（git 3-way merge 成功），因为它们在 Paper 26.1 里涉及的上下文变化不大：

1. Build changes（手动解决 1 行冲突）
2. Leaves Server Config（手动处理合并点位置变化）
3. Redstone Shears Wrench
4. Throttle goal selector during inactive ticking
5. Syncmatica Protocol
6. Dont respond ping before start fully
7. Bow infinity fix
8. Villager infinite discounts
9. Disable offline warn if use proxy
10. Make Item tick vanilla
11. Servux Protocol
12. Can disable LivingEntity aiStep alive check
13. Vanilla creative pickup behavior

#### 3.3 134 个待办 minecraft-patches

保存在 `leaves-server/minecraft-patches/features-todo/` 目录（非标准 paperweight 目录，paperweight 不会自动处理，是人工 backlog）。

跳过的原因主要是 `Repository lacks necessary blobs to fall back on 3-way merge`——目标文件的 git blob hash 与 Paper 26.1 不匹配，无法自动合并，需要人工处理。

其中一个典型例子：**`Leaves Utils`**（在 `Entity.java` 等核心类加 NBT 扩展字段和 `@NotNull` 注解）——需要适配 Paper 26.1 里 `Entity.save/load` 的新 API。

#### 3.4 1 个待办 paper-patch

`leaves-server/paper-patches/features-todo/0013-Leaves-Plugin.patch`

这是一个**大骨头**：Paper 26.1 彻底重构了 plugin provider 系统，以下文件**不再存在**：
- `PluginInitializerManager.java`
- `LegacyPaperMeta.java`, `PaperPluginMeta.java`
- `PluginFileType.java`
- `PaperPluginParent.java`
- `SimpleProviderStorage.java`
- `PluginRemapper.java`

Leaves 需要基于新 Paper plugin 架构（在 `src/main/java/io/papermc/paper/plugin/provider/` 下的新类）重新实现相同功能。

---

## 三、关键发现

### 3.1 Mojang 去混淆的副作用：类名/包名重命名

Paper 26.1 的最大变化不只是删除 Spigot 映射，**Mojang 也改了很多类的"官方名"**。已经识别并批量修复的重命名：

| 旧名 | 新名 | 涉及文件数 | 方式 |
|---|---|---|---|
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | 82 | 类名 + 字段名全局替换 |
| `FriendlyByteBuf.writeResourceLocation` | `writeIdentifier` | 6 | 方法重命名 |
| `ResourceKey.location()` | `ResourceKey.identifier()` | 5 | 点位精确替换 |
| `net.minecraft.Util` | `net.minecraft.util.Util` | 5 | import 路径 |
| `net.minecraft.world.entity.projectile.ThrownEnderpearl` | `.projectile.throwableitemprojectile.ThrownEnderpearl` | 1 | import 路径 |
| `net.minecraft.world.entity.npc.AbstractVillager` | `.npc.villager.AbstractVillager` | 1 | import 路径 |
| `net.minecraft.world.entity.vehicle.AbstractBoat` | `.vehicle.boat.AbstractBoat` | 1 | import 路径 |
| `net.minecraft.world.level.GameRules` | `.level.gamerules.GameRules` | 1 | import 路径 |
| `net.minecraft.world.entity.monster.ZombieVillager` | `.monster.zombie.ZombieVillager` | 1 | import 路径 |
| `net.minecraft.world.entity.animal.Chicken` | `.animal.chicken.Chicken` | 1 | import 路径 |

总计改动：**90 个 Leaves 自有 Java 文件，~300 处替换**（主要集中在 ResourceLocation）。

**临时占位**（TODO，后续要按新 recipe 系统重写）：
- `TippedArrowRecipe` → `CustomRecipe`（Minecraft 26.1 删除了这个特殊 recipe 类）
- `MapCloningRecipe` → `CustomRecipe`（同上）
- `case TippedArrowRecipe ignored` → `case CustomRecipe ignored1`（同上，case 匹配会失效）
- `case MapCloningRecipe ignored` → `case CustomRecipe ignored2`

**后续可能继续冒出的重命名**：无法完全预判，需要边 rebase patch 边发现。

### 3.2 Paper 插件系统重构

Paper 26.1 重构了插件 provider 架构。Leaves 的 "Leaves Plugin" patch（含自己的插件加载器、`PluginFileType` 注册、`PaperPluginMeta` 扩展）需要基于新架构**完全重写**。

### 3.3 Paper 26.1.2 是 ALPHA

- channel=ALPHA，`updatingMinecraft=false`
- 构建号 #5（截至评估时）
- Paper 可能还会有破坏性变更

### 3.4 mache 依赖冲突（已解决）

mache 26.1.2+build.1 引入 `at.yawk.lz4:lz4-java:1.10.1`，与 Leaves 原 `org.lz4:lz4-java:1.8.0` 有 capability 冲突。已移除 Leaves 侧的 lz4-java 依赖（mache 提供）。

### 3.5 .DS_Store 污染 patch（已解决）

在 macOS 上 paperweight 的内部 git 仓库会把 `.DS_Store` 的变化写进 patch。通过 `git config --global core.excludesfile ~/.gitignore_global` 添加全局忽略规则解决（记得接手者也要做这一步）。

---

## 四、目前的局限

### 4.1 未完成的 patch 必然会使编译失败

`./gradlew :leaves-server:compileJava` 当前仍报 **~101 个错误**。

**好消息**：类名/包路径的批量重命名已经修复。如果只是"名字"问题，错误早就应该降到接近 0。

**坏消息**：**剩余的错误都是"Leaves patch 提供的方法/字段不存在"**，因为 134 个 minecraft-patches 还没 rebase。例如：

- `ServerPlayerList.realPlayers()`（7 处）— 来自某个 minecraft-patch 加的便利方法
- `LivingEntity.lithium$subscribe(...)`（4 处）— 来自 `Lithium-Sleeping-Block-Entity.patch`
- `Entity.getLeavesData()`（1 处）— 来自 `Leaves-Utils.patch`（已知的大冲突 patch）
- `ServerPlayer.getBotList()`（3 处）— 来自 `Leaves-Fakeplayer.patch`
- `PlayerList.keepConnectionAliveAsync(...)`— 来自 `Async-keepalive.patch`
- `Player.placeNewPhotographer / removePhotographer`— 来自 `Replay-Mod-API.patch`
- `Entity.spawnInvulnerableTime`（2 处）— 来自 `Spawn-invulnerable-time.patch`
- `Entity.elytraAeronauticsNoChunk`（4 处）— 来自 `Elytra-aeronautics-no-chunk-load.patch`

更严重的是：**paper-server 侧的 feature patches 已经成功 apply（Leaves 在 CraftServer/CraftWorld 里加的 `nms.realPlayers()` 之类调用），但它们引用的 Minecraft 方法因为 minecraft-patch 被跳过了而不存在**。这说明 paper-patch 和 minecraft-patch 之间有深度依赖。

**换句话说**：到这里为止，我能"批量"做的都做了。接下来每减少一个错误，都需要成功 rebase 对应的 minecraft-patch。

### 4.2 paperweight 的 apply-skip-rebuild 工作流会丢失 patch

`git am --skip` 后 `rebuildMinecraftPatches` 只输出成功应用的 patch，**未应用的 patch 被从 `features/` 目录删除**。我手动备份到了 `features-todo/`，但：
- 这个目录不是 paperweight 标准目录，不会自动处理
- 如果有人再次运行 `rebuildMinecraftPatches`，todo 不会被恢复
- 继续 rebase 时应该从 `features-todo/` 一个个搬回 `features/` 并解决冲突

### 4.3 未验证的改动

以下改动已做但未被端到端验证，因为还没到能跑完整构建的阶段：

- `leavesweight` 的 LeavesclipTasks 单 jar 重写（dev bundle 路径未测）
- `leaves-server/build.gradle.kts.patch` 的 `--add-modules=jdk.incubator.vector`（JDK 25 中 Vector API 是否还在 incubator 未验证）
- mache 26.1.2+build.1 与 leavesclip 3.0.10 的兼容性（leavesclip 未必理解新的 bundler 格式）

---

## 五、给接手者的工作指南

### 5.1 环境准备

- macOS/Linux，JDK 25 LTS，Gradle 9.2+
- `gh` 已登录 HyacinthHaru 账号（HTTPS token，不用 SSH）
- `git config --global core.excludesfile ~/.gitignore_global`（含 `.DS_Store`）
- leavesweight 2.1.0-SNAPSHOT 已 publishToMavenLocal

### 5.2 Checkout & 验证现状

```bash
cd /Users/haru/Desktop/LeavesMC/Leaves
git status  # 应在 upgrade-26.1 分支
./gradlew applyAllPatches  # 应成功
./gradlew :leaves-server:compileJava  # 会失败（100+ 错误，预期）
```

### 5.3 推荐的继续路径（按工作量和依赖）

**优先级 1：把 Leaves 自有源码里的类名重命名处理掉**
- 这是所有后续工作的前置依赖
- 用脚本批量 grep + replace：
  ```bash
  # ResourceLocation → Identifier（举例，确认正确后可批量）
  find leaves-server/src/main/java leaves-api/src/main/java -name "*.java" \
    -exec sed -i '' 's/net\.minecraft\.resources\.ResourceLocation/net.minecraft.resources.Identifier/g; s/\bResourceLocation\b/Identifier/g' {} \;
  ```
- 然后 `compileJava` 看错误，逐个类名修复。每个 rename 确认前先用 `grep` 查 Minecraft 源确认新名。

**优先级 2：逐个 rebase `minecraft-patches/features-todo/` 里的 patch**
- 按 Leaves 原顺序或按冲突复杂度优先处理"简单功能"patch
- 每个 patch：
  1. 把它 `mv` 到 `features/` 当前最大编号 +1
  2. `./gradlew applyMinecraftPatches` 观察冲突
  3. 进入 `leaves-server/src/minecraft/java`（是个 git 工作区），手动解决冲突
  4. `git add . && git am --continue`
  5. `./gradlew :leaves-server:rebuildMinecraftPatches` 把解决方案保存回 patch 文件
- **重要**：不要一次跑 `rebuildMinecraftPatches` 处理多个 patch，否则没解决的会被删。一次一个。

**优先级 3：重写 `paper-patches/features-todo/0013-Leaves-Plugin.patch`**
- 硬骨头。需要先熟悉 Paper 26.1 的新 plugin provider 架构
- Leaves 原 patch 意图：给 Leaves 自有 plugin 类型加 loader/meta/provider
- 新架构下应该基于 `PluginProvider`/`PluginProviderFactory` 重新实现

### 5.4 提交约定（记忆已存入我的 memory）

- commit author 保持 `HyacinthHaru <122684177+HyacinthHaru@users.noreply.github.com>`
- 推送用 `gh` 的 HTTPS token，不用 SSH（本地私钥 `~/.ssh/id_ed25519` 不在 HyacinthHaru 的 GitHub 绑定列表中）
- 每 5-10 个 patch 做一次 commit + push，commit message 写清楚改了什么、遗留什么

---

## 六、文件索引

| 文件 | 作用 |
|---|---|
| `UPGRADE_TO_26.1.2.md` | 升级前的上游研究文档（调研） |
| `UPGRADE_26.1.2_PROGRESS.md` | **本文档**：进度与移交 |
| `leaves-server/minecraft-patches/features/` | 13 个已 rebase 的 minecraft patch |
| `leaves-server/minecraft-patches/features-todo/` | 134 个待 rebase 的 minecraft patch |
| `leaves-server/paper-patches/features/` | 15 个已 rebase 的 paper patch |
| `leaves-server/paper-patches/features-todo/` | 1 个待重写的 paper patch（Leaves Plugin） |
| `leaves-api/paper-patches/features/` | 9 个 api patch，全部通过 |
| `gradle.properties`, `build.gradle.kts`, `leaves-server/build.gradle.kts.patch`, `leaves-api/build.gradle.kts.patch` | 阶段 2 的构建配置 |

---

## 七、关联仓库

| 仓库 | 分支 | 状态 |
|---|---|---|
| [HyacinthHaru/leavesweight](https://github.com/HyacinthHaru/leavesweight) | `upgrade-26.1` | 已推送 bd023b6 |
| [HyacinthHaru/Leaves](https://github.com/HyacinthHaru/Leaves) | `upgrade-26.1` | 已推送 f96cebd，本次 commit 待推 |
| [PaperMC/Paper](https://github.com/PaperMC/Paper) | `main` (pinned to `8987f91c`) | upstream |
| [PaperMC/paperweight](https://github.com/PaperMC/paperweight) | `main` | upstream (leavesweight rebase target) |

---

_本文档由 Claude Code 在执行升级任务过程中持续更新。_
