# REI 协议在 Paper 26.1 下的特殊配方迁移记录

> **批次**：batch 33（2026-04-17）
> **目标**：修复 LIMITATIONS §1.1 — REI 客户端看不到"箭/地图"合成
> **状态**：✅ 已修复

---

## 一、问题背景

batch 31 做编译清零时，并行 agent 里有一项改动是这样的：

```java
// 改动前（Leaves 1.21.10 的代码）
switch (holder.value()) {
    case ShapedRecipe ignored -> ...
    case ShapelessRecipe ignored -> ...
    case TippedArrowRecipe ignored -> builder.addAll(Display.ofTippedArrowRecipe(...));
    case MapCloningRecipe ignored -> builder.addAll(Display.ofMapCloningRecipe(...));
    case CustomRecipe ignored -> ...
    ...
}
```

Paper 26.1 删除了 `TippedArrowRecipe` / `MapCloningRecipe` 两个独立类。Agent 为了让代码通过编译，直接**删掉了那两个 case 分支**，留下了 TODO：

```java
// Leaves - Paper 26.1: TippedArrowRecipe / MapCloningRecipe no longer distinct classes;
// emit fillers once after the loop
```

TODO 没有实现。结果：

- 客户端 REI 界面里搜 `tipped_arrow` → 空白
- 搜 `map` → 看不到"空地图 + 填充地图 → 2 填充地图"合成
- 这是**功能降级**（regression），不是上游删除

---

## 二、调研过程

### 2.1 错误假设

batch 31 的 TODO 里暗示："这两个现在都是普通 `CustomRecipe` instance，只能通过 identifier 区分"。照这个思路修会写成：

```java
case CustomRecipe cr when "minecraft:tipped_arrow".equals(holder.id().identifier().toString()) ->
    builder.addAll(Display.ofTippedArrowRecipe((RecipeHolder) holder));
```

但实际跑去 Paper 26.1 源码里 grep 会发现：**这两个 recipe 根本不是 `CustomRecipe`**。

### 2.2 证据 1：RecipeSerializers 注册表没有它们

```
grep "crafting_special_" leaves-server/src/minecraft/java/.../RecipeSerializers.java
```

结果里有 `bookcloning`、`mapextending`、`firework_rocket`、`firework_star`、`firework_star_fade`、`bannerduplicate`、`shielddecoration`、`repairitem`，但**没有** `crafting_special_tippedarrow` 或 `crafting_special_mapcloning`。

这意味着 Paper 26.1 不仅删了类，连 serializer 注册都没了。

### 2.3 证据 2：VanillaRecipeProvider 里的实际注册

```
grep "tipped_arrow\|map_cloning" leaves-server/src/minecraft/java/.../VanillaRecipeProvider.java
```

找到关键片段：

```java
// tipped_arrow
CustomCraftingRecipeBuilder.customCrafting(
    RecipeCategory.MISC,
    (commonInfo, bookInfo) -> new ImbueRecipe(
        commonInfo, bookInfo,
        Ingredient.of(Items.LINGERING_POTION),
        Ingredient.of(Items.ARROW),
        new ItemStackTemplate(Items.TIPPED_ARROW, 8)
    )
).save(this.output, "tipped_arrow");

// map_cloning
TransmuteRecipeBuilder.transmute(
    RecipeCategory.MISC,
    Ingredient.of(Items.FILLED_MAP),
    Ingredient.of(Items.MAP),
    new ItemStackTemplate(Items.FILLED_MAP)
).addMaterialCountToOutput()
 .setMaterialCount(TransmuteRecipe.FULL_RANGE_MATERIAL_COUNT)
 .group("map_cloning")
 .save(this.output, "map_cloning");
```

所以：
- `tipped_arrow` ← 换成了 `ImbueRecipe`（新类，`extends NormalCraftingRecipe`，serializer 名 `crafting_imbue`）
- `map_cloning` ← 换成了 `TransmuteRecipe`（**和 dye-shulker-box/dye-bundle 复用同一个类**）

### 2.4 ImbueRecipe 究竟是什么？

