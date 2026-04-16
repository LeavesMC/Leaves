# Leaves 26.1.2 迁移 — 局限性与技术债清单

> **文档角色**：这份文档是**当前迁移不完美之处的完整账本**。
> 写作时间：2026-04-17（batch 31 / 阶段 4 完成后）。
>
> **不是**日常看的，是合并回 LeavesMC 上游前做最后一轮清理的检查清单。
> 每一项给出：影响、风险、能否修、如何修、预估工作量、推荐优先级。

---

## 零、总览：当前迁移是 100% 完整的吗？

**不是**。通过以下 4 个维度评估：

| 维度 | 完成度 | 说明 |
|---|---|---|
| Patch rebase | **170/173 = 98%** | 5 个 obsoleted（Paper upstream 吸收），剩下都 rebase 完成 |
| 编译 | **100%**（0 errors） | ✅ |
| 功能保真 | **~95%** | 有 2 处明确的功能降级 + 若干"没验证但理论上等价"的改动 |
| 运行时验证 | **0%** | 从未跑过 `createMojmapLeavesclipJar` 或启动测试 |

本文档梳理功能保真和运行时风险层面的 **17 个具体项**，按"必须处理 → PR 前处理 → 可忽略"分级。

---

## 一、🔴 功能性降级（确定存在，需修复）

### 1.1 REI 客户端看不到"箭/地图"合成展示

**现象**：REI (Roughly Enough Items) 插件列出 crafting recipe 时，不会再有 tipped arrow（各种箭）和 map cloning（地图复制）的合成预览。

**原因**：Paper 26.1 删除了 `TippedArrowRecipe` 和 `MapCloningRecipe` 两个特殊类 —— 它们现在都是普通 `CustomRecipe` instance，只能通过 recipe 的 `Identifier`（如 `minecraft:tipped_arrow`）区分。Agent 在 batch 31 里直接删掉了这两个 `case`（为了避免 switch case dominance 错误），但替代的"emit fillers once after the loop" **只留了注释 TODO、没实现**。

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/protocol/rei/REIServerProtocol.java:142`

**修复思路**：
1. `Display.ofTippedArrowRecipe` / `Display.ofMapCloningRecipe` 本身仍然存在且签名兼容（接收 `RecipeHolder<CustomRecipe>`）
2. 在 `recipeMap.byType(RecipeType.CRAFTING).forEach(...)` 循环**后**，单独遍历一次：
   ```java
   recipeMap.byType(RecipeType.CRAFTING).forEach(holder -> {
       if (holder.value() instanceof CustomRecipe && holder.id().identifier().toString().equals("minecraft:tipped_arrow")) {
           builder.addAll(Display.ofTippedArrowRecipe((RecipeHolder) holder));
       }
   });
   ```
3. 或者查 recipe registry key 做精确匹配（更稳健）

**预估工作量**：30 分钟（含测试）
**优先级**：**合并 PR 前必须修**
**风险**：客户端 REI 插件用户体验下降，但服务器功能正常

---

### 1.2 ServuxHudDataProtocol 的 `spawnChunkRadius` 已被注释掉

**现象**：Servux HUD 协议里"服务器 spawn chunk 半径"字段不再发送给客户端。

**原因**：1.21.9 删除了 `GameRules.RULE_SPAWN_CHUNK_RADIUS`。代码里有预先的 TODO：

```java
// metadata.putInt("spawnChunkRadius", level.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));
// TODO: 1.21.9 removed spawn chunk, should we keep this?
```

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/protocol/servux/ServuxHudDataProtocol.java:105,173`

**修复思路**：
- 选项 A：如果客户端 Servux mod 不再读这个字段，直接删除相关注释 + 协议版本号保持不变
- 选项 B：如果客户端还读，查 1.21.9+ 上游 servux mod 怎么处理，对齐实现
- 这个 TODO 不是我们引入的，是 Leaves upstream 已有

