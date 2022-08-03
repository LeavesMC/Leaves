Contributing to Leaves
===========

[English](https://github.com/LeavesMC/Leaves/blob/master/docs/CONTRIBUTING.md) | **中文**


我们很开心您愿意为我们的项目做出共享。一般来说，我们对PR的审核是十分宽松的，
但是如果您可以遵守下列的规则，我们可以更快地完成审核。

## 使用个人账户进行fork

我们会定期尝试合并已有的PR，如果有一些小问题，会尝试帮您解决这些问题。

但是如果您使用了组织账号进行PR，我们就不能对您的PR进行修改了。因此我们只能关闭你的PR然后进行手动合并

所以，请不要使用组织账号进行fork

您可以看看 [这个](https://github.com/isaacs/github/issues/1681) 来了解一下我们为什么无法修改组织账号的PR

## 开发环境

在开始开发之前，您首先需要拥有以下软件作为开发环境

- `git`
- Java17或更高版本的JDK
    - 我们使用了Gradle的Toolchains，这让你可以使用JRE8就进行构建。(Gradle在找不到JDK17的时候会自动下载)

如果你使用Windows系统进行开发，那么你可以使用WSL来加速构建

## 了解补丁

Leaves使用和Paper一样的补丁系统，并为了针对不同部分的修改分成了两个目录

- `leaves-api` - 对 `Paper-API`/`Spigot-API`/`Bukkit` 进行的修改
- `leaves-server` - 对 `Paper`/`Spigot`/`CraftBukkit` 进行的修改

补丁系统是基于git的，你可以在这里了解git的基本内容 <https://git-scm.com/docs/gittutorial>

如果你已经fork了主储存库，那么下面你应该这么做

1. 将你的仓库clone到本地
2. 在你的IDE或终端内执行gradle的`applyPatches`任务，如果是在终端内，你可以执行`./gradlew applyPatches`
3. 进入`leaves-server`或`leaves-api`进行修改

`leaves-server`和`leaves-api`并不是正常的git仓库

- 在应用补丁前，基点将会指向未被更改的源码
- 在基点后的每一个提交都是一个补丁
- 只有在paper最后一个提交后的提交才会被视为leaves补丁

## 增加补丁

按照以下步骤增加一个补丁是非常简单的

1. 对`leaves-server`或者/和`leaves-api`进行修改
2. 使用git添加你的修改，比如`git add .`
3. 使用`git commit -m <提交信息>`进行提交
4. 运行gradle任务`rebuildPatches`将你的提交转化为一个补丁
5. 将你生成的补丁文件进行PR

这样做以后，你就可以将你的补丁文件进行PR提交

## 修改补丁


