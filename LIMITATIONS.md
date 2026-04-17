# Leaves 26.1.2 迁移 — 局限性与技术债清单

> **文档角色**：这份文档是**当前迁移不完美之处的完整账本**。
> 写作时间：2026-04-17（batch 31 / 阶段 4 完成后）。
> 最近更新：2026-04-17（batch 36 根据外部 bug audit 修复 3 项 HIGH：§2.2 BotStatsCounter parse override、§2.5 photographer 移除殃及、§3.7 BuddingAmethyst `@Override` 恢复）。
>
> **不是**日常看的，是合并回 LeavesMC 上游前做最后一轮清理的检查清单。
> 每一项给出：影响、风险、能否修、如何修、预估工作量、推荐优先级。
>
> **状态标签**：
> - ✅ = 已修复（日期在条目开头）
> - ⏳ = 待修
> - 💡 = 需运行时验证才能决定

---

## 零、总览：当前迁移是 100% 完整的吗？

**不是**。通过以下 4 个维度评估：

| 维度 | 完成度 | 说明 |
|---|---|---|
| Patch rebase | **170/173 = 98%** | 5 个 obsoleted（Paper upstream 吸收），剩下都 rebase 完成 |
| 编译 | **100%**（0 errors） | ✅ |
| 功能保真 | **~95%** | 有 2 处明确的功能降级 + 若干"没验证但理论上等价"的改动 |
| 运行时验证 | **~10%** | jar 构建 + CI 绿 + 静态日志测试通过；功能级运行时未验证 |

本文档梳理功能保真和运行时风险层面的 **17 个具体项**，按"必须处理 → PR 前处理 → 可忽略"分级。

---

## 一、🔴 功能性降级（确定存在，需修复）

### 1.1 ✅ REI 客户端"箭/地图"合成展示（2026-04-17 batch 33 已修复）

**修复方案**：定位到真正根因——Paper 26.1 里这两个 recipe 换了底层实现：
- `tipped_arrow` 现在是 `ImbueRecipe`（extends `NormalCraftingRecipe`，serializer 注册为 `crafting_imbue`）
- `map_cloning` 现在是 `TransmuteRecipe`（复用 generic transmute 机制，在 `map_cloning` group 下注册）

这两个类 **都不是 CustomRecipe**，所以 batch 31 的 "CustomRecipe" case 分支压根就不可能匹配到它们。

修改：
1. 在 `REIServerProtocol.reloadRecipe()` 的 switch 里加两个新 case
   - `case TransmuteRecipe ignored when "minecraft:map_cloning".equals(holder.id().identifier().toString())`
     —— 放在 generic `case TransmuteRecipe` 之前，优先走"filled map + empty map → 2 filled maps"的 ShapelessDisplay
   - `case ImbueRecipe ignored` —— ImbueRecipe 目前只用于 tipped_arrow，按类型匹配即可。委托给 `Display.ofTippedArrowRecipe` 遍历所有 potion 生成逐个 display
2. `Display.ofTippedArrowRecipe` / `Display.ofMapCloningRecipe` 的形参类型从 `RecipeHolder<CustomRecipe>` 放宽为 `RecipeHolder<?>`（方法内只用到了 `recipeHolder.id().identifier()`，类型参数无实质作用）
3. 删掉 `Display.java` 不再需要的 `import net.minecraft.world.item.crafting.CustomRecipe`

**改动文件**：
- `leaves-server/src/main/java/org/leavesmc/leaves/protocol/rei/REIServerProtocol.java`
- `leaves-server/src/main/java/org/leavesmc/leaves/protocol/rei/display/Display.java`

**验证**：阶段 5 运行时测试 —— 客户端 REI 界面搜索 "tipped_arrow" 和 "map" 能看到合成预览。

---

### 1.2 ⏳ ServuxHudDataProtocol 的 `spawnChunkRadius` 已被注释掉（2026-04-17 batch 35 注释已清理）

**现象**：Servux HUD 协议里"服务器 spawn chunk 半径"字段不再发送给客户端。