**预估工作量**：20 分钟（查上游 servux mod 行为）+ 可能 15 分钟改动
**优先级**：中（协议兼容性，非阻塞启动）
**风险**：MiniHUD 等客户端可能看不到 spawn chunk 信息

---

## 二、🟡 可疑行为 / 需要运行时验证

### 2.1 ServerBot 的 `setClientLoaded(true)` 变 no-op

**现象**：bot 构造时不再通知系统"客户端已加载"。

**原因**：Paper 26.1 把 `clientLoaded` 状态从 `ServerPlayer` 移到了 `ServerGamePacketListenerImpl`（即 connection 上）。Bot 没有真实 connection，无法直接调用新的 `markClientLoaded(boolean)`（且此方法 private）。

**可能后果**（需运行时验证）：
- `ServerPlayer.isInvulnerableTo(...)` 检查 `!this.connection.hasClientLoaded()` — bot 可能被判定为"未加载"从而**永久无敌**
- Entity tracker 可能认为 bot 客户端没准备好，不推送 packet
- 登录事件 / PlayerLoadedWorldEvent 可能永远不触发

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/bot/ServerBot.java:125`

**修复思路**（按风险/工作量排序）：
1. **反射 hack**（最简单）：在构造函数末尾反射设 `this.connection.clientLoadedTimeoutTimer = 0`，让下一次 tick 就 `markClientLoaded`
2. **为 bot 提供假 connection**：在 `ServerBot.connection` field 上挂一个特殊子类，该子类的 `hasClientLoaded()` 始终返回 true
3. **minecraft patch 改 ServerPlayer 基类**：给 `isInvulnerableTo` 等方法加 bot 分支（`if (this instanceof ServerBot) return ...`）
4. **加 Leaves AT**：把 `markClientLoaded(boolean)` 在 AT 里提升为 public，然后 bot 构造调一下

**预估工作量**：
- 方案 1：15 分钟
- 方案 2：1 小时
- 方案 3：30 分钟但侵入性强
- 方案 4：15 分钟但需要改 AT 机制

**优先级**：**必须验证**（阶段 5 第一个要检查的点）
**风险**：bot 功能可能严重受损

---

### 2.2 BotStatsCounter 的 `parseLocal` override 被完全删除

**现象**：bot 的统计计数器不再 override `parseLocal` 方法。

**原因**：Paper 26.1 `ServerStatsCounter` 已经没有 `parseLocal(DataFixer, String)` 方法。旧代码里的空 override 本意是"避免从磁盘加载 bot 统计"，因为 bot 的 UNKOWN_FILE 是假路径。

**可能后果**：
- 26.1 的新 `parse(DataFixer, JsonElement)` 方法（如果有默认实现）可能尝试读 UNKOWN_FILE 路径
- 或者 bot 根本不会触发加载流程（因为 Path 不存在）

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/bot/BotStatsCounter.java:28`

**修复思路**：
1. 启动时观察日志是否有 `Failed to parse stats file: BOT_STATS_REMOVE_THIS` 之类的异常
2. 如果有，override `parse(DataFixer, JsonElement)` 为空实现

**预估工作量**：5-10 分钟（一旦验证出现异常）
**优先级**：运行时验证后再决定
**风险**：可能产生每个 bot 登入时一条 WARN 日志，功能应不受影响

---

### 2.3 Replay Mod Recorder 的 `forceDayTime` 语义可能偏移

**现象**：batch 31 把 `new ClientboundSetTimePacket(packet1.dayTime(), forceDayTime, false)`（旧 3 参）重写成：

```java
new ClientboundSetTimePacket(
    packet1.gameTime(),
    Util.mapValues(packet1.clockUpdates(),
        state -> new ClockNetworkState(forceDayTime, 0.0F, 0.0F))
);
```

