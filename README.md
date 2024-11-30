Leaves 
===========

[![Leaves CI](https://github.com/LeavesMC/Leaves/actions/workflows/build.yml/badge.svg)](https://github.com/LeavesMC/Leaves/actions/workflows/leaves.yml)
[![Leaves Download](https://img.shields.io/github/downloads/LeavesMC/Leaves/total?color=0&logo=github)](https://github.com/LeavesMC/Leaves/releases/latest)
[![Discord](https://badgen.net/discord/online-members/5hgtU72w33?icon=discord&label=Discord&list=what)](https://discord.gg/5hgtU72w33)
[![QQ](https://img.shields.io/badge/QQ_Unofficial-815857713-blue)](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=nisbmnCFeEJCcYWBQ10th4Fu99XWklH4&authKey=8VlUxSdrFCIwmIpxFQIGR8%2BXvIQ2II%2Bx2JfxuQ8amr9UKgINh%2BdXjudQfc%2FIeTO5&noverify=0&group_code=815857713)

**English** | [中文](README_cn.md)

> Fork of [Paper](https://github.com/PaperMC/Paper) aims at repairing broken vanilla properties.

> You can see what we modify and fix at [here](https://docs.leavesmc.org/en/leaves/reference/configuration)

## How To (Server Admins)
Leaves use the same leavesclip(paperclip fork) jar system that Paper uses.

You can download the latest build (1.21.x) of Leaves by going [here](https://github.com/LeavesMC/Leaves/releases/latest)

You can also [build it yourself](#building).

You can visit our [documentation](https://docs.leavesmc.org/leaves/guides/getting-started) for more information.

## How To (Plugin developers)
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

In order to use Leaves as a dependency you must [build it yourself](#building).
Each time you want to update your dependency, you must re-build Leaves.

Leaves-Server:
```kotlin
dependencies {
    compileOnly("org.leavesmc.leaves:leaves:1.21.1-R0.1-SNAPSHOT")
}
 ```

## Building

You need JDK 21 and good Internet conditions

Clone this repo, run `./gradlew applyPatches`, then run `./gradlew createMojmapLeavesclipJar` in your terminal.  

You can find the jars in the `build/libs` directory.

## Pull Requests

See [Contributing](docs/CONTRIBUTING.md)

## Special Thanks To:

[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="150">](https://www.jetbrains.com)

[JetBrains](https://www.jetbrains.com/), creators of the IntelliJ IDEA, supports Leaves with one of their [Open Source Licenses](https://www.jetbrains.com/opensource/). Leaves recommend using IntelliJ IDEA as your IDE.
