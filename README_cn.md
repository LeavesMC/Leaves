Leaves
---------

[English](https://github.com/LeavesMC/Leaves/blob/master/README.md) | **中文**

> 一个致力于修复原版服务端被破坏特性的 [Paper](https://github.com/PaperMC/Paper) 分支

> 你可以在[这里](https://github.com/LeavesMC/Leaves/blob/master/docs/MODIFICATION_cn.md)查看所有的修改和修复内容

> 此服务端的性能可能会略低于Paper原版 并不适合所有服务器使用

## 对于服务器管理员
此分支使用与Paper一致的paperclip分发

你可以从 [此处](null) 下载最新的构建结果 (1.18.x)

也可以通过 [此处](https://github.com/LeavesMC/Leaves/blob/master/README_cn.md#自行构建) 的指南自行构建

## 对于插件开发者
> 此服务端并不会提供好用的api,为什么不去依赖Paper呢

如果你要将Leaves作为依赖,那么你必须进行 [自行构建](https://github.com/LeavesMC/Leaves/blob/master/README_cn.md#自行构建)

Leaves-API:
```xml
<dependency>
    <groupId>top.leavesmc.leaves</groupId>
    <artifactId>leaves-api</artifactId>
    <version>1.18.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
 </dependency>
 ```

Leaves-Server:
```xml
<dependency>
    <groupId>top.leavesmc.leaves</groupId>
    <artifactId>leaves</artifactId>
    <version>1.18.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
## 自行构建
需要Java17或更高版本

`./gradlew applyPatches`

`./gradlew createReobfBundlerJar`
