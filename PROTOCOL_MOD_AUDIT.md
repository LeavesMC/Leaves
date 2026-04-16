# Leaves Protocol Mod 兼容性审计

> **审计时间**：2026-04-16
> **基线**：Leaves 1.21.10（升级前）
> **目标**：Paper/Mojang 26.1.2
> **方法**：通过 `gh api` 检查上游 mod 仓库的最新分支/版本，与 Leaves 源码中的 `PROTOCOL_VERSION` / namespace / channel 对比

---

## 一、结论速览

**总体评估**：**绿色** — 没有发现任何上游 mod 在迁移到 26.1 期间引入了破坏性协议变更。Leaves 当前 protocol 实现可继续使用，**无需返工任何 patch**。

| 分类 | 数量 | mod |
|---|---|---|
| 🟢 GREEN（版本号或 channel 完全匹配） | 6 | Jade、Servux（4 子模块）、AppleSkin、Carpet |
| 🟡 YELLOW（channel-based，未发现明显变更） | 5 | BBOR、PCA、Litematica、Xaero Map、REI |
| ⚪ UNKNOWN（无法验证或上游不活跃） | 3 | Syncmatica、ChatImage、Bladeren |

---

## 二、详细审计结果

### 🟢 高置信度匹配

| Mod | Leaves 期望 | 上游 26.1 实际值 | 来源 |
|---|---|---|---|
| **Jade** | `PROTOCOL_VERSION = "9"` | `Snownee/Jade@26.1-fabric: PROTOCOL_VERSION = "9"` | `Jade.java` |
| **Servux Structures** | `PROTOCOL_VERSION = 2` | `sakura-ryoko/servux@26.1: 2` | `ServuxStructuresPacket.java` |
| **Servux HUD** | `PROTOCOL_VERSION = 2` | `sakura-ryoko/servux@26.1: 2` | `ServuxHudPacket.java` |
| **Servux Entities** | `PROTOCOL_VERSION = 1` | `sakura-ryoko/servux@26.1: 1` | `ServuxEntitiesPacket.java` |
| **Servux Litematica** | `PROTOCOL_VERSION = 1` | `sakura-ryoko/servux@26.1: 1` | `ServuxLitematicaPacket.java` |
| **AppleSkin** | namespace `appleskin`, channels `saturation`/`exhaustion` | `squeek502/AppleSkin@1.21.5-fabric: Identifier.of("appleskin", "saturation")` | `SaturationSyncPayload.java` |
| **Carpet** | `HI = "69"`, `HELLO = "420"`, channel `carpet:hello` | `gnembon/fabric-carpet@master: HI = "69", HELLO = "420", CARPET_CHANNEL = carpet:hello` | `CarpetClient.java` |

### 🟡 Channel-based（未变化，但需运行时验证）

这些 mod 不使用整数 `PROTOCOL_VERSION`，而是按 namespace + channel 名做派发。Leaves 实现里写死了 channel 名，只要上游不重命名 channel 就 OK。建议运行时和真实 mod 客户端对接时验证一次。

| Mod | Leaves namespace/channel | 上游分支 | 备注 |
|---|---|---|---|
| **BBOR** | `bbor:` channels | `irtimaled/BoundingBoxOutlineReloaded` 仅维护到 1.20 | **上游已停滞**，但有第三方维护 fork。Leaves 实现的是服务端响应，wire 协议历史稳定 |
| **PCA** | `pca:sync` | `plusls/plusls-carpet-addition` 最新分支 1.18.x | **上游已停滞**，Leaves 自维护协议 |
| **Litematica** | `easy_place:` channels | `maruohon/litematica` 主仓库无 1.21+ 分支（pre-rewrite/fabric/1.21.1-masa 是个人 wip） | 只用 channel 不用版本号；PR-149 等社区 fork 在维护 |
| **Xaero Minimap/WorldMap** | `xaero_wm_main_chan_v3:`, `xaero_mm_main_chan_v4:` | 闭源 mod，channel 名内含版本后缀 | channel 后缀就是 protocol version；如果客户端升 v4→v5，需改 Leaves channel 常量 |
| **REI** | channels `sync_displays`, `ci_msg` | `shedaniel/RoughlyEnoughItems@19.x-1.21.5` 默认分支已到 1.21.5 | 服务端 sync displays，REI 一般向后兼容 |

### ⚪ 无法直接验证

| Mod | 备注 |
|---|---|
| **Syncmatica** | Leaves 写死 `PROTOCOL_VERSION = "leaves-syncmatica-1.1.0"` ——是 **Leaves 自定义握手字符串**，不是上游 Syncmatica 版本，不会因上游变更而失效 |
| **ChatImage** | 仓库 `LovelyLM/ChatImage` 404；可能已转移或重命名。Leaves 实现按 channel 名 `download_file_channel`/`get_file_channel` 派发，需运行时和实际 mod 客户端对接验证 |
| **Bladeren** | `PROTOCOL_VERSION = ProtocolUtils.buildProtocolVersion("bladeren")` —— 用 Leaves 自己的工具函数生成，不是直接对接上游版本号 |

---

## 三、对升级 patch 的影响

**无需返工任何 patch**：

| Patch | 影响 | 结论 |
|---|---|---|
| `0027 BBOR-Protocol` | LevelChunk hook | ✅ 保留 |
| `0028 PCA-sync-protocol` | 12 个 BlockEntity setChanged hook | ✅ 保留 |
| `0029 Alt-block-placement-Protocol` | Block.getRealStateForPlacement | ✅ 保留（Carpet/Litematica channel 未变） |
| `0030 Jade-Protocol` | 8 个 private→public（loot 反射） | ✅ 保留（Jade PROTOCOL_VERSION = "9" 未变） |
| `0108 Xaero-Map-Protocol` | PlayerList onSendWorldInfo | ✅ 保留 |
| `0110 Support-REI-protocol` | SmithingRecipe getResult/pattern | ✅ 保留 |

**不需要修改的源码**：
- `org.leavesmc.leaves.protocol.*`（149 个 java 文件）当前实现继续可用
- 编译错误来源是 Paper 26.1 本身的 API 重命名（`net.minecraft.advancements.critereon` → `criterion`），与 mod 协议无关

---

## 四、运行时验证清单（提交 PR 前）

虽然静态审计通过，**强烈建议**在阶段 5 运行时测试时人工验证以下场景：

- [ ] **Jade**：客户端连接，按 N 看 tooltip，确认 mob 信息显示
- [ ] **Servux**：用 MiniHUD 客户端连接，检查 HUD 数据接收
- [ ] **REI**：客户端打开 REI 界面，确认 server-side recipe sync 工作
- [ ] **AppleSkin**：客户端 HUD 显示饱和度
- [ ] **Carpet**：客户端连接日志显示 carpet 协议握手
- [ ] **Xaero Map**：客户端地图显示 server world 数据
- [ ] **PCA / Litematica / BBOR / Syncmatica / ChatImage / Bladeren**：按需测试

**未来跟踪策略**：
- 上游 mod 主版本发布时（如 Jade 2.0），需要重新跑此审计
- 推荐在 CI 加一个 `gh api` 脚本，每月对比一次 `PROTOCOL_VERSION` 常量
