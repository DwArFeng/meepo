# libcmd - 外部指令文件夹

该文件夹下放置 meepo 项目所需的外部指令文件，包括但不限于脚本文件、可执行文件等。

Meepo 在执行指令时，会将该目录设为指令的工作目录，然后执行指令文件。

因此，编写 `executors.yaml` 时，在对应的 `command_info` 的 `command` 标签下，推荐使用相对路径。

以下是一个例子：

```yaml
- id: "test1"
  condition_command_infos:
    - id: "foo"
      # 该指令会在 libcmd 目录下寻找 foo.bat 文件
      command: "cmd /c foo.bat"
  module_command_infos:
    - id: "baz"
      # 该指令会在 libcmd 目录下寻找 baz.bat 文件
      command: "cmd /c baz.bat"
  observer_command_infos:
    - id: "quux"
      # 该指令会在 libcmd/observer 目录下寻找 quux.bat 文件
      command: "cmd /c observer/quux.bat"
  continue_on_failure: true
```
