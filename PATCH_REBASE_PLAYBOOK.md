# Leaves 升级到 Paper 26.1.2 — Patch Rebase Playbook

> **文档角色**：**操作手册 + 协作约定**。读完这份，应该知道「怎么做一个 patch 的 rebase、怎么避开历史踩过的坑、push 前怎么清理、怎么跟进 Paper upstream 新版本」。
>
> **不追踪即时进度**（那属于 [`SPRINT_PHASE.md`](./SPRINT_PHASE.md)）。本文档只在工作流/约定发生变化时更新。
>
> 配套：
> - [`SPRINT_PHASE.md`](./SPRINT_PHASE.md) — 当前状态
> - [`LIMITATIONS.md`](./LIMITATIONS.md) — 局限性与技术债
> - [`UPGRADE_26.1.2_PROGRESS.md`](./UPGRADE_26.1.2_PROGRESS.md) — 历史脉络 + obsoleted 账本
> - [`STAGE5_TEST_CHECKLIST.md`](./STAGE5_TEST_CHECKLIST.md) — 运行时测试 checklist
> - [`UPGRADE_TO_26.1.2.md`](./UPGRADE_TO_26.1.2.md) — 升级前上游调研

---

## 一、当前阶段（2026-04-17）

**阶段 3/4 完成，阶段 5 准备就绪**。171/171 patch 已 rebase，compileJava=0 errors，jar 可构建，CI 绿，剩运行时 mod 客户端对接测试。

详细数字见 [`SPRINT_PHASE.md` §一](./SPRINT_PHASE.md)。后续阶段概览见 [`UPGRADE_26.1.2_PROGRESS.md` §二](./UPGRADE_26.1.2_PROGRESS.md)。

---

## 二、标准工作流（每个 patch）

### Step 1：从 `features-todo/` 挑一个 patch

当前只剩 3 个，见 [`SPRINT_PHASE.md` §二](./SPRINT_PHASE.md)。对早期阶段（patches 多）的挑选策略：
- 优先选超小/小规模的
- 检查 patch 是否引用其他 patch 加的字段/方法（有依赖关系的放后面）
- 同一个源文件多个 patch 应该连续做，否则后 apply 的会因前 apply 改了上下文而失败

### Step 2：移动到 `features/` 并尝试 apply

```bash
mv leaves-server/minecraft-patches/features-todo/XXXX-Name.patch \
   leaves-server/minecraft-patches/features/NNNN-Name.patch
./gradlew :leaves-server:applyMinecraftPatches --no-daemon
```

几乎 100% 会失败，错误是 `Repository lacks necessary blobs to fall back on 3-way merge`（Paper 26.1 的 blob hash 和 patch 里记录的不符）。

### Step 3：手动应用

```bash
cd leaves-server/src/minecraft/java   # paperweight 的内部 git 工作区
git am --show-current-patch > /tmp/current.patch   # 看原始 patch
```

对每个 hunk：
1. `grep -n "关键 context 行" <file>` 定位当前位置
2. 用 `Read` 看周围上下文
3. 用 `Edit` apply 改动
4. 注意 **Paper 26.1 常见重命名**（见第四节速查表）

### Step 4：继续 am

```bash
cd leaves-server/src/minecraft/java
git add <specific-file.java>   # ⚠️ 不要用 git add -A
git am --continue
```

⚠️ **不要 `git add -A`**！paperweight 的内部 git 仓库初始化时把 `.DS_Store` 文件也 track 了（paperweight 在 `SetupMinecraftSources` 里用的是类似 `git add .` 的逻辑），`git add -A` 会把 macOS Finder 新增的 `.DS_Store` binary stage 到你的 commit 里。然后 `git format-patch` 导出的 patch 会带 `.DS_Store` 的 binary diff，CI 上的干净环境会在 `git am` 3-way merge 时 bail 出（`error: invalid object ... for '.DS_Store'`）。

用 `git add <具体文件路径>` 只 stage 你真正改动的那个文件，别的文件让它留在工作树里。

### Step 5：用 `git format-patch` 保存 patch 文件

⚠️ **不要用 `./gradlew :leaves-server:rebuildMinecraftFeaturePatches`**！它隐式依赖 `applyMinecraftFeaturePatches`，会先 reset 工作区 + 重新 apply，导致刚手动修好的 patch 因为还没保存而再次失败，形成死循环。