**原因**：1.21.9 删除了 `GameRules.RULE_SPAWN_CHUNK_RADIUS`。

**当前状态**：batch 35 把 batch 之前留下的 `TODO: 1.21.9 removed spawn chunk` 注释整理成明确的说明，标注"等待运行时验证客户端 mod 行为"。代码本身不变（字段仍然不发送）。

**影响文件**：`leaves-server/src/main/java/org/leavesmc/leaves/protocol/servux/ServuxHudDataProtocol.java:105,173`

**剩余决策**（需阶段 5 运行时验证后决定）：
- 选项 A：MiniHUD/Servux 客户端 mod 已不读这个字段 → 直接删除 putInt 调用即可
- 选项 B：客户端还读 → 改用 `level.getServer().getServerSpawnRadius()` 之类的 26.1 等价 API

**预估工作量**：5 分钟（A）/ 15 分钟（B）
**优先级**：低（协议兼容性，客户端通常宽容缺失字段）
**风险**：MiniHUD 等客户端可能看不到 spawn chunk 半径信息

---

## 二、🟡 可疑行为 / 需要运行时验证

### 2.1 ✅ ServerBot 的 `setClientLoaded(true)` 变 no-op（2026-04-17 batch 32 已修复）

**现象（已修复前）**：bot 被玩家攻击无反应，`ServerPlayer.isInvulnerableTo` 检查 `!this.connection.hasClientLoaded()` 永远为 true。

**修复方案**：采用了方案 2 的变体。在 `ServerBotPacketListenerImpl` 里 override `hasClientLoaded()` 直接返回 `true`：

```java
@Override
public boolean hasClientLoaded() {
    return true;
}
```

**修改文件**：`leaves-server/src/main/java/org/leavesmc/leaves/bot/ServerBotPacketListenerImpl.java`

**为什么选这个方案**：bot 的 connection 已经是 `ServerBotPacketListenerImpl`（自定义子类），而且 `hasClientLoaded()` 是 public，override 无需反射也无需 AT。最干净。

**验证**：阶段 5 启动测试时攻击 bot 能造成伤害

---

### 2.2 ✅ BotStatsCounter 的 `parseLocal` override 被完全删除（2026-04-17 batch 36 已修复）

**现象（修复前）**：bot 的统计计数器不再 override `parseLocal` 方法（Paper 26.1 把 `parseLocal(DataFixer, String)` 整个删了）。

**修复方案**：在 `BotStatsCounter.java` 补一个对新方法的空 override：

```java
@Override
public void parse(@NotNull DataFixer fixerUpper, @NotNull JsonElement element) {
}
```

这样就算未来 CWD 里意外出现 `BOT_STATS_REMOVE_THIS` 文件，父类构造器也不会把真实磁盘内容读进 bot 的 stats。属于防御性 override，与原先 `save()` / `setValue()` / `getValue()` 的 stub 策略一致。

**修改文件**：`leaves-server/src/main/java/org/leavesmc/leaves/bot/BotStatsCounter.java:32-34`

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

### 2.5 ⚠️ 0143 Replay-Mod-API patch 的 photographer 可能覆盖 real player（2026-04-17 batch 36 修复了"移除殃及"子问题）

**现象**：`placeNewPhotographer` 把 photographer 加到 `this.playersByName`/`this.playersByUUID`。如果 photographer 名字和某个 real player 相同，会覆盖后者。更严重的是，`removePhotographer` 原本**无条件**按名字删除条目——若同名真实玩家之前的映射已被 photographer 覆盖，photographer 退场时会把真实玩家的名字条目一并清掉，导致此后 `server.getPlayerByName("xxx")` 返回 null 直到真人重登。

**batch 36 修复**：在 `removePhotographer` 里把名字删除加上与 UUID 分支对齐的 `==` 守卫：

```java
final String nameKey = entityplayer.getScoreboardName().toLowerCase(java.util.Locale.ROOT);
if (this.playersByName.get(nameKey) == entityplayer) {
    this.playersByName.remove(nameKey);
}
```

