# Leaves 升级到 Paper 26.1.2 — Patch Rebase Playbook

> **用途**：这是一份**操作手册 + 待办清单**，面向接手 / 延续 Leaves 26.1 升级工作的任何人（AI 或人）。读完这份文档，应该知道「此刻该做什么、怎么做、做完怎么验证」。
>
> 搭配文档：
> - [`UPGRADE_26.1.2_PROGRESS.md`](./UPGRADE_26.1.2_PROGRESS.md) — 历史进度和背景
> - [`UPGRADE_TO_26.1.2.md`](./UPGRADE_TO_26.1.2.md) — 升级前期调研（上游事实）

---

## 一、当前进度快照

截至 2026-04-15（commit `05b18a2`）：

| 模块 | 总数 | 已 rebase | 未完成 | 完成率 |
|---|---|---|---|---|
| `leaves-api/paper-patches/` (API patches) | 9 | 9 | 0 | **100%** ✓ |
| `leaves-server/paper-patches/files/` | 1 | 1 | 0 | **100%** ✓ |
| `leaves-server/paper-patches/features/` | 16 | 15 | 1 | 94% |
| `leaves-server/minecraft-patches/files/` | 0 | 0 | 0 | — |
| `leaves-server/minecraft-patches/features/` | 147 | 15 | 132 | 10% |
| 自有 Java 源码的类名迁移 | ~396 | 90 改动完成 | 新错误随 patch rebase 浮现 | ~85% 第一轮 |
| **总计 patch** | **173** | **40** | **133** | **23%** |

`./gradlew applyAllPatches` 当前完整通过。
`./gradlew :leaves-server:compileJava` 仍报 101 个错误（全部来自未 rebase 的 patch 提供的方法/字段）。

---

## 二、剩余待办总览

### 2.1 `leaves-server/minecraft-patches/features-todo/` 里的 132 个 patch

按规模粗分（以 `wc -l` 为指标）：

| 规模 | 行数区间 | 数量 | 预估单个耗时 | 累计耗时 |
|---|---|---|---|---|
| **超小** | ≤25 行，1 文件 1 hunk | ~35 个 | 3-5 分钟 | ~3 小时 |
| **小** | 25-60 行 | ~40 个 | 5-10 分钟 | ~5 小时 |
| **中** | 60-200 行 | ~35 个 | 10-25 分钟 | ~10 小时 |
| **大** | 200-1000 行 | ~15 个 | 30-60 分钟 | ~10 小时 |
| **硬骨头** | 1000+ 行 / 涉及已删除的 Paper 架构 | ~7 个 | 1-4 小时（可能需要放弃/重写） | ~15 小时 |
| **总计** | — | 132 | — | **~43 小时** |

### 2.2 `leaves-server/paper-patches/features-todo/0013-Leaves-Plugin.patch`

**已知硬骨头**。Paper 26.1 删了 `PluginInitializerManager`、`LegacyPaperMeta`、`PaperPluginMeta`、`PluginFileType`、`PaperPluginParent`、`SimpleProviderStorage`、`PluginRemapper` 等类。Leaves 的插件系统扩展要基于 Paper 26.1 的新 `PluginProvider` / `PluginProviderFactory` 架构**重写**。估计 4-8 小时。

### 2.3 后续工作（patch 全部 rebase 完之后）

- `./gradlew :leaves-server:compileJava` 全绿（可能暴露新的适配问题）
- `./gradlew createMojmapLeavesclipJar` 构建成功
- 运行时启动测试（`./gradlew runLeavesclip`）
- Fakeplayer / 协议 / Linear 区域 / 各种 config 项的功能测试
- leavesclip 3.0.10 在 Paper 26.1 的 bundler 格式下是否仍兼容需要验证
- 发布和 CI 完整跑通

---

## 三、标准工作流（每个 patch）

### Step 1：从 `features-todo/` 挑一个 patch

优先选超小/小规模的，按相互依赖关系：
- 依赖少的先做（检查 patch 是否引用其他 patch 加的字段/方法）
- 同一个源文件多个 patch 应该连续做，否则后 apply 的会因前 apply 改了上下文而失败

### Step 2：移动到 `features/` 并尝试 apply

```bash
mv leaves-server/minecraft-patches/features-todo/XXXX-Name.patch \
   leaves-server/minecraft-patches/features/NNNN-Name.patch
./gradlew :leaves-server:applyMinecraftPatches --no-daemon
```

几乎 100% 会失败，错误是 `Repository lacks necessary blobs to fall back on 3-way merge`（因为 Paper 26.1 的 blob hash 和 patch 里记录的不符）。

### Step 3：手动应用

```bash
cd leaves-server/src/minecraft/java   # 这是 paperweight 的内部 git 工作区
git am --show-current-patch > /tmp/current.patch   # 看原始 patch
```