正确做法：直接从 git HEAD 取 patch 文件：

```bash
cd leaves-server/src/minecraft/java
# N = 本次 session 一共 am 成功的 patch 数
git format-patch -N --no-signature --zero-commit --full-index --no-stat \
    -o /tmp/new-patches/
```

然后把 `/tmp/new-patches/0001-*.patch` 依次 `cp` 到 `leaves-server/minecraft-patches/features/` 下对应的编号位置：

```bash
cd <project root>
cp /tmp/new-patches/0001-XXX.patch leaves-server/minecraft-patches/features/NNNN-XXX.patch
# ... 对每个 patch 重复
```

### Step 5.5：验证 patch 可独立 apply

清空后再跑一次：

```bash
rm -rf paper-server leaves-server/src/minecraft
./gradlew applyAllPatches --no-daemon
```

必须 BUILD SUCCESSFUL。如果失败，打开失败的 patch 手动调整 context。

paper-patches 也要注意同样的 `.DS_Store`/Finder 污染问题 — 先清理：
```bash
find paper-server leaves-server -name ".DS_Store" -delete
find paper-server leaves-server -name "* [0-9].java" -delete
find . -name "* [0-9].patch" -delete
```

### Step 6：验证 + commit

```bash
git diff --stat leaves-server/minecraft-patches/features/
./gradlew :leaves-server:applyMinecraftPatches --no-daemon  # 应该成功
./gradlew :leaves-server:compileJava --no-daemon --console=plain 2>&1 | \
    grep -c "error:"   # 错误数应下降或与上一 batch 持平
```

commit 用这个格式（已在历史 commit 里保持一致）：

```
Patch rebase batch NN: <patch 名或主题> (XX→YY, A%→B%)
```

---

## 三、针对大型 patch 的分段应用策略

从 batch 22 开始遇到 1000+ 行的大 patch（`0003 Leaves-Utils`、`0007 Leaves-Fakeplayer`、`0136 Lithium-Sleeping-Block-Entity` 等），直接 `git am` 的失败信息太庞杂，建议：

1. **先 `git am --abort` 然后手工 split**：把 patch 按文件拆成独立的 hunk bundle，逐个文件处理
2. **或者用 agent 并行处理**：如果多个文件相互独立，可以分发给多个 Agent 并发编辑，但要确保**没有互相影响**（batch 23 发生过"并发 agent 把 PCA 改动污染进 0131 desync commit"的事故）
3. **每次只 format-patch 当前 commit**：`git format-patch HEAD~1..HEAD --no-signature --zero-commit --full-index -o /tmp/`
4. **大 patch 提示**：`Xmaxerrs=500` 会帮你看到真实错误分布；错误从"几百"降到"几十"往往意味着进展

---

## 四、Paper 26.1 常见适配速查表

### 4.1 Minecraft 类重命名（Leaves 自有源码批量修过，但 patch 里的 context 仍是旧名）

| 旧名 | 新名 |
|---|---|
| `ResourceLocation` | `Identifier` |
| `ResourceKey.location()` | `ResourceKey.identifier()` |
| `FriendlyByteBuf.writeResourceLocation` | `writeIdentifier` |
| `FriendlyByteBuf.readResourceLocation` | `readIdentifier` |
| `net.minecraft.Util` | `net.minecraft.util.Util` |
| `GameRules.RULE_SPECTATORSGENERATECHUNKS` + `.getBoolean()` | `GameRules.SPECTATORS_GENERATE_CHUNKS` + `.get()` |
| `GameRules.RULE_DISABLE_RAIDS` + `!getBoolean()` | `GameRules.RAIDS` + `get()`（**逻辑反转**） |

### 4.2 Entity/其他类包迁移

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
| `critereon`（包） | `advancements.critereon` | `advancements.criterion` |

（更多类位置可能变化，需要通过 `find leaves-server/src/minecraft/java -name "ClassName.java"` 定位。）

### 4.3 方法参数 / 局部变量重命名（影响 patch context 匹配）

