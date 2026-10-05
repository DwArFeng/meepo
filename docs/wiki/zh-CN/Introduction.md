# Meepo

Meepo 是一个多节点指令调度工具，可以通过配置文件，同时向多个节点发送指令，支持多种指令类型，支持自定义指令。

Meepo 来源于 Dota2 中的英雄 Meepo，Meepo 是一个可以分裂成多个独立实体的英雄，
每个实体都可以独立行动，可以通过 Poof 技能将所有实体传送到同一个位置。这个工具的功能就是向多个节点发送指令，就像 Meepo 一样。

---

## 特性

- 跨平台支持。
- 提供在后台运行的 daemon 模块。
- 任何节点都可相互调度。
- 支持事件监听。

## 使用场景

- 数据备份。
- 集群管理。

## 文档

该项目的文档位于 [docs](../../../docs) 目录下，包括：

### wiki

wiki 为项目的开发人员为本项目编写的详细文档，包含不同语言的版本，主要入口为：

1. [简介](./Introduction.md) - 即本文件。
2. [目录](./Contents.md) - 文档目录。

## 安装说明

1. 下载源码

   使用 git 进行源码下载。

   ```shell
   git clone git@github.com:DwArFeng/meepo.git
   ```

   对于中国用户，可以使用 gitee 进行高速下载。

   ```shell
   git clone git@gitee.com:dwarfeng/meepo.git
   ```

2. 项目打包

   进入项目根目录，执行 maven 命令。

   ```shell
   mvn clean package
   ```

   打包结束后，会在 `target` 目录下生成一个 `meepo-{version}-release.tar.gz` 的压缩包，
   将其部署到目标机器上并解压，即可使用。

3. 启动 daemon 模块

   daemon 模块是一个后台运行的模块，可以调用 bin 目录下的 sh 或 bat 脚本启动。

   以 Linux 为例，执行以下命令启动 daemon 模块：

   ```shell
   sh bin/daemon.sh start
   ```

4. 使用 poof 模块调度自身或其他节点

   poof 模块是一个调度模块，可以调用 bin 目录下的 sh 或 bat 脚本运行。

   以 Linux 为例，执行以下命令调度自身节点：

   ```shell
   # 调度自身节点
   sh bin/poof.sh localhost:8089 foobar
   # 调度其他节点
   sh bin/poof.sh otherhost:8089 foobar
   ```
