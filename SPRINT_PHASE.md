# Leaves 26.1.2 升级 — Sprint Phase（冲刺阶段）

> **更新时间**：2026-04-16（batch 26 完成）
> **当前状态**：阶段 3 进度 96%（165/173 patch），minecraft 编译错误 5 个（全部来自 0136 依赖）

---

## 一、剩余 3 个 minecraft-patches（features-todo/）

| # | Patch | 行数 | 难度 | 说明 |
|---|---|---|---|---|
| 0136 | Lithium-Sleeping-Block-Entity | 2480 | 🔴 | 最大 patch。Lithium 休眠 BE 优化，涉及 LevelChunk/ItemStack 内部。解锁 0141 的 5 个编译错误 |
| 0102 | Old-Block-remove-behaviour | 808 | 🔴 | 30+ 种方块的 `popExperience`/`spawnAfterBreak`，Paper 26.1 重构了方块破坏流程 |
| 0072 | Replay-Mod-API | 537 | 🟡 | ServerPhotographer 录像，涉及网络层 |

### 推荐处理顺序

```
第 1 波: 0136 Lithium-Sleeping-Block-Entity (2480行) → 解锁 0141 的 5 个 ItemStack 错误 + lithium 模块 23 个错误
第 2 波: 0072 Replay-Mod-API (537行)
第 3 波: 0102 Old-Block-remove (808行)
```

---

## 二、1 个 paper-patch（features-todo/）

| Patch | 说明 |
|---|---|
| 0013-Leaves-Plugin | Paper 26.1 重构 plugin provider 系统，需完全重写 |

---

## 三、164 个编译错误分布

| 分类 | 错误数 | 来源 |
|---|---|---|
| minecraft patches (EntityEquipment lithium deps) | 5 | 等待 0136 |
| `protocol/servux/` | 42 | API 重命名 |
| `lithium/` | 23 | 等待 0136 |
| `protocol/jade/` | 18 | API 重命名 |
| `protocol/rei/` | 20 | API 重命名 |
| `region/linear/` | 26 | 已部分解锁，剩余需 API 迁移 |
| `protocol/` (PCA, BBOR, AppleSkin) | 11 | API 迁移 |
| `paper-server/craftbukkit/` | 12 | API 迁移 |
| `plugin/provider/` | 4 | 等待 0013 |
| 其他 | 3 | 逐个修复 |

---

## 四、数字快照（batch 26）

| 指标 | 值 |
|---|---|
| 已 rebase 的 minecraft-patches | 141/147 (96%) |
| 已 rebase 的 paper-patches | 15/16 (94%) |
| 已 rebase 的 API patches | 9/9 (100%) |
| Obsoleted patches | 5 |
| **总进度** | **165/173 (95%)** |
| minecraft-source 编译错误 | 5（全部来自 0136 依赖） |
| Leaves 自有源码编译错误 | 159 |
| `applyAllPatches` | ✅ 通过 |
| `compileJava` | ❌ 164 errors |