**可能的语义错位**：
- `forceDayTime` 是"想显示的 time-of-day"（0-24000 范围）
- `ClockNetworkState.totalTicks` 是"时钟的绝对 tick 值"（可能远超 24000）
- 直接赋值 `totalTicks = forceDayTime`，rate=0（冻结），**可能**让客户端显示的时间戳错位（显示 tick=0 附近）
- 不同 world（overworld/nether/end）可能有不同 clock，全部覆盖可能让末地/下界时间显示异常

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/replay/Recorder.java:204`

**修复思路**：
1. 运行时录制一段视频，回放看时间戳是否正常
2. 如有问题，研究 `net.minecraft.world.clock.WorldClock` 的 `packNetworkState` 方法，模仿其计算逻辑
3. 最坏情况：为每个 world 的 clock 构造 state 时用 `forceDayTime + (totalTicks - totalTicks % DAY_LENGTH)`

**预估工作量**：30 分钟-2 小时（取决于需要对齐的程度）
**优先级**：中（仅影响 replay 功能）
**风险**：Replay 回放时间显示错乱

---

### 2.4 LeavesMinecraftSessionService 用自建 MinecraftClient

**现象**：Leaves 原代码直接访问父类 `YggdrasilMinecraftSessionService.client` 字段；26.1 此字段 private。batch 31 改成 Leaves 子类自己 `MinecraftClient.unauthenticated(proxy)` 创建。

**理论等价性**：`MinecraftClient.unauthenticated(Proxy)` 正是父类构造 client 的方式，语义应当 100% 等价。

**风险点**：如果父类构造时对 client 做了额外配置（例如添加 header、设置 timeout），子类的独立 client 不会继承这些配置。查了 authlib 7.0.63 源码确认父类没有额外配置，所以**理论上等价**。

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/profile/LeavesMinecraftSessionService.java:76`

**验证**：运行时测试"额外 Yggdrasil 服务"功能（多重认证）是否正常。

**预估工作量**：5 分钟验证
**优先级**：低
**风险**：极低

---

### 2.5 0143 Replay-Mod-API patch 的 photographer 可能覆盖 real player

**现象**：`placeNewPhotographer` 把 photographer 加到 `this.playersByName`/`this.playersByUUID`。如果 photographer 名字和某个 real player 相同，会覆盖后者。