读 `ImbueRecipe.java`：
- 3x3 crafting grid
- 中心一格：`source`（lingering potion）
- 周围 8 格：`material`（arrow）
- 输出：`template`（8 tipped_arrows）
- 有个"imbue"语义 — 把 source 浸渍到 material 上去

这本质上是一个**"周围包围中心"结构的 shaped recipe 的特化版本**，服务器端只保留一条 recipe 声明，所有具体的 potion 变种是客户端 REI filler 扩展出来的。

### 2.5 为什么 map_cloning 的 TransmuteRecipe 有特殊性？

`TransmuteRecipe` 在 26.1 里用于：
- `map_cloning`（filled_map + empty_map → 2 filled_map，用了 `addMaterialCountToOutput`）
- `shulker_box` 染色（dye + shulker → dyed_shulker，在 `RecipeProvider.java` 里通用注册）
- `bundle` 染色（同上）

也就是说，如果 REI 协议只按 `TransmuteRecipe` 类型统一走 `Display.ofTransmuteRecipe()`，map_cloning 会被当成和 shulker_box 染色、bundle 染色一样的"transmute"展示 —— 客户端会看到，但**样式和 vanilla 1.21.10 的 map_cloning filler 不一样**。

---

## 三、修复方案

### 3.1 `REIServerProtocol.reloadRecipe()` 的 switch 扩展

核心改动（位于 `leaves-server/src/main/java/org/leavesmc/leaves/protocol/rei/REIServerProtocol.java:136-152`）：

```java
switch (holder.value()) {
    case ShapedRecipe ignored -> ...
    case ShapelessRecipe ignored -> ...

    // map_cloning 是 TransmuteRecipe，但要用 ShapelessDisplay 样式展示
    // 放在 generic TransmuteRecipe 之前，走特化分支
    case TransmuteRecipe ignored when "minecraft:map_cloning".equals(holder.id().identifier().toString()) ->
        builder.addAll(Display.ofMapCloningRecipe((RecipeHolder) holder));

    // 其它 TransmuteRecipe（shulker_box 染色 / bundle 染色）走通用 transmute 展示
    case TransmuteRecipe ignored -> builder.addAll(Display.ofTransmuteRecipe((RecipeHolder) holder));

    case FireworkRocketRecipe ignored -> ...

    // ImbueRecipe 目前只用于 tipped_arrow，直接类型匹配即可
    case ImbueRecipe ignored -> builder.addAll(Display.ofTippedArrowRecipe((RecipeHolder) holder));

    default -> { }
}
```

### 3.2 `Display.java` 的方法签名放宽

原签名：

```java
public static Collection<Display> ofTippedArrowRecipe(@NotNull RecipeHolder<CustomRecipe> recipeHolder)
public static Collection<Display> ofMapCloningRecipe(@NotNull RecipeHolder<CustomRecipe> recipeHolder)
```

但真实传进来的是 `RecipeHolder<ImbueRecipe>` 和 `RecipeHolder<TransmuteRecipe>`。方法内部只用到了 `recipeHolder.id().identifier()`（该方法定义在 `RecipeHolder<T>` 而非 `T`），类型参数根本没用到。

所以改成：

```java
public static Collection<Display> ofTippedArrowRecipe(@NotNull RecipeHolder<?> recipeHolder)
public static Collection<Display> ofMapCloningRecipe(@NotNull RecipeHolder<?> recipeHolder)
```

同时删掉 `import net.minecraft.world.item.crafting.CustomRecipe`（不再需要）。

### 3.3 为什么选 pattern matching `when` 子句

JDK 21+ 支持 switch pattern matching 带 guard：

```java
case TransmuteRecipe ignored when <condition> -> ...
```

比起 instanceof + if 的写法，这更简洁，也利于 javac 做 case exhaustiveness 检查。

### 3.4 为什么 ImbueRecipe 不加 guard

因为 `ImbueRecipe.SERIALIZER` 注册名是 `crafting_imbue`，这个 serializer 在 26.1 里**只被 tipped_arrow 使用**（查过 `VanillaRecipeProvider.java` 确认）。未来如果有其它 `ImbueRecipe` 的用法（比如 mod），这个 case 会捕获它们并调用 `ofTippedArrowRecipe` — 这是语义错配。