| Paper 1.21.10 | Paper 26.1 |
|---|---|
| `Entity.move(MoverType type, Vec3 movement)` | `Entity.move(MoverType moverType, Vec3 delta)` |
| `FireworkRocketItem.use(... ItemStack itemInHand ...)` | `... ItemStack itemStack ...` |
| `ServerPlayer.hurtServer(... DamageSource damageSource ...)` | `... DamageSource source ...` |
| `Entity#snapTo` 单一方法 | 拆成 `#snapTo` + `#absSnapTo`；chunk touch 只在 `absSnapTo` |
| `profilerFiller` 字段 | `profiler` |
| `level.random`（public 字段） | `level.getRandom()`（protected 字段，走 getter） |
| `context`（BlockItem 方法参数） | `placeContext` |
| `clickedPos`（ShovelItem） | `pos` |
| `blockState`（ShearsItem） | `state` |
| `shootable`（Player 字段） | `heldWeapon` |

### 4.4 API 签名变化

| 原 | 现 |
|---|---|
| `SimpleContainer.addListener(listener)` | 已删。改用匿名子类重写 `setChanged()` |
| `ItemStackTemplate.display()`（REI） | 已删。改用 `new SlotDisplay.ItemStackSlotDisplay(stack)` |
| `Player.getGameProfile()` | 已删。改用 `player.gameProfile.name()`（公共字段） |
| `@Nullable OuterType.InnerType` | jspecify：`OuterType.@Nullable InnerType` |

### 4.5 删除的类 / 功能（要改策略）

| 实体 | 做法 |
|---|---|
| `TippedArrowRecipe` / `MapCloningRecipe` | Mojang 换成 transmute recipe system。临时：在 Leaves 自有代码里用 `CustomRecipe` 代替泛型参数（已做）；REI display 实际行为待重写 |
| Paper 的 plugin provider 系统（`PaperPluginMeta`, `PluginInitializerManager` 等） | 见 `0013-Leaves-Plugin.patch` 的待办 |
| Paper Entity `Metrics.java` 的 `legacy_plugins` 分类 | 已随 "Leaves Plugin" paper-patch 被跳过而消失 |
| Paper Main.java 的 "build 14 天过时提醒" | 已删除，Leaves 的品牌改动随之失效（不需要保留） |

### 4.6 依赖冲突（已解决）

- `org.lz4:lz4-java:1.8.0` 冲突 mache 自带的 `at.yawk.lz4:lz4-java:1.10.1`。→ 已从 `leaves-server/build.gradle.kts.patch` 移除。

### 4.7 遇到 obsolete patch 时的处理流程

有时候一个 patch 到 Paper 26.1 下已经"没必要了"，两种典型情况：
- **a. Paper 自己修了同一个 bug**（如 `Fix-Paper-config-preventMovingIntoUnloadedChunks`，Leaves 作者贡献回了上游）
- **b. Paper 重构/删除了相关代码路径**（如 `Only-check-for-spooky-season-once-an-hour`）

判断方法：在当前 apply 失败时，先读 patch 原意图（commit message、代码上下文），然后用 `grep` 在 `leaves-server/src/minecraft/java/` 里找对应 Paper 26.1 的实现，看是否已经包含了等价修复。

**处理步骤**：

1. **直接删除 patch 文件**（不要放进 `features-todo/`，那里表示"待 rebase"，obsolete 不属于这类）
2. **在 [`UPGRADE_26.1.2_PROGRESS.md` §三](./UPGRADE_26.1.2_PROGRESS.md) 的 Obsoleted 表格追加一行**
3. **保留残留的 LeavesConfig 字段**（避免用户升级后的 `leaves.yml` 报 "unknown key"）
4. **commit message 说明**：写清是 "dropped (obsoleted by upstream)"，不是 "rebased"

---

## 五、主要改动位置（grep 自 patch headers）

`features-todo/` 里的 patch 主要改动集中在：

| 文件 | 改动类型 |
|---|---|
| `net/minecraft/world/entity/Entity.java` | NBT 字段、tick hook、save/load |
| `net/minecraft/world/entity/LivingEntity.java` | aiStep hook、effect hook |
| `net/minecraft/server/level/ServerPlayer.java` | tick hook、damage hook、field |
| `net/minecraft/server/level/ServerLevel.java` | tick、chunk、entity |
| `net/minecraft/world/level/Level.java` | tick、update |
| `net/minecraft/server/MinecraftServer.java` | server startup / tick |
| `net/minecraft/server/network/ServerGamePacketListenerImpl.java` | 网络包处理 |
| `net/minecraft/server/players/PlayerList.java` | `realPlayers()` 等 |
| `net/minecraft/world/entity/player/Player.java` | 字段、hook |
| `ca/spottedleaf/moonrise/paper/PaperHooks.java` | Moonrise 集成 |

