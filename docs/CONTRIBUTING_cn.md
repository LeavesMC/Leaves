Contributing to Leaves
===========

[English](https://github.com/LeavesMC/Leaves/blob/master/docs/CONTRIBUTING.md) | **中文**

我们很开心您想为我们的项目做出贡献！一般来说，我们对PR的审核是十分宽松的；
但是如果您可以遵守下列的规则，我们可以更快地完成审核。

## 使用个人账户进行 Fork

我们会定期尝试合并已有的 PR，如果有一些小问题，会尝试帮您解决这些问题。

但是如果您使用了组织账号进行 PR，我们就不能对您的 PR 进行修改了。因此我们只能关闭你的PR然后进行手动合并。

所以，请不要使用组织账号进行 Fork！

您可以看看 [这个 Issue](https://github.com/isaacs/github/issues/1681) 来了解一下我们为什么无法修改组织账号的 PR。

## 开发环境

在开始开发之前，您首先需要拥有以下软件作为开发环境：

- `git`
- `JDK 21 或更高版本`
  - 我们使用了 Gradle 的工具链，这让你可以使用 JRE 8 就进行构建。(Gradle 在找不到 JDK 21 时会自动下载)

如果你使用 Windows 系统进行开发，那么你可以使用 `WSL` 来加速构建。

## 了解补丁（Patches）

Leaves 使用和 Paper 一样的补丁系统，并为了针对不同部分的修改分成了两个目录：

- `leaves-api` - 对 `Paper-API` / `Spigot-API` / `Bukkit` 进行的修改。
- `leaves-server` - 对 `Paper`/ `Spigot` / `CraftBukkit` 进行的修改。

补丁系统是基于 git 的，你可以在这里了解 git 的基本内容: <https://git-scm.com/docs/gittutorial>

如果你已经 Fork 了主储存库，那么下面你应该这么做：

1. 将你的仓库 clone 到本地；
2. 在你的 IDE 或 终端 内执行 Gradle 的 `applyPatches` 任务，如果是在终端内，你可以执行 `./gradlew applyPatches`；
3. 进入 `leaves-api` 或 `leaves-server` 文件夹进行修改。

By the way，`leaves-api` 和 `leaves-server` 并不是正常的 git 仓库。

- 在应用补丁前，基点将会指向未被更改的源码
- 在基点后的每一个提交都是一个补丁
- 只有在 Paper 最后一个提交后的提交才会被视为 leaves 补丁

## 增加补丁

按照以下步骤增加一个补丁是非常简单的：

1. 对 `leaves-api` 或者 / 和 `leaves-server` 进行修改；
2. 使用 git 添加你的修改，比如 `git add .`；
3. 使用 `git commit -m <提交信息>` 进行提交；
4. 运行 Gradle 任务 `rebuildPatches` 将你的提交转化为一个补丁；
5. 将你生成的补丁文件进行推送。

这样做以后，你就可以将你的补丁文件进行 PR 提交。

## 修改补丁

你可以使用以下方法来修改一个补丁的内容：

1. 在 HEAD 上直接进行修改；
2. 使用 `git commit -a --fixup <hash>` 来进行一个更正提交；
   - 如果你想要更改提交信息，你也可以用 `--squash` 来代替 `--fixup`。
3. 使用 `git rebase -i --autosquash base` 来进行自动变基，你只需要输入 `:q` 来关闭确认页面即可；
4. 运行 Gradle 任务 `rebuildPatches` 来修改已被修改的补丁；
5. 将修改后的补丁 PR 发回储存库。