**原因**：这是原 Leaves 1.21.10 的设计，rebase 未改。Photographer 名字通常来自录制用户，本身就已经在线 —— 该"覆盖"可能导致 `server.getPlayerByName("xxx")` 返回 photographer 而不是真人。

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/replay/ServerPhotographer.java` 链路（以及 `PlayerList.placeNewPhotographer`）

**修复思路**：可能需要为 photographer 引入独立的 name/UUID 映射（例如加 `_recorder` 后缀），或者干脆不加入 `playersByName`/`playersByUUID`。

**预估工作量**：1-2 小时
**优先级**：中等（Leaves upstream 原设计，不算我们的问题，但已知可疑）
**风险**：录制期间别的插件按名字查玩家会返回错误对象

---

### 2.6 0142 Lithium-Sleeping-Block-Entity 的 `PatchedDataComponentMap.ensureMapOwnership` 过度通知

**现象**：`ensureMapOwnership` 里无条件 `subscriber.lithium$notify(...)`；原 Lithium mixin 只在 `copyOnWrite=true` 时通知。

**影响**：性能下降（可能显著 —— 每次 hopper 尝试移动 item 都会触发），但不是正确性 bug。

**影响文件**：`leaves-server/minecraft-patches/features/0142-Lithium-Sleeping-Block-Entity.patch` L66-68

**修复思路**：把 `if (sleepingBlockEntity && subscriber != null)` 条件合并 `this.copyOnWrite` 判断：
```java
if (sleepingBlockEntity && subscriber != null && this.copyOnWrite) {
    subscriber.lithium$notify(...);
}
```

**预估工作量**：5 分钟
**优先级**：PR 前修（忠实复刻原 Leaves 瑕疵）
**风险**：无正确性风险，性能 regression

---

## 三、🟡 Rebase 遗留的技术债（不影响功能，但 PR 前应清理）

### 3.1 4 个"Fix-" 补丁应折叠进对应原 patch

**当前状态**：rebase 过程中新加了 4 个"补丁的补丁"：

| Fix patch | 折叠目标 |
|---|---|
| `0122-Fix-latent-compile-errors-in-rebased-patches` | 分散到各原 minecraft patch |
| `0136-Fix-PCA-addListener-and-REI-display-API-mismatch` | `0129-PCA-sync-protocol` + `0133-Support-REI-protocol` |
| `0138-Fix-Fakeplayer-getGameProfile-API` | `0137-Leaves-Fakeplayer` |
| `0140-Fix-Nullable-annotation-on-IRegionFile` | `0139-More-Region-Format-Support` |

**为什么保留**：rebase 过程中保留独立便于回溯 / debug。

**修复思路**：
1. 进入 `leaves-server/src/minecraft/java` 的 paperweight git 工作区
2. `git rebase -i` 把 Fix commit `squash` 或 `fixup` 到目标 commit
3. `git format-patch` 导出新补丁集
4. 删除独立 Fix patch

**预估工作量**：30-60 分钟
**优先级**：合并回 LeavesMC 上游前必须做
**风险**：低（纯组织问题）

---

### 3.2 `features/` 的两个 `0035-*.patch` 重复编号

**当前状态**：
- `0035-Modify-end-void-rings-generation.patch`
- `0035-Skip-cloning-advancement-criteria.patch`

`applyAllPatches` 按字母序执行未报错，但命名不规范。

**修复思路**：把后者改成最大编号 +1（例如 `0145-Skip-cloning-advancement-criteria.patch`），或者重命名两个让它们顺序正确。

**预估工作量**：5 分钟
**优先级**：低
**风险**：无

---

### 3.3 保留但失效的 LeavesConfig 字段

**当前状态**：`LeavesConfig.java` 保留这些 obsolete 字段以兼容老 `leaves.yml`：

| 字段 | 状态 |
|---|---|
| `LeavesConfig.fix.vanillaEndVoidRings` | **仍生效**（batch 20 恢复） |
| `LeavesConfig.performance.checkSpookySeasonOnceAnHour` | 失效（Paper 26.1 已简化 `isHalloween`） |
| `LeavesConfig.performance.cacheClimbCheck` | 失效（Paper 26.1 用 O(1) 快速路径） |
| `LeavesConfig.fix.vanillaFluidPushing` | 失效（Paper 26.1 重写了流体推送） |

**修复思路**：由 Leaves 上游 maintainer 决定：
- A. 加 `@Deprecated` 注释但保留（向后兼容）
- B. 完全删除（下个大版本）
- C. 针对 `vanillaFluidPushing` 基于新 `EntityFluidInteraction` 重写为新 patch

**预估工作量**：10 分钟（A/B）；1-2 小时（C）
**优先级**：PR 前由 maintainer 决策
**风险**：如果删除，用户升级时 `leaves.yml` 里残留配置会"静默忽略"

---

### 3.4 0143 里的 `// Leaves stop` typo + CopyOnWriteArrayList 缺泛型

**当前状态**：忠实复刻原 Leaves 1.21.10 代码，但：
- `// Leaves stop - replay mod api` 应为 `// Leaves end`（原 patch 笔误）
- `new CopyOnWriteArrayList()` 缺泛型参数（unchecked warning）

**影响文件**：`leaves-server/minecraft-patches/features/0143-Replay-Mod-API.patch` L327, L511

**修复思路**：直接在 patch 里 Edit 这两行。

**预估工作量**：2 分钟
**优先级**：合并回上游时顺手修
**风险**：无

---

### 3.5 `Level.lithium$getLoadedExistingBlockEntity` 缺 `@Nullable` 注解