### Leaves 自有代码

`leaves-server/src/main/java/org/leavesmc/leaves/` 下约 344 个 Java 文件。已完成第一轮 Mojang 重命名（`ResourceLocation`→`Identifier` 等），但阶段 4 还要扫第二轮 API 迁移。

---

## 六、验证方法

### 6.1 本地

```bash
# 基本验证
./gradlew applyAllPatches                          # 应成功
./gradlew :leaves-server:compileJava --console=plain 2>&1 | grep -c "error:"

# 错误分布（找缺失的 symbol）
./gradlew :leaves-server:compileJava --console=plain 2>&1 | \
    grep "symbol:" | awk '{print $NF}' | sort | uniq -c | sort -rn | head -20

# 错误按文件分组
./gradlew :leaves-server:compileJava --console=plain 2>&1 | \
    grep "error:" | awk -F: '{print $1}' | sort | uniq -c | sort -rn | head -20
```

### 6.2 CI

- `test.yml` (Leaves Test CI) 会在 push 时自动触发
- `Apply Patches` step 必须绿
- `Create Leavesclip Jar` 在所有 patch rebase 完前都会红，这是预期

### 6.3 里程碑

| 里程碑 | 标志 | 状态 |
|---|---|---|
| M1 | `applyAllPatches` 绿 | ✅ 已达成（batch 10） |
| M2 | `compileJava` 错误数 < 50 | ✅ 已达成（batch 31 直接到 0） |
| M3 | `compileJava` = 0 errors | ✅ 已达成（batch 31） |
| M4 | `createLeavesclipJar` 成功（task 在 Paper 26.1 改名，原 `createMojmapLeavesclipJar`）| ✅ 已达成（batch 32 起） |
| M5 | runtime 启动成功 | 🎯 阶段 5 人工验证待开始 |
| M6 | 功能测试通过 | 🎯 可发布 |

---

## 七、推进节奏建议（历史参考）

**原则**：每 1-2 个 patch 一次 commit + push；每个 session 聚焦一个 size 档次；每次 push 前跑 `compileJava` 作为进度指标。

**阶段 3 时的节奏**（剩 3 个大 patch 时）：
- 单独一个 batch 做 0136（2480 行），谨慎处理
- 单独一个 batch 做 0072（537 行）
- 单独一个 batch 做 0102（808 行）
- 之后转入阶段 4（API 迁移扫第二轮）

每个 session 结束建议产出：
- 更新 [`SPRINT_PHASE.md`](./SPRINT_PHASE.md) 的状态快照和批次历史
- 对于特别大的 patch 或发现新现象，产出 `SESSION_REPORT_YYYY-MM-DD.md`

---

## 八、"硬骨头" 清单（历史回顾）

以下 patch 当时被标记为"单个工作量 > 1 小时"。**全部已完成**：

| patch | batch | 实际情况 |
|---|---|---|
| `Leaves-Utils` (原 0003 → 现 0124) | 22 | Entity NBT 扩展、大量 `@Nullable` → `@NotNull` 注解 |
| `Leaves-Protocol-Core` (原 0004 → 现 0122) | 21 | 自定义 `CustomPacketPayload` 基类 + 注册中心 |
| `Leaves-Fakeplayer` (原 0007 → 现 0138) | 24 | 26 文件，`ServerBot`、`BotList`、`interactAt` |
| 6 个 protocol patches（BBOR/PCA/Alt-block/Jade/Xaero/REI） | 22 | 都依赖 Protocol-Core |
| `Async-keepalive` | 23 | 网络层最新 |
| `Replay-Mod-API` (0072 → 0144) | 28 | 537 行；修正 GameRules API、FeatureHooks view/sim distance |
| `Lithium-Sleeping-Block-Entity` (0136 → 0143) | 27 | 2480 行，29 文件，最大单 patch。全部 hunk 手工应用 |
| `Old-Block-remove-behaviour` (0102 → 0145) | 29 | 808 行，33 文件。30+ 种方块的 `onRemove` |
| `Leaves-Plugin` (paper-patches 0013 → 0016) | 30 | 实际比预估简单很多。PluginRemapper hunk 整块删除（Paper 26.1 已删），其它 7 文件 3-way merge |

