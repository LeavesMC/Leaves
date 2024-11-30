Leaves
===========

[![Leaves CI](https://github.com/LeavesMC/Leaves/actions/workflows/build.yml/badge.svg)](https://github.com/LeavesMC/Leaves/actions/workflows/leaves.yml)
[![Leaves Download](https://img.shields.io/github/downloads/LeavesMC/Leaves/total?color=0&logo=github)](https://github.com/LeavesMC/Leaves/releases/latest)
[![Discord](https://badgen.net/discord/online-members/5hgtU72w33?icon=discord&label=Discord&list=what)](https://discord.gg/5hgtU72w33)
[![QQ](https://img.shields.io/badge/QQ_Unofficial-815857713-blue)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=nisbmnCFeEJCcYWBQ10th4Fu99XWklH4&authKey=8VlUxSdrFCIwmIpxFQIGR8%2BXvIQ2II%2Bx2JfxuQ8amr9UKgINh%2BdXjudQfc%2FIeTO5&noverify=0&group_code=815857713)

[English](README.md) | **中文**

> 一个致力于修复原版服务端被破坏特性的 [Paper](https://github.com/PaperMC/Paper) 分支

> 你可以在 [这里](https://docs.leavesmc.org/zh_Hans/leaves/reference/configuration) 查看所有的修改和修复内容

## 对于服务器管理员
此分支使用与 Paper 一致的 leavesclip(paperclip的分支) 分发

你可以从 [此处](https://github.com/LeavesMC/Leaves/releases/latest) 下载最新的构建结果 (1.21.x)

也可以通过 [此处](#自行构建) 的指南自行构建

如果你想要获得更多信息，那么你可以访问我们的 [文档](https://docs.leavesmc.org/zh_Hans/leaves/guides/getting-started)

## 对于插件开发者
Leaves-API:
```kotlin
maven {
    name = "leavesmc-repo"
    url = "https://repo.leavesmc.org/snapshots/"
}

dependencies {
    compileOnly("org.leavesmc.leaves:leaves-api:1.21.1-R0.1-SNAPSHOT")
}
 ```

如果你要将 Leaves 作为依赖,那么你必须进行 [自行构建](#自行构建)

Leaves-Server:
```kotlin
dependencies {
    compileOnly("org.leavesmc.leaves:leaves:1.21.1-R0.1-SNAPSHOT")
}
 ```

## 自行构建

你需要最低 JDK 21 和一个可以正常访问各种 git/maven 库的网络

首先克隆此储存库，然后在你的终端里依次执行 `./gradlew applyPatches` 和 `./gradlew createMojmapLeavesclipJar`

最后 你可以在 `build/libs` 文件夹里找到对应的jar文件

## 对于想要出一份力的开发者

可查看 [贡献须知](docs/CONTRIBUTING_cn.md)

## 特别感谢

[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="150">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/)，IntelliJ IDEA 的创造者，为 Leaves 提供了 [开源许可证](https://www.jetbrains.com/opensource/)。我们极力推荐使用 IntelliJ IDEA 作为你的 IDE。