这修复了"殃及真实玩家"的严重子问题。**但"photographer 在位期间仍覆盖真实玩家名字查找"是 Leaves 原 1.21.10 设计**，未改——需要 photographer 上线时给真实玩家让出位置，或为 photographer 启用独立名字空间。

**修改文件**：`leaves-server/minecraft-patches/features/0144-Replay-Mod-API.patch`（对应 `PlayerList.removePhotographer`）

**剩余工作**（中期）：重新设计 photographer 是否该出现在 `playersByName`（可能让其完全不参与名字查找）。

**预估剩余工作量**：1-2 小时
**优先级**：中（生产环境里"photographer 与真实玩家同名"概率低，但录制自己的用户会遇到）

---

### 2.6 ✅ 0142 `PatchedDataComponentMap.ensureMapOwnership` 过度通知（2026-04-17 batch 32 已修复）

**修复**：在 0142 patch 里把条件改成 `sleepingBlockEntity && this.copyOnWrite && subscriber != null` —— 只在真正发生 copy 时通知 subscriber。与原 Lithium mixin 语义对齐。

---

## 三、🟡 Rebase 遗留的技术债（不影响功能，但 PR 前应清理）

### 3.1 ⚠️ 4 个"Fix-" 补丁折叠尝试失败，决定保留独立形态（2026-04-17 batch 35）

**当前状态**：rebase 过程中新加了 4 个"补丁的补丁"，由 batch 35 尝试 squash 失败：

| Fix patch | 折叠目标 | batch 35 结果 |
|---|---|---|
| `0122-Fix-latent-compile-errors-in-rebased-patches` | 分散到各原 minecraft patch | 未尝试（拆分成本高） |
| `0136-Fix-PCA-addListener-and-REI-display-API-mismatch` | `0129-PCA-sync-protocol` + `0133-Support-REI-protocol` | 未尝试（跨 2 个目标 commit） |
| `0138-Fix-Fakeplayer-getGameProfile-API` | `0137-Leaves-Fakeplayer` | 试做（squash 成功）→ 撤回 |
| `0140-Fix-Nullable-annotation-on-IRegionFile` | `0139-More-Region-Format-Support` | 试做（squash 成功）→ 撤回 |

**batch 35 实验结果**：在 paperweight workspace 里 `git rebase -i 2d26c07` + `GIT_SEQUENCE_EDITOR` sed 把两个 fixup commit 合并进目标 commit，然后 `git format-patch --zero-commit --full-index` 重新导出 143 个 patch。但**重新 `applyAllPatches` 失败**：因为 squash 改变了 patch 的 blob hash，且后续所有 patch 的 `index <hash>..<hash>` 行都重新计算，3-way merge 在 0054 等多个 patch 失败级联。

**结论**：squash 操作虽然 commit 历史看起来"更干净"，但破坏了与 Paper upstream 的 blob-hash 连续性，导致 patch 无法 clean apply。**接受现状，保留 4 个 Fix patch 独立**。

**替代方案**（向 LeavesMC 上游 PR 时）：
- A. 由 maintainer 在 review 阶段决定是否需要 squash。如果决定 squash，那就把所有受影响的 patch 重新解决冲突（30-60 min 手动工作）
- B. 直接 PR 145 个 patch 的当前形态，把 4 个 Fix patch 当作"修复历史 commit"接受

**优先级**：低（不影响功能，纯历史美观度）
**风险**：上游 reviewer 可能要求 squash，届时按方案 A 处理

---

### 3.2 ✅ `features/` 的两个 `0035-*.patch` 重复编号（2026-04-17 batch 34 顺带修复）

batch 34 做 Paper upstream 升级时 `git format-patch` 重新导出整个 patch 集，两个 0035 自动被 renumber 成 `0035-Modify-end-void-rings-generation.patch` + `0036-Skip-cloning-advancement-criteria.patch`，0036 之后整段向后挪一位。现在 145 个 patch 编号 0001-0145 全部唯一。

---

