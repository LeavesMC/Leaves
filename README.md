Leaves 
===========

[![Leaves CI](https://github.com/LeavesMC/Leaves/actions/workflows/leaves.yml/badge.svg)](https://github.com/LeavesMC/Leaves/actions/workflows/leaves.yml)
[![Leaves Download](https://img.shields.io/github/downloads/LeavesMC/Leaves/total?color=0&logo=github)](https://github.com/LeavesMC/Leaves/releases/latest)
[![Discord](https://badgen.net/discord/online-members/5hgtU72w33?icon=discord&label=Discord&list=what)](https://discord.gg/5hgtU72w33)

**English** | [中文](https://github.com/LeavesMC/Leaves/blob/master/README_cn.md)

> Fork of [Paper](https://github.com/PaperMC/Paper) aims at repairing broken vanilla properties.

> You can see what we modify and fix at [here](https://github.com/LeavesMC/Leaves/blob/master/docs/MODIFICATION.md)

## How To (Server Admins)
Leaves use the same paperclip jar system that Paper uses.

You can download the latest build (1.20.x) of Leaves by going [here](https://github.com/LeavesMC/Leaves/releases/latest)

You can also [build it yourself](https://github.com/LeavesMC/Leaves#building).

You can visit our [documentation](https://docs.leavesmc.top/leaves) for more information.

## How To (Plugin developers)
Leaves-API:
```kotlin
maven {
    name = 'leavesmc-repo'
    url = 'https://repo.leavesmc.top/snapshots/'
}

dependencies {
    compileOnly("top.leavesmc.leaves:leaves-api:1.20.2-R0.1-SNAPSHOT")
}
 ```

In order to use Leaves as a dependency you must [build it yourself](https://github.com/LeavesMC/Leaves#building).
Each time you want to update your dependency, you must re-build Leaves.

Leaves-Server:
```kotlin
dependencies {
    compileOnly("top.leavesmc.leaves:leaves:1.20.2-R0.1-SNAPSHOT")
}
 ```

## Building

You need JDK 17 and good Internet conditions

Clone this repo, run `./gradlew applyPatches`, then run `./gradlew createReobfBundlerJar` in your terminal.  

You can find the jars in the `build/libs` directory.

## Pull Requests

See [Contributing](https://github.com/LeavesMC/Leaves/blob/master/docs/CONTRIBUTING.md)

## Special Thanks To:

[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="150">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports We with one of their [Open Source Licenses](https://www.jetbrains.com/opensource/). We recommend using IntelliJ IDEA as your IDE.
