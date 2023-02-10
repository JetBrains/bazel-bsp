# Project View

The project view file (*.bazelproject) is used to import a subset of bazel targets into the IDE, configure a project,
and specify how the bsp server will be started.

This is our adaptation of the project view mechanism known
from [Bazel Plugin for Intellij](https://ij.bazel.build/docs/project-views.html)

> The project view file uses a python-like format with 2 spaces indentation and # comments. You can share the
> *.bazelproject file between projects, use your own copy, or both.
>
> In general, you can start with just ~~directories and~~ targets and add more sections as you want to further tweak
> your IDE workspace.


## Usage

**Note:** We will be changing this mechanism in future releases.

Simply put `projectview.bazelproject` file in the root of your project and fill it.

## Available sections

---

#### import

Imports another project view.

You may use multiple imports in any project view. Any list type sections (e.g. `targets`) compose. Single-value
sections (e.g. `bazel_path`) override and use the last one encountered, depth-first parse order (i.e. imports are
evaluated as they are encountered).

##### example:

```
import path/to/another/projectview.bazelproject
```

---

### Server installer sections

These sections are read during server [installation](https://github.com/JetBrains/bazel-bsp#installation)
-- the moment when `.bsp/` is created containing all the necessary information to start the server.

**Note**: It happens when the installer is invoked -- **before** starting the server!

---

#### java_path

Path to java which will be used to start the server (first argument in `argv` in `.bsp/bazelbsp.json`).

##### example:

```
java_path: /Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home/bin/java
```

##### default:

The following code will be used to deduct a java path automatically:

```
System.getProperty("java.home").resolve("bin").resolve("java")
```

---

#### debugger_address

Address of debugger which will be attached to the java program by the flag:

```
<java path> <server runner> -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=<debugger_address>`
```

##### example:

```
debugger_address: 127.0.0.1:8000
```

##### default:

The server will be started without debugger

--- 

### Project sections

These sections are read when server is starting -- usually when you open a project in IDE.

---

#### targets

A list of bazel target expressions, they support `/...` notation.

Targets are built during the server lifetime, so the more targets you have, the slower your IDE experience might be. You
can use negative targets to have server ignore certain targets (
e.g. `-//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/...`).

##### example:

```
targets:
  //install/src/main/java/org/jetbrains/bsp/bazel/install
  //executioncontext/projectview/...
  -//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/...
```

##### default:

All targets are included:

```
targets:
  //...
```

---

#### bazel_path

Path to bazel which will be used to invoke bazel from the server (e.g. to build a project, or query bazel).

##### example:

```
bazel_path: /usr/local/bin/bazel
```

##### default:

The server will deduct bazel path from `$PATH`

---

#### directories

A list of directories to be mapped into bazel targets.

You can use negative directories to have server ignore certain directories (
e.g. `-executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/...`).

##### example:

```
directories:
  install/src/main/java/org/jetbrains/bsp/bazel/install
  executioncontext/projectview/
  -executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser
```

##### default:

No directories included.

---

#### derive_targets_from_directories

A flag specifying if targets should be derived from list of directories in directories section.

Flag is boolean value, so it can take either true or false. In the first case targets will be derived from directories, in the second they won't.

##### example:

```
derive_targets_from_directories: true
```

##### default:

Targets will not be derived from directories.

---

#### import_depth

A numerical value that specifies how many levels of bazel targets dependencies should be imported as modules.
Only the targets that are present in workspace are imported.

You can use negative value to import all transitive dependencies.

##### example:

```
import_depth: 1
```

##### default:

The default value is 0, meaning that only root targets are imported

---

#### produce_trace_log

A flag specifying if `./bazelbsp/bazelbsp.trace.log` should be created and used to store trace logs.

*NOTE: trace log may affect server-client communication performance! It is recommended that this flag be set to false.

Flag is boolean value, so it can take either true or false.

##### example:

```
produce_trace_log: true
```

##### default:

Log file won't be created.

---

#### workspace_type

_We are working on it, you can expect support for this section in future releases._

---

#### additional_languages

_We are working on it, you can expect support for this section in future releases._

---

#### java_language_level

_We are working on it, you can expect support for this section in future releases._

---

#### test_sources

_We are working on it, you can expect support for this section in future releases._

---

#### shard_sync

_We are working on it, you can expect support for this section in future releases._

---

#### target_shard_size

_We are working on it, you can expect support for this section in future releases._

---

#### exclude_library

_We are working on it, you can expect support for this section in future releases._

---

#### build_flags

A set of bazel flags added to **all** bazel command invocations.

##### example:

```
build_flags:
  --define=ij_product=intellij-latest
```

##### default:

No flags.

---

#### sync_flags

_We are working on it, you can expect support for this section in future releases._

---

#### test_flags

_We are working on it, you can expect support for this section in future releases._

---

#### import_run_configurations

_We are working on it, you can expect support for this section in future releases._

---

#### android_sdk_platform

_We are working on it, you can expect support for this section in future releases._

---

#### android_min_sdk

_We are working on it, you can expect support for this section in future releases._

---

#### generated_android_resource_directories

_We are working on it, you can expect support for this section in future releases._

---

#### ts_config_rules

_We are working on it, you can expect support for this section in future releases._

---

#### build_manual_targets

A flag specifying if targets with `manual` tag should be built.
Flag is boolean value, so it can take either true or false. In the first case targets with `manual` tag will be build,
otherwise they will not.

##### example:

build_manual_targets: true

##### default:

build_manual_targets: false
