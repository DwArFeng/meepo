# ChangeLog

### Release_1.1.0_20240603_build_A

#### 功能构建

- 简化 libcmd 目录。

- 增强 Poof - Arrival 机制。
  - 增加了黑白名单机制，以便 Daemon 端可以根据黑白名单来决定是否接受 Poof 端的请求。

- 优化部分代码的格式。
  - com.dwarfeng.meepo.handler.daemon.ExecuteHandlerImpl。

- 优化 Poof 功能的数据交互机制。
  - Daemon 接受到 poof 并处理请求后，向 poof 端发送 ArrivalResponse，而不再只发送状态码。

- 新增依赖。
  - 增加依赖 `fastjson` 以应用其功能，版本为 `1.2.83`。

#### Bug修复

- (无)

#### 功能移除

- (无)

---

### Release_1.0.0_20240531_build_A

#### 功能构建

- 完成项目主要逻辑，打包测试及启动测试通过。

- 项目结构建立，程序清理测试通过。

#### Bug修复

- (无)

#### 功能移除

- (无)