**当前状态**：方法能返回 null 但签名没标注，jspecify 警告（不是 error）。

**影响文件**：`leaves-server/minecraft-patches/features/0142-Lithium-Sleeping-Block-Entity.patch` L494（Level.java 新增方法）

**修复思路**：
```java
public @Nullable BlockEntity lithium$getLoadedExistingBlockEntity(BlockPos pos) {
```

**预估工作量**：1 分钟
**优先级**：合并回上游时顺手修
**风险**：无

---

### 3.6 `PaperPluginMeta.authors` 被 Leaves 从 `private` 改成 `protected`

**当前状态**：batch 30 改了 Paper 的 `PaperPluginMeta.authors` 字段可见性为 `protected`，以便 `LeavesPluginMeta` 子类可访问并配置。

**风险点**：如果 Paper upstream 改回 private 或重构这个类，Leaves 会断。

**修复思路**：
- 长期方案：把这个改动作为 Paper PR 提交上游，或通过 AT（access transformer）处理而不是直接改 Paper patch
- 短期：保持现状，在 Leaves PR 说明里标注

**预估工作量**：PR 上游 1-2 小时；AT 方案 15 分钟
**优先级**：PR 前考虑
**风险**：未来 Paper 升级时断

---

## 四、🟢 环境 / 工具链瑕疵（不影响代码）

### 4.1 Finder 重复文件每次 applyAllPatches 都重现

**现象**：paper-server 内部 git 的 `0001-Build-changes` commit tracked 了 28 个 `* 2.java` / `* 3.java`（macOS Finder 在 sync 时产生的副本）。每次 `rm -rf paper-server && ./gradlew applyAllPatches` 都会把它们还原。

**当前 workaround**：每次 compile 前跑
```bash
find paper-server leaves-server -name "* [0-9].java" -delete
```

**根本修复**：
1. 在 paper-server git 工作区找到 `Build changes` commit
2. `git rebase -i <that commit>^` + `edit` 进入
3. `git rm "* 2.java" "* 3.java"`
4. `git commit --amend`
5. 从 workspace format-patch 出来覆盖 `leaves-server/paper-patches/features/0001-Build-changes.patch`

**预估工作量**：30 分钟
**优先级**：PR 前必须修（Linux CI 不应依赖 macOS workaround）
**风险**：低（修坏了可以 revert）

---

### 4.2 JDK 25 Vector API 在 incubator 还是 preview 未验证

**当前状态**：`leaves-server/build.gradle.kts.patch` 里保留了 `--add-modules=jdk.incubator.vector`。JDK 25 里 Vector API 是否还在 incubator 不确定。

**修复思路**：
- 跑一次 `./gradlew compileJava` 看是否有 warning 说 `jdk.incubator.vector` 不存在
- 如果需要改，可能是 `--add-modules=jdk.vector`（stable）或 `--enable-preview --add-modules=jdk.vector`（preview）

**预估工作量**：10 分钟
**优先级**：阶段 5 构建 jar 前验证
**风险**：jar 构建可能失败

---

### 4.3 leavesclip 3.0.10 vs Paper 26.1 bundler 格式

**当前状态**：leavesclip 是 paperclip 的 fork，Leaves 用它打包最终 jar。Paper 26.1 可能改了 bundler 格式（新的 `META-INF/versions` 结构等），leavesclip 3.0.10 可能不识别。

**修复思路**：
- 跑 `./gradlew createMojmapLeavesclipJar` 看是否报错
- 如果报错，可能需要升级 leavesclip（需要同时 rebase leavesclip 到上游最新）

**预估工作量**：未知（0-8 小时，取决于上游变化大小）
**优先级**：阶段 5 第一步
**风险**：可能需要额外一轮 leavesclip rebase

---

## 五、🟢 文档 / CI 层面瑕疵

### 5.1 CI 在 Linux 上跑时没有 macOS 污染问题