**改进建议**（不紧急）：加 guard `when "minecraft:tipped_arrow".equals(...)` 以防未来。现在不加是因为当前代码库是纯 vanilla + 不允许三方 ImbueRecipe。

---

## 四、验证

### 4.1 编译

```bash
./gradlew :leaves-server:compileJava
# BUILD SUCCESSFUL in 5s （0 errors）
```

### 4.2 打包

```bash
./gradlew :leaves-server:createLeavesclipJar
# BUILD SUCCESSFUL in 39s
# 产物 62,505,826 bytes（比 batch 32 略大 ~5KB，对应新增的 2 个 switch 分支代码）
```

### 4.3 运行时（阶段 5 待验证）

**需要**：MC 26.1 客户端 + REI mod 连接服务器 → 打开 REI 界面（默认 R 键）。

**期望**：
- 搜 "tipped_arrow" / "poison arrow" / "instant damage arrow" 等 → 每种药水对应一个 3x3 合成预览（8 arrow + 1 lingering potion → 8 tipped arrow）
- 搜 "map" → "filled_map + empty_map → 2 filled_map" 的合成出现在 crafting 分类下

---

## 五、关于 LIMITATIONS §1.1 的关闭

**原预估工作量**：30 分钟（含测试）
**实际耗时**：~25 分钟（大部分花在调研定位真正根因 —— batch 31 TODO 的"CustomRecipe"假设是错的）
**修改文件**：2 个 Leaves 自有源码，无 minecraft-patch 改动
**新增 patch**：无（这是 Leaves 自有代码修正，不触及 paperweight patch 机制）

从静态测试角度已经通过。REI 客户端对接测试留到阶段 5 真正启动服务器时做。

---

## 六、延伸：其它可能需要类似处理的 recipe filler

检查 `Display.java` 里的 "ofXxxRecipe" 方法，验证每个背后的实际 recipe 类型是否与 26.1 相符：

| REI filler 方法 | 1.21.10 对应类 | 26.1 对应类 | 当前 switch 匹配 | 状态 |
|---|---|---|---|---|
| `ofShapedRecipe` → `ShapedDisplay` | `ShapedRecipe` | `ShapedRecipe` | `case ShapedRecipe` | ✅ 不变 |
| `ofShapelessRecipe` → `ShapelessDisplay` | `ShapelessRecipe` | `ShapelessRecipe` | `case ShapelessRecipe` | ✅ 不变 |
| `ofTippedArrowRecipe` | `TippedArrowRecipe` (CustomRecipe) | `ImbueRecipe` (NormalCraftingRecipe) | `case ImbueRecipe` | ✅ batch 33 修 |
| `ofMapCloningRecipe` | `MapCloningRecipe` (CustomRecipe) | `TransmuteRecipe` + id=map_cloning | `case TransmuteRecipe when id=...` | ✅ batch 33 修 |
| `ofFireworkRocketRecipe` | `FireworkRocketRecipe` | `FireworkRocketRecipe`（仍在） | `case FireworkRocketRecipe` | ✅ 不变 |
| `ofTransmuteRecipe` | `TransmuteRecipe` | `TransmuteRecipe`（shulker/bundle/map） | `case TransmuteRecipe` | ✅ 不变（但 map_cloning 特例化了）|
| `ofTransforming` (smithing) | `SmithingTransformRecipe` | 同名 | 同名 | ✅ 不变 |
| `ofSmithingTrimRecipe` | `SmithingTrimRecipe` | 同名 | 同名 | ✅ 不变 |

所有其它分类都是 `RecipeType.STONECUTTING / SMELTING / BLASTING / SMOKING / CAMPFIRE_COOKING / SMITHING`，按 recipe type 分发，没有受 Paper 26.1 影响。

**结论**：REI 协议的 recipe filler 在 26.1 下只有"箭"和"地图复制"两个点需要特殊处理，都已修复。

---

_文档写于 batch 33 完成时。如果将来 Paper 改变 recipe 架构（例如把 `ImbueRecipe` 拆成多个子类），本文档可作为第一手参考。_