注：patch 编号括号内是"原 → 现"——batch 34 升级 Paper upstream 时重导出，原来的 0036+ 全部 +1 编号。

---

## 九、Paper upstream 升级流程（batch 34 经验）

跟进 PaperMC/Paper 新 commit 时的标准操作：

### 9.1 评估阶段（不动代码）

1. 获取新 commit 列表：`git log --oneline <old-pin>..<new-pin>`
2. 看每个 commit 的改动范围：`git show --stat <commit>`
3. 重点看上游改动的 `paper-server/patches/**` 和 API 源文件 — 这些会影响 Leaves 的 patch apply
4. 评估冲突可能性（查 Leaves 自有代码是否 touch 了同一区域）

### 9.2 升级操作

```bash
# 1. 改 pin
sed -i '' "s/paperRef=<old>/paperRef=<new>/" gradle.properties

# 2. 强制重 fetch Paper upstream（paperweight 不会自动检测 commit 变化）
rm -rf leaves-server/.gradle/caches/paperweight/upstreams/paper
rm -rf paper-server leaves-server/src/minecraft paper-api

# 3. 重跑 apply
./gradlew applyAllPatches --no-daemon
```

### 9.3 冲突处理

paperweight 的 am 在上游改动影响 Leaves patch context 时会报 `git am` 失败。常见形式：

- `CONFLICT (modify/delete): X deleted in <Leaves patch> and modified in HEAD`
  → 如果 Leaves 要删除该文件，用 `git rm X` 确认删除意图，然后 `git am --continue`
- `error: Failed to merge in the changes` on blob hash mismatch
  → 需要去 paperweight 内部 git 工作区手动修改上下文。看 `git am --show-current-patch=diff` 对比当前文件状态

### 9.4 导出更新后的 patch 集

**关键**：paperweight 不会自动把 conflict 后的 manual fix 保存回 Leaves 的 `features/*.patch`。你必须：

```bash
# 进入 paperweight 内部 git（对 api/mc/server 各跑一次）
cd leaves-api  # 或 leaves-server/src/minecraft/java 或 paper-server

# 导出所有 Leaves 层的 commit 到临时目录
git format-patch -N --no-signature --zero-commit --full-index --no-stat -o /tmp/new-patches/
# N = Leaves 层 commit 数（不含 Paper 上游部分）

# 归一化 subject（重要：paperweight 默认用 [PATCH]）
sed -i '' 's/^Subject: \[PATCH [0-9]*\/[0-9]*\]/Subject: [PATCH]/' /tmp/new-patches/*.patch

# 覆盖 features/
rm <project>/features/*.patch
cp /tmp/new-patches/*.patch <project>/features/
```

### 9.5 验证

```bash
rm -rf paper-server leaves-server/src/minecraft paper-api
./gradlew applyAllPatches          # 期望 BUILD SUCCESSFUL
./gradlew :leaves-server:createLeavesclipJar  # 期望产出 ~60MB jar
```

### 9.6 注意事项

- **"discourage world name use"这类上游 API 转向**不一定直接破坏 Leaves 代码 — 只要是 `@ApiStatus.Obsolete` 而不是删除，继续用老 API 仍能编译
- **重导出会自动修复"重复编号"之类的 patch 整理瑕疵**（如 batch 34 顺带清掉了原"两个 0035"问题）
- **从不同 CI 运行结果看**：gradle task 名可能随 upstream 改（例如 batch 32 发现 `createMojmapLeavesclipJar` → `createLeavesclipJar`）。每次 Paper 大升级后确认 `gradlew tasks --all` 相应任务还在

---

## 十、协作约定

- **推送身份**：HyacinthHaru（`122684177+HyacinthHaru@users.noreply.github.com`），通过 `gh` 的 HTTPS token 推送（不用 SSH）
- **不要在 commit message 里加 `Co-Authored-By: Claude ...`**：Leaves 是 HyacinthHaru 的 fork，commit log 上保持单一作者更清爽。如果不小心加了，需要在 push 前用 `git filter-branch` 或 `git rebase -i` 清掉再 force-push
- **macOS 用户注意**：
  - 确保 `git config --global core.excludesfile ~/.gitignore_global`，里面至少有 `.DS_Store`，否则 paperweight 内部 git 会把 macOS Finder 垃圾带进 patch
  - 需要清理 `runLeavesSetup` 缓存里 tracked 的 .DS_Store：
    ```bash
    cd leaves-server/.gradle/caches/paperweight/taskCache/runLeavesSetup
    git rm -f $(git ls-files | grep DS_Store)
    git commit -m "remove DS_Store"
    ```
    否则每次 3-way merge 都会被它阻塞