**当前状态**：GitHub Actions 的 `test.yml` 会在 Linux runner 上跑 `applyAllPatches`。Linux 没有 macOS Finder，不会产生 `* 2.java` 文件。但 paper-server 内部 git 的 `Build changes` commit 带了这 28 个文件 —— 它们会被 applyAllPatches 还原出来，然后 compile 失败。

**可能 CI 现状**：我们本地一直 workaround，**CI 可能从未成功过 compileJava**。阶段 5 启动前必须验证。

**修复思路**：见 §4.1

**预估工作量**：同 §4.1
**优先级**：阶段 5 启动前
**风险**：CI 可能一直在红（我没查过）

---

### 5.2 PROTOCOL_MOD_AUDIT.md 是静态审计，未经运行时验证

**当前状态**：batch 24 时通过 `gh api` 对比了上游 mod 仓库的 `PROTOCOL_VERSION` 常量，结论"全绿"。但从未用真实客户端连接测试。

**修复思路**：阶段 5 运行时测试阶段，按 [`PROTOCOL_MOD_AUDIT.md` §四](./PROTOCOL_MOD_AUDIT.md) 的验证清单逐一对接。

**预估工作量**：2-4 小时（手动测试每个 mod）
**优先级**：阶段 5
**风险**：可能发现实际不兼容，需回炉

---

### 5.3 仅 macOS 验证过 applyAllPatches

**当前状态**：所有 rebase 验证都在一台 MacBook Air 上做的。Linux 生产环境未测试。

**修复思路**：push 后观察 GitHub Actions `test.yml` 结果；必要时借 Linux 环境本地跑一遍。

**预估工作量**：1 小时
**优先级**：PR 前
**风险**：低（paperweight 本身跨平台）

---

## 六、清单汇总（按优先级）

### 🚨 必须在启动前处理（阻塞阶段 5）
- §1.1 REI tipped-arrow / map-cloning filler 修复（30 min）
- §2.1 ServerBot setClientLoaded 运行时验证 + 可能修复（15 min–1 h）
- §4.1 paper-server Finder 污染根治（30 min）
- §4.2 JDK 25 Vector API 验证（10 min）
- §4.3 leavesclip 与 Paper 26.1 bundler 兼容性（未知）

### ⚠️ PR 前应处理（技术债清理）
- §3.1 4 个 Fix 补丁折叠回对应原 patch（30–60 min）
- §3.4 0143 typo + 泛型（2 min）
- §3.5 `@Nullable` 漏标（1 min）
- §3.2 0035 重复编号（5 min）
- §3.3 Obsoleted config 字段决策（由 maintainer）
- §3.6 `PaperPluginMeta.authors` 修改作为 PR 提上游（1–2 h）
- §2.6 `PatchedDataComponentMap.ensureMapOwnership` 过度通知（5 min）

### 🔍 运行时验证后决定
- §1.2 Servux spawnChunkRadius（20+ min）
- §2.2 BotStatsCounter 可能日志噪音（5–10 min）
- §2.3 Recorder forceDayTime 时间显示（30 min–2 h）
- §2.4 LeavesMinecraftSessionService 额外认证（5 min）
- §2.5 Photographer 覆盖 real player（1–2 h）
- §5.1 CI 状态（同 §4.1）
- §5.2 协议 mod 运行时对接（2–4 h）
- §5.3 Linux 验证（1 h）

---

## 七、一句话结论

**迁移完成度**：**编译层面 100%，功能保真 ~95%**。

真正的"完整"需要：
1. 启动测试（阶段 5）
2. 上述 5 个"🚨 必须在启动前处理"项
3. 协议 mod 运行时对接

在那之前，我们**能编译成功**但**不能保证运行正确**。

---

_本文档在阶段 5 完成后应大面积缩减 —— 验证过的项移到"已解决"，真正的技术债再提 issue 追踪。_
