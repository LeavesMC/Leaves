Leaves Modification
===========

[English](https://github.com/LeavesMC/Leaves/blob/master/docs/MODIFICATION.md) | **中文**

## 修复

> 所有的修复内容都不会存在配置项

- 重力方块复制
- 虚空交易
- 绊线钩即将被水破坏时亦然生成激活的绊线

## 修改

> 所有的修改内容都会存在配置项

- 玩家可以编辑已经放置的告示牌
- 雪球和鸡蛋可以击退玩家
- 假人支持 (类似carpet) (指令为 /bot)
- 发射器里的剪刀可以无限使用
- 剪刀可以用来旋转红石原件 (类似调试棒)
- 紫水晶母岩可以被活塞推动
- 观察者不会获得进度
- 对盔甲架下蹲使用木棍可以修改盔甲架的手臂状态
- 删除玩家聊天内的签名 (可以替代NoChatReportMod的服务器侧)
- 重新引入更新抑制机制
- 扁平化随机数三角分布 (类似Carpet-TIS-Addition)
- 玩家操作限制器 (可禁止自动破基岩mod)
- 可再生鞘翅 (当潜影贝杀死幻翼时)
- 可堆叠空潜影箱
- 生电模式
- 返回传送门位置修复
- 额外外置登录服务器支持
- 原版随机数 (支持RNG控制)
- 更新抑制/跳略崩服修复
- 破基岩榜
- 有摔落缓冲不会踩坏田
- 共享村民打折
- 红石粉不会连接到活扳门 (恢复简易更新抑制)
- 手上有方块的末影人一样会被刷新
- 创造飞行无碰撞箱 (需要配合carpet协议和客户端mod)
- 无限和精修不再冲突
- 可以铲的雪
- 怪物生成无视lc值

## 性能

> 所有的性能内容都会存在配置项

> Powered by [Pufferfish](https://github.com/pufferfish-gg/Pufferfish)
- 生物生成优化 (正在升级 暂不可用)
- 异步实体追踪 (正在升级 暂不可用)
- 修复Paper#6045
- 实体坐标键优化
- 窒息检测优化
- 实体射线优化
- 万圣节检测优化
- 区块刻优化
- 跳过矿车中实体的方块搜索
- 实体目标检测优化
- 使用更多的线程不安全随机数发生器
- 关闭方法分析器
- 禁用非活跃实体的目标选择器
- 跳过战利品表参数复制
- 减少实体分配
- 删除部分lambda表达式
- 删除容器检查中的iterators
- 删除玩家检测中的流
- 删除范围检查中的流和iterators
- 异步实体寻路 (正在升级 暂不可用)
- 缓存实体攀爬检测
- 使用更好的生物群系温度缓存
- 优化实体流体检查
- 优化末影人传送时的区块寻找
- 更好的原版无序配方
- 优化流体距离计算缓存

> Powered by [Purpur](https://github.com/PurpurMC/Purpur)
- 减少不必要包的发送

> Powered by [Carpet-AMS-Addition](https://github.com/Minecraft-AMS/Carpet-AMS-Addition)
- 龙战优化

## 额外协议支持

> 所有的协议内容都会存在配置项

- PCA同步协议
- BBOR结构显示协议
- Jade数据同步协议
- Carpet精确放置协议 (carpet-extra)
- 苹果皮显示协议
- Xaero服务器地图设置协议
- 共享原理图协议 ([syncmatica](https://github.com/End-Tech/syncmatica))
- Leaves-Carpet协议 仅用于同步设置