- **每次 push 前清理**：
  ```bash
  find paper-server leaves-server -name ".DS_Store" -delete
  find paper-server leaves-server -name "* [0-9].java" -delete
  find . -name "* [0-9].patch" -delete
  ```
- **batch 20 教训**：误把 `0038-CCE-update-suppression.patch` / 重复的 `0039-Movable-Budding-Amethyst.patch` 留在 `features/`（untracked，git ls-files 看不出），但 `applyAllPatches` 仍会按文件名顺序读取，导致缺失前置依赖时整条链断裂。**新增 patch 前务必先 `git ls-files leaves-server/minecraft-patches/features/` 看一下与 `ls` 输出的差集**
- **batch 23 教训**：并发派发 agent 处理多个独立文件时，可能会出现"agent A 的改动进了 agent B 的 commit"的污染（git 工作区是共享的）。**如果使用并发 agent，每个 agent 要在独立 worktree 里工作，或者强制串行**
- **Paper upstream 漂移**：上游 Paper main 持续推进，已有 patch 的 `index <SHA>..<SHA>` 行可能因为底层文件被改而无法 3-way merge（典型如 batch 20 遇到的 0035 `DensityFunctions`，Paper 把 end-island 生成代码用 `ca.spottedleaf.moonrise.common.PlatformHooks.configFixMC159283()` 重写）。处理：手动改写 patch 内容以对齐新上下文（参考 `0035-Modify-end-void-rings-generation.patch`），保留 Leaves 配置开关意图
- **编译错误截断**：`build.gradle.kts` 已设 `-Xmaxerrs 500`。如果 javac 输出正好停在 500，真实错误数可能更多，酌情再调大

---

## 十一、目录结构导航

```
/Users/haru/Desktop/LeavesMC/Leaves/
├── SPRINT_PHASE.md                         ← 当前状态（single source of truth）
├── LIMITATIONS.md                          ← 局限性与技术债账本
├── UPGRADE_26.1.2_PROGRESS.md              ← 历史 + 背景
├── PATCH_REBASE_PLAYBOOK.md                ← 本文档：操作手册
├── STAGE5_TEST_CHECKLIST.md                ← 运行时测试 checklist
├── REI_RECIPE_MIGRATION.md                 ← REI recipe 迁移深度调研（batch 33 产物）
├── PROTOCOL_MOD_AUDIT.md                   ← 协议 mod 审计
├── SESSION_REPORT_2026-04-15.md            ← batch 1-10 历史
├── build.gradle.kts                        ← JDK 25, -Xmaxerrs 500
├── leaves-server/
│   ├── build.gradle.kts.patch              ← 含 -Xmaxerrs 500
│   ├── build/libs/                         ← jar 产物（leavesclip 62.5M / bundler 101M / server 29M）
│   ├── src/minecraft/java/                 ← applyAllPatches 产物（5256 vanilla 源 + Leaves 修改）
│   │   └── (paperweight 内部 git 工作区，手动 am 在这)
│   ├── minecraft-patches/
│   │   ├── features/                       ← 145 个 patch（0001-0145 唯一编号）
│   │   └── features-todo/                  ← 空
│   └── paper-patches/
│       ├── features/                       ← 16 个 patch
│       ├── features-todo/                  ← 空
│       └── files/src/                      ← 1 个单文件 patch
├── leaves-api/paper-patches/features/      ← 9 个 patch
├── paper-server/                           ← applyAllPatches 产物（paperweight 内部 git）
├── paper-api/                              ← applyAllPatches 产物（paperweight 内部 git）
└── .gradle/.../paperweight/taskCache/runLeavesSetup/
                                            ← paperweight 缓存
```

---

_本文档最后更新：2026-04-17，batch 34 完成后的阶段 5 准备期。更新新增了 §九 Paper upstream 升级流程（基于 batch 34 实战经验）。_
_更新节奏：只在工作流/约定变化时改；patch 数字变化去 `SPRINT_PHASE.md`。_