对每个 hunk：
1. `grep -n "关键context行" <file>` 定位当前位置
2. 用 `Read` 看周围上下文
3. 用 `Edit` apply 改动
4. 注意 **Paper 26.1 常见重命名**（见第五节）

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

paper-patches 也要注意同样的 `.DS_Store`/Finder 污染问题——先清理 `find paper-server -name ".DS_Store" -delete; find . -name "* [0-9].patch" -delete`。

### Step 6：验证 + commit

```bash
git diff --stat leaves-server/minecraft-patches/features/
./gradlew :leaves-server:applyMinecraftPatches --no-daemon  # 应该成功
./gradlew :leaves-server:compileJava --no-daemon --console=plain 2>&1 | \
    grep -c "error:"   # 错误数应下降
```

每 5 个 patch 做一次 commit + push。commit message 写清改了哪些 patch 和每个的关键适配点。

---

## 四、主要改动位置（按目录/文件出现频次）

`features-todo/` 里的 patch 90% 改动集中在以下文件（grep 自 patch headers 得出）：

| 文件 | 预估被触及次数 | 改动类型 |
|---|---|---|
| `net/minecraft/world/entity/Entity.java` | ~25 | NBT 字段、tick hook、save/load |
| `net/minecraft/world/entity/LivingEntity.java` | ~20 | aiStep hook、effect hook |
| `net/minecraft/server/level/ServerPlayer.java` | ~15 | tick hook、damage hook、field |
| `net/minecraft/server/level/ServerLevel.java` | ~12 | tick、chunk、entity |
| `net/minecraft/world/level/Level.java` | ~10 | tick、update |
| `net/minecraft/server/MinecraftServer.java` | ~8 | server startup / tick |
| `net/minecraft/server/network/ServerGamePacketListenerImpl.java` | ~10 | 网络包处理 |
| `net/minecraft/server/players/PlayerList.java` | ~6 | `realPlayers()` 等 |
| `net/minecraft/world/entity/player/Player.java` | ~8 | 字段、hook |
| `ca/spottedleaf/moonrise/paper/PaperHooks.java` | ~3 | Moonrise 集成 |

### Leaves 自有代码（可能还要改的）

`leaves-server/src/main/java/org/leavesmc/leaves/` 下约 344 个 Java 文件。已完成第一轮 Mojang 重命名（`ResourceLocation`→`Identifier` 等），但 patch rebase 时会暴露新的 API 变化——需要按出错反馈增量修。

---

## 五、Paper 26.1 常见适配速查表

rebase patch 时最常踩的坑，按影响范围排序：

### 5.1 Minecraft 类重命名（已批量修复在 Leaves 自有源码，但 patch 里的 context 还是旧名）

| 旧名 | 新名 |
|---|---|
| `ResourceLocation` | `Identifier` |
| `ResourceKey.location()` | `ResourceKey.identifier()` |
| `FriendlyByteBuf.writeResourceLocation` | `writeIdentifier` |
| `FriendlyByteBuf.readResourceLocation` | `readIdentifier` |
| `net.minecraft.Util` | `net.minecraft.util.Util` |
| `GameRules.RULE_SPECTATORSGENERATECHUNKS` + `.getBoolean()` | `GameRules.SPECTATORS_GENERATE_CHUNKS` + `.get()` |

### 5.2 Entity 子类包迁移

| 类 | 旧包 | 新包 |
|---|---|---|
| `ThrownEnderpearl` | `world.entity.projectile` | `world.entity.projectile.throwableitemprojectile` |
| `AbstractVillager` | `world.entity.npc` | `world.entity.npc.villager` |
| `AbstractBoat` | `world.entity.vehicle` | `world.entity.vehicle.boat` |
| `ZombieVillager` | `world.entity.monster` | `world.entity.monster.zombie` |
| `Chicken` | `world.entity.animal` | `world.entity.animal.chicken` |
| `GameRules` | `world.level` | `world.level.gamerules` |

（更多类位置可能变化，需要通过 `find leaves-server/src/minecraft/java -name "ClassName.java"` 定位。）

### 5.3 方法参数 / 局部变量重命名（影响 patch context 匹配）

| Paper 1.21.10 | Paper 26.1 |
|---|---|
| `Entity.move(MoverType type, Vec3 movement)` | `Entity.move(MoverType moverType, Vec3 delta)` |
| `FireworkRocketItem.use(... ItemStack itemInHand ...)` | `... ItemStack itemStack ...` |
| `ServerPlayer.hurtServer(... DamageSource damageSource ...)` | `... DamageSource source ...` |
| `Entity#snapTo` 拆成 `#snapTo` + `#absSnapTo` | chunk touch 只在 `absSnapTo` |

### 5.4 删除的类 / 功能（要改策略）