### 3.3 ✅ 保留但失效的 LeavesConfig 字段（2026-04-17 batch 35 加 `@Deprecated`）

**当前状态**：`LeavesConfig.java` 保留这些 obsolete 字段以兼容老 `leaves.yml`：

| 字段 | 状态 |
|---|---|
| `LeavesConfig.fix.vanillaEndVoidRings` | **仍生效**（batch 20 恢复） |
| `LeavesConfig.performance.checkSpookySeasonOnceAnHour` | ⚠️ `@Deprecated(forRemoval = true, since = "26.1.2")` —— Paper 26.1 已简化 `isHalloween` |
| `LeavesConfig.performance.cacheClimbCheck` | ⚠️ `@Deprecated(forRemoval = true, since = "26.1.2")` —— Paper 26.1 用 O(1) 快速路径 |
| `LeavesConfig.fix.vanillaFluidPushing` | ⚠️ `@Deprecated(forRemoval = true, since = "26.1.2")` —— Paper 26.1 重写了流体推送 |

**batch 35 选择方案 A**：加 `@Deprecated(forRemoval = true, since = "26.1.2")` 注释，每个字段附带 Javadoc 解释失效原因。这样：
- 老 `leaves.yml` 仍可加载，不会报错
- IDE 在使用方会显示删除线警告
- 下一个大版本（27.0+）可以彻底删除

**剩余工作**（不阻塞 PR）：
- C. `vanillaFluidPushing` 如果用户强烈需要，可基于新 `EntityFluidInteraction` API 重写为新 patch（1-2 h）

**改动文件**：`leaves-server/src/main/java/org/leavesmc/leaves/LeavesConfig.java:451,830,1316`

---

### 3.4 ✅ 0143 里的 `// Leaves stop` typo + CopyOnWriteArrayList 缺泛型（2026-04-17 batch 32 已修复）

改为 `// Leaves end - replay mod api` 和 `new CopyOnWriteArrayList<>()`。

---

### 3.5 ✅ `Level.lithium$getLoadedExistingBlockEntity` 缺 `@Nullable` 注解（2026-04-17 batch 32 已修复）

改为 `public @org.jspecify.annotations.Nullable BlockEntity lithium$getLoadedExistingBlockEntity(BlockPos pos)`。

---

### 3.7 ✅ `BuddingAmethystBlock.getResetPushReaction()` 丢失 `@Override`（2026-04-17 batch 36 已修复）

**现象（修复前）**：0123 Fix-latent-compile-errors patch 的一个 hunk 把 `BuddingAmethystBlock.getResetPushReaction()` 上的 `@Override` 删掉了。代码仍能编译运行（0124 Leaves-Utils 在 `Block` 基类加了同名方法，签名一致），但丢失了"覆盖不匹配"的编译期契约检查。

**batch 36 修复**：删除 0123 patch 里针对 `BuddingAmethystBlock` 的 hunk，恢复 0039 原 patch 里的 `@Override` 注解。`applyAllPatches` + `compileJava` 双绿。

**为什么这个 hunk 原本存在**：rebase 过程中一度有 "method does not override anything" 的 javac 错误（推测在某一中间态基类方法签名暂时不匹配）；重新排序 patch 或修复基类后，删除 `@Override` 成为"看起来能跑"的权宜之计。现在所有 patch 排序稳定，基类方法签名一致，`@Override` 可以安全恢复。

**修改文件**：`leaves-server/minecraft-patches/features/0123-Fix-latent-compile-errors-in-rebased-patches.patch`（删除最后一个 hunk）

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

### 4.1 ✅ Finder 重复文件污染（2026-04-17 batch 32 已修复）

**修复**：用 Python 脚本从 `0001-Build-changes.patch` 里移除了 28 个 `* [0-9].java` 的 "new file" diff 条目。patch 从 1.3MB 缩到 99KB。应用后 paper-server 不再出现 Finder 垃圾。

对于 paper-api 的同类污染（20 个未 tracked 的 `* [0-9].java`）：这些本来就只在 macOS 本地出现，Linux CI 不会生成，`find -delete` 即可，无需改 patch。

---

### 4.2 ✅ JDK 25 Vector API（2026-04-17 batch 32 验证）

jar 构建成功 = `--add-modules=jdk.incubator.vector` flag 在 JDK 25 下仍然有效。无需改动。

---

### 4.3 ✅ leavesclip vs Paper 26.1 bundler 格式（2026-04-17 batch 32 验证）

`./gradlew :leaves-server:createLeavesclipJar` 构建成功，产生 62.5MB 可启动 jar。leavesclip 3.0.10 与 Paper 26.1 bundler 格式兼容。无需升级。

---

## 五、🟢 文档 / CI 层面瑕疵

### 5.1 ✅ CI Linux 运行正常（2026-04-17 batch 32-34 验证）

batch 32 修复了 `0001-Build-changes.patch` 里的 28 个 Finder 污染条目 + CI task 名 + 产物文件名。batch 33、batch 34 的 CI run 都 `✓ build in ~4m` 绿通，artifact `leaves-26.1.2.jar` 自动上传。无遗留问题。

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
- ~~§1.1 REI tipped-arrow / map-cloning filler~~ ✅ batch 33 已修
- ~~§2.1 ServerBot setClientLoaded~~ ✅ batch 32 已修
- ~~§4.1 paper-server Finder 污染根治~~ ✅ batch 32 已修
- ~~§4.2 JDK 25 Vector API 验证~~ ✅ batch 32 验证（jar 构建成功）
- ~~§4.3 leavesclip 与 Paper 26.1 bundler 兼容性~~ ✅ batch 32 验证（60MB jar 构建成功）

**所有阻塞级项目已清空 ✅**

### ⚠️ PR 前应处理（技术债清理）
- ~~§3.1 4 个 Fix 补丁折叠回对应原 patch~~ ⚠️ batch 35 实验失败，决定保留独立形态（见 §3.1）
- ~~§3.3 Obsoleted config 字段决策~~ ✅ batch 35 加 `@Deprecated(forRemoval, since="26.1.2")`
- ~~§3.7 BuddingAmethyst `@Override` 恢复~~ ✅ batch 36 删除 0123 相关 hunk
- §3.6 `PaperPluginMeta.authors` 修改作为 PR 提上游（1–2 h）
- §2.4 LeavesMinecraftSessionService 额外认证验证（5 min，可能无需改动）
- Leaves 自有代码 **2** 处 `world.getName()` → `world.getKey()`（5 min，soft deprecation；语义上仍正确，仅 IDE 警告）。准确位置：`ListCommand.java:69`、`LeavesServerConfigProvider.java:99`

### 🔍 运行时验证后决定
- §1.2 Servux spawnChunkRadius（5-15 min，注释已清理）
- ~~§2.2 BotStatsCounter~~ ✅ batch 36 补 `parse(DataFixer, JsonElement)` 空 override
- §2.3 Recorder forceDayTime 时间显示（30 min–2 h）
- §2.5 Photographer 覆盖 real player 的"仍在位期间"部分（1–2 h，"移除殃及"子问题 batch 36 已修）
- §5.2 协议 mod 运行时对接（2–4 h）
- §5.3 Linux 验证（CI 已过，本地 Linux 运行时测试 1 h）

---

## 七、一句话结论（2026-04-17 更新）

**迁移完成度**：**编译层面 100%、jar 打包 100%、CI 100%、功能保真 ~98%**。

所有 🚨 阻塞项已清空。真正的"完整"剩下：
1. 阶段 5 运行时启动 + 功能验证（[`STAGE5_TEST_CHECKLIST.md`](./STAGE5_TEST_CHECKLIST.md) 剩余项）
2. 协议 mod 客户端对接验证
3. PR 前技术债清理（4 个 Fix 补丁折叠等）

在那之前，我们**能编译、能打包、能通过 CI**，但**运行时行为尚未人工验证**。

---

_本文档在阶段 5 完成后应大面积缩减 —— 验证过的项移到"已解决"，真正的技术债再提 issue 追踪。_