| 实体 | 做法 |
|---|---|
| `TippedArrowRecipe` / `MapCloningRecipe` | Mojang 把它们换成 transmute recipe system。临时 workaround：在 Leaves 自有代码里用 `CustomRecipe` 代替泛型参数（已做），但 REI display 的实际行为需要重写 |
| Paper 的 plugin provider 系统（`PaperPluginMeta`, `PluginInitializerManager` 等） | 见 `0013-Leaves-Plugin.patch` 的待办 |
| Paper Entity `Metrics.java` 的 `legacy_plugins` 分类 | 已自动从 "Leaves Plugin" 的 paper-patch 被跳过时消失 |
| Paper Main.java 的 "build 14 天过时提醒" | 已删除，Leaves 的品牌改动随之失效（不需要保留） |

### 5.5 依赖冲突（已解决）

- `org.lz4:lz4-java:1.8.0` 冲突 mache 自带的 `at.yawk.lz4:lz4-java:1.10.1`。→ 已从 `leaves-server/build.gradle.kts.patch` 移除。

---

## 六、"硬骨头" 清单

以下 patch **单个工作量 > 1 小时**，建议留到中后期、在工具链稳定后集中处理：

1. **`Leaves-Utils`** (原 0003) — Entity NBT 扩展、大量 `@Nullable` → `@NotNull` 注解、跨 Entity/Level/LevelAccessor 的 `getServer()` 签名改动
2. **`Leaves-Protocol-Core`** (原 0004) — 自定义 `CustomPacketPayload` 基类 + 注册中心
3. **`Leaves-Fakeplayer`** (原 0007) — 大型 patch，加 Bot 系统（`ServerBot`、`BotList`、`interactAt`）
4. **`Leaves-Plugin`** (paper-patches 0013) — **Paper 插件系统已重构，需完全重写**
5. **`BBOR-Protocol` / `PCA-sync-protocol` / `Alternative-block-placement-Protocol` / `Jade-Protocol` / `Xaero-Map-Protocol` / `Support-REI-protocol`** — 客户端协议，多半依赖 `Leaves-Protocol-Core` 先完成
6. **`Replay-Mod-API`** — `placeNewPhotographer` / `removePhotographer` API
7. **`Async-keepalive`** — 最新的改动（原 #838 commit），依赖 Paper 网络层的最新状态

---

## 七、推荐的推进节奏

**原则**：每 5-15 个 patch 一次 commit；每个 session 聚焦一个 size 档次；每次 push 前跑 `compileJava` 作为进度指标。

建议顺序：

1. **第 1 批（本节奏的第一个 session）**：5 个 ≤25 行的超小 patch。目标：10-15 分钟内完成 + 测试。
2. **第 2-4 批**：每批 5-10 个小-中 patch，按相互依赖分批。
3. **第 5 批之后**：开始挑战"硬骨头"，每个 patch 自己一次 commit。
4. **收尾阶段**：`Leaves Plugin` paper-patch（Paper 26.1 新架构重写）+ 全量 compile + 运行时测试。

每个 session 结束建议产出：
- 一份 `SESSION_REPORT_YYYY-MM-DD.md`（记录：改了哪些 patch、每个的 key 适配点、错误数变化、遗留问题）
- `UPGRADE_26.1.2_PROGRESS.md` 中的统计数字同步更新

---

## 八、验证方法

### 8.1 本地

```bash
# 基本验证
./gradlew applyAllPatches                          # 应成功
./gradlew :leaves-server:compileJava --console=plain 2>&1 | grep -c "error:"  # 错误数

# 错误分布
./gradlew :leaves-server:compileJava --console=plain 2>&1 | \
    grep "symbol:" | awk '{print $NF}' | sort | uniq -c | sort -rn | head -20
```

### 8.2 CI

- `test.yml` (Leaves Test CI) 会在 push 时自动触发
- `Apply Patches` step 必须绿
- `Create Leavesclip Jar` 在所有 patch rebase 完前都会红，这是预期

### 8.3 里程碑

| 里程碑 | 标志 |
|---|---|
| M1：applyAllPatches 绿 | ✅ 已达成 |
| M2：compileJava 错误数 < 50 | 🎯 下一个目标（需 rebase ~30 个 patch） |
| M3：compileJava 错误数 = 0 | 🎯 所有 patch rebase 完 |
| M4：`createMojmapLeavesclipJar` 成功 | 🎯 jar 产物可用 |
| M5：runtime 启动成功 | 🎯 服务端能起来 |
| M6：功能测试通过 | 🎯 可发布 |

---

## 九、协作约定

- **推送身份**：HyacinthHaru（`122684177+HyacinthHaru@users.noreply.github.com`），通过 `gh` 的 HTTPS token 推送（不用 SSH）
- **macOS 用户注意**：确保 `git config --global core.excludesfile ~/.gitignore_global`，里面至少有 `.DS_Store`，否则 paperweight 内部 git 会把 macOS Finder 垃圾带进 patch
- **每次 push 前清理**：`find paper-server leaves-server -name ".DS_Store" -delete; find paper-server leaves-server -name "* [0-9].java" -delete`

---

_最后更新：2026-04-15，commit `05b18a2`_
