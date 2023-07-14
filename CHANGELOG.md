# Changelog

<!-- Keep a Changelog guide -> https://keepachangelog.com -->


## [Unreleased]
### Features 🎉
- Experimental `workspace/libraries` endpoint that returns list of external libraries

### BREAKING CHANGES 🚨

- Bloop support has been dropped.
- Project view file is obligatory now! Server requires path to the file in `argv` in `.bsp/bazelbsp.json`.
  Debugger address (`debugger_address`), java path (`java_path`) and flag for trace log (`produce_trace_log`) are *no 
  longer* fields in project view files! They can be set *only* using installer flags (check [README](install/README.md)).
- Project view `bazel_path` has been renamed to `bazel_binary` (now it's compatible with https://ij.bazel.build/docs/project-views.html#bazel_binary).

### Features 🎉
- Enhance support for Kotlin by providing Kotlin target's specific info.
  | [#BAZEL-460](https://youtrack.jetbrains.com/issue/BAZEL-460)

### Fixes 🛠️
- Now we report the failure of the whole test target and binaries are reporting stdout.

## [2.7.2]

### Fixes 🛠️
- Collect scalac options from the toolchain
  | [#433](https://github.com/JetBrains/bazel-bsp/pull/433)
- Add support for Scala SDK provided by rules_jvm_external
  | [#403](https://github.com/JetBrains/bazel-bsp/pull/403)

### Performance
-  Reduce peak memory footprint
   | [#428](https://github.com/JetBrains/bazel-bsp/pull/428)

## [2.7.1]

### Fixes 🛠️
-  Publish `build/publishDiagnostics` with an empty array of diagnostics to clear former diagnotics.
   | [#381](https://github.com/JetBrains/bazel-bsp/pull/381)
-  Prioritize most frequently used JDKs when selecting the project JDK.
   | [#420](https://github.com/JetBrains/bazel-bsp/pull/420)
-  Fix ide classpath computation for recent rules_jvm_external.
   | [#421](https://github.com/JetBrains/bazel-bsp/pull/421)
-  Process exit instead of hang in case of uncaught exception in pooled threads
   | [#425](https://github.com/JetBrains/bazel-bsp/pull/425),
   [#426](https://github.com/JetBrains/bazel-bsp/pull/426)

## [2.7.0]

### Features 🎉
- Server uses BEP to log bazel progress.
  | [ae52b8f](https://github.com/JetBrains/bazel-bsp/commit/ae52b8f401b793ba15e84d492ba0f72a462b74dc)

### Fixes 🛠️
-  Add class jars generated during annotation processing.
  | [#372](https://github.com/JetBrains/bazel-bsp/pull/372)
- Set PublishDiagnosticsParams.reset to be true.
  | [#377](https://github.com/JetBrains/bazel-bsp/pull/377)
- Update document about how to use projectview.
  | [#383](https://github.com/JetBrains/bazel-bsp/pull/383)
- Fixup failed target names from BEP in bazel 6+ in the bloop mode.
  | [#402](https://github.com/JetBrains/bazel-bsp/pull/402)

## [2.6.1]

### SECURITY 🚨

- Make BEP Server listening on localhost instead of 0.0.0.0.
  | [#369](https://github.com/JetBrains/bazel-bsp/pull/369)

### Fixes 🛠️

- Create BEP server on demand for each Bazel call.
  | [#370](https://github.com/JetBrains/bazel-bsp/pull/370)

## [2.6.0]

### Features 🎉

- Project view file flag for disabling trace log.
  | [#344](https://github.com/JetBrains/bazel-bsp/pull/344)
- Create BEP connection on demands instead of keeping it as a service (reduces memory footprint)
  | [#356](https://github.com/JetBrains/bazel-bsp/pull/356)

### Fixes 🛠️

- Fixed handle bazelisk exec on windows.
  | [#6294219](https://github.com/JetBrains/bazel-bsp/commit/629421998dcd1adb1e9c87973a128b207b9993eb)
- Reduced memory footprint of the application after project import is done
  | [#359](https://github.com/JetBrains/bazel-bsp/pull/359)
- Exit bazel-bsp process when std io streams are closed
  | [#356](https://github.com/JetBrains/bazel-bsp/pull/356)

## [2.5.1] - 08.02.2023

### Changes 🔄
 
- Initialize the hash sets with the number of elements they are expected to hold.
  | [#339](https://github.com/JetBrains/bazel-bsp/pull/339)

### Fixes 🛠️

- Current target jar is excluded from dependencies - aka jump to definition within a module should work now.
  | [#340](https://github.com/JetBrains/bazel-bsp/pull/340)
- Changed separator for classpath to os-agnostic.
  | [#BAZEL-255](https://github.com/JetBrains/bazel-bsp/commit/8b58b265dbdf5888b1d355866b41a7feda5c0e11)

## [2.5.0] - 29.01.2023

### Features 🎉

- Add mainClasses parameters to `JvmEnvironmentItem`.
  | [#309](https://github.com/JetBrains/bazel-bsp/pull/309)

- Exclude bazel-* symlinks; `outputPaths` endpoint implemented.
  | [#322](https://github.com/JetBrains/bazel-bsp/pull/322)

## [2.4.0] - 2.01.2023

### Features 🎉

- Support Bazel 6.0.0
  | [#318](https://github.com/JetBrains/bazel-bsp/pull/318)

## [2.3.1] - 21.12.2022

### Fixes 🛠️

- Correctly handle java files with package names not corresponding to file paths
  | [#312](https://github.com/JetBrains/bazel-bsp/pull/312)
- Correctly determine language type for JVM binary targets.
  | [#306](https://github.com/JetBrains/bazel-bsp/pull/306)

## [2.3.0] - 26.10.2022

### Features 🎉

- Send notifications to client while JUnit5 testing.
  | [#299](https://github.com/JetBrains/bazel-bsp/pull/299)

### Changes 🔄

- Add async and sync output processors.
  | [#288](https://github.com/JetBrains/bazel-bsp/pull/288)
- Pass originId into diagnostics.
  | [#291](https://github.com/JetBrains/bazel-bsp/pull/291)
- Pass originId into server responses.
  | [#289](https://github.com/JetBrains/bazel-bsp/pull/289)

### Fixes 🛠️

- `BuildTargetIdentifier` mapping uses `.uri` instead of `.toString`.
  | [#295](https://github.com/JetBrains/bazel-bsp/pull/295)
- `JVMLanguagePluginParser.calculateJVMSourceRoot` does not throw 
  an exception for package longer than the path.
  | [#294](https://github.com/JetBrains/bazel-bsp/pull/294)
- Fix transitive target failure check in bloop export.
  | [#287](https://github.com/JetBrains/bazel-bsp/pull/287)
- Use direct source dependencies for thrift targets if present.
  | [#298](https://github.com/JetBrains/bazel-bsp/pull/298)
- Postpone bazel info.
  | [#300](https://github.com/JetBrains/bazel-bsp/pull/300)

## [2.2.1] - 09.08.2022

### Fixes 🛠️

- Adding generated jars to source jars list.
  | [#285](https://github.com/JetBrains/bazel-bsp/pull/285)
- Workspace root is always used as a bazel directory.
  | [#284](https://github.com/JetBrains/bazel-bsp/pull/284)
- Kebab case in the installer flags & help typo fix & log typo fix.
  | [#283](https://github.com/JetBrains/bazel-bsp/pull/283)
- Don't use `Label.toString` now that it's no longer overriden.
  | [#282](https://github.com/JetBrains/bazel-bsp/pull/282)

## [2.2.0] - 27.07.2022

### Features 🎉

- Server can be used with bloop (`--use_bloop` flag).
  | [#246](https://github.com/JetBrains/bazel-bsp/pull/246)
- Project view supports `build_manual_targets` - now it is possible to build targets with `manual` tag.
  | [#243](https://github.com/JetBrains/bazel-bsp/pull/243)
- `directories` and `derive_targets_from_directories` sections are now available in project view files.
  | [#247](https://github.com/JetBrains/bazel-bsp/pull/247)
- Dependent targets can be imported as modules based on `import_depth` project view parameter.
  | [#248](https://github.com/JetBrains/bazel-bsp/pull/248)
- BSP can be installed in a separate directory. `-w` flag sets Bazel workspace, `-d` sets the BSP installation root

### Changes 🔄

- Added cache of targets info in incremental syncs.
  | [#273](https://github.com/JetBrains/bazel-bsp/pull/273)
- Dependency sources are now filtered if they belong to other root target
  | [#265](https://github.com/JetBrains/bazel-bsp/pull/265)
- Packages are parsed in order to determine source roots.
  | [#258](https://github.com/JetBrains/bazel-bsp/pull/258)
- BazelPathsResolver caches uris and paths (preformance improvement).
  | [#256](https://github.com/JetBrains/bazel-bsp/pull/256)
- Scala version regex has been fixed.
  | [#249](https://github.com/JetBrains/bazel-bsp/pull/249)
- e2e test for local jdk and remote jdk
  | [#253](https://github.com/JetBrains/bazel-bsp/pull/253)
- Dependency sources are now filtered if they belong to other root target
  | [#265](https://github.com/JetBrains/bazel-bsp/pull/265)
- JvmEnvironmentItem is set using env from target
  | [#260](https://github.com/JetBrains/bazel-bsp/pull/260)
- Server has undergone basic changes leading to the ability to work with CPP language.
  | [#277](https://github.com/JetBrains/bazel-bsp/pull/277)
  
### Fixes 🛠️
- CLI project view generator now supports `--excluded-targets` and `--excluded-directories` instead of excluded `-` prefix.
  | [#267](https://github.com/JetBrains/bazel-bsp/pull/267)

## [2.1.0] - 11.05.2022

### Features 🎉

- Manual tag and `build_manual_targets` support in project view. 
  Using this flag it is possible to build targets marked as manual.
  | [#236](https://github.com/JetBrains/bazel-bsp/pull/236)
- Build flags are reloaded during sync.
  | [#242](https://github.com/JetBrains/bazel-bsp/pull/242)

### Changes 🔄

- Support for detecting jdk through runtime_jdk poperty; infer jdk
  | [#241](https://github.com/JetBrains/bazel-bsp/pull/241)
- New project view parser without default file.
  | [#242](https://github.com/JetBrains/bazel-bsp/pull/242)
- Map label->module is no longer serialized -
  it was doubling size of the file.
  | [#233](https://github.com/JetBrains/bazel-bsp/pull/233)
- Diagnostic message doesn't contain path to file anymore.
  Also, column number is now inferred from the message.
  | [#238](https://github.com/JetBrains/bazel-bsp/pull/238)

## [2.0.0] - 22.04.2022

### Features 🎉

- New installer with project view generator -
  now it is possible to create a project view using installer.
  | [#227](https://github.com/JetBrains/bazel-bsp/pull/227)
- New implementation of diagnostics extraction. It collects
  both warnings and errors, doesn't show irrelevant errors
  in BUILD files, shows line and character position and does
  not run bazel query before each build anymore.
  | [#225](https://github.com/JetBrains/bazel-bsp/pull/225)
- `build_flags` support in project view.
  | [#194](https://github.com/JetBrains/bazel-bsp/pull/194)
- New installer - it is possible to specify installation directory
  and project view file using flags.
  | [#201](https://github.com/JetBrains/bazel-bsp/pull/201)
- Server now keeps the state of the project between runs.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Improved bazel runner. The BSP Client now receives invoked bazel command,
  its output, duration and exit code.
  | [#198](https://github.com/JetBrains/bazel-bsp/pull/198)
- Scala diagnostics work without a fork (technically it worked already for a while).
  | [#170](https://github.com/JetBrains/bazel-bsp/pull/170)

### Fixes 🛠️

- Parsing of project view file fails if the file doesn't exist.
  | [#215](https://github.com/JetBrains/bazel-bsp/pull/215)
- Project view path is mapped to the absolute path in the installer.
  | [#213](https://github.com/JetBrains/bazel-bsp/pull/213)
- Kotlin targets don't break import.
  | [#211](https://github.com/JetBrains/bazel-bsp/pull/211)
- Now sources of thrift dependencies are included as dependencies.
  | [#202](https://github.com/JetBrains/bazel-bsp/pull/202)
- Handle the case when there is no JDK in the project.
  | [#200](https://github.com/JetBrains/bazel-bsp/pull/200)
- Fixed extraction of java version and java home for bazel `5.0.0`.
  | [#165](https://github.com/JetBrains/bazel-bsp/pull/165)
- Log messages are no longer trimmed.
  | [#157](https://github.com/JetBrains/bazel-bsp/pull/157)
- Memoize BazelProcess output so that it doesn't get lost.
  | [#154](https://github.com/JetBrains/bazel-bsp/pull/154)

### Changes 🔄

- `bazel info` call caching mechanism.
  | [#228](https://github.com/JetBrains/bazel-bsp/pull/228)
- Logging level has been set to info and doesn't include events.
  | [#223](https://github.com/JetBrains/bazel-bsp/pull/223)
- Deletion of semantic version class.
  | [#216](https://github.com/JetBrains/bazel-bsp/pull/216)
- Duplicate bazel output lines are not shown in bsp-client.
  | [#209](https://github.com/JetBrains/bazel-bsp/pull/209)
- Project uses bazel `5.1.0`.
  | [#208](https://github.com/JetBrains/bazel-bsp/pull/208)
- Parse bazel query output from stream rather than from all bytes.
  | [#210](https://github.com/JetBrains/bazel-bsp/pull/210)
- JUnit5!
  | [#206](https://github.com/JetBrains/bazel-bsp/pull/206)
- Do not throw on aborted event from BEP. Show warning instead.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Readable `toString` for all `Project` related classes.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Measure and report time of longer operations during sync.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Support for excluded targets in the sync mechanism.
  | [#196](https://github.com/JetBrains/bazel-bsp/pull/196)
- Introduction of installation context.
  | [#184](https://github.com/JetBrains/bazel-bsp/pull/184)
- Project uses (mostly) `io.vavr.Option` and `io.vavr.List`.
  | [#192](https://github.com/JetBrains/bazel-bsp/pull/192)
- Introducing execution context and workspace context.
  | [#172](https://github.com/JetBrains/bazel-bsp/pull/172)
- Changed the structure of the [README](README.md) and other documents and added contribution guide.
  | [#181](https://github.com/JetBrains/bazel-bsp/pull/181)
- Project view parser has more logging, targets section is optional and
  sections are using specific types instead of raw strings.
  | [#166](https://github.com/JetBrains/bazel-bsp/pull/166)
- Improve the `install.sh` script.
  | [#167](https://github.com/JetBrains/bazel-bsp/pull/167)
- Now the project is using the latest bazel version - `5.0.0`.
  Installer skips bazelisk cache binaries during bazel binary discovery mechanism.
  | [#160](https://github.com/JetBrains/bazel-bsp/pull/160)
- Rewrite of all endpoints to rely on single bazel aspect to extract all necessary data.
  It improves performance and correctness as wel as extensibility of the server.
  With this change also c++ support is temporarily dropped.
  | [#147](https://github.com/JetBrains/bazel-bsp/pull/147)

## [1.1.1] - 16.02.2022

### Features 🎉

- New, better project view! Now you can configure installer as well!
  | [#143](https://github.com/JetBrains/bazel-bsp/pull/143)
- Server returns only the relevant output jar rather than 5-6 jars.
  | [#136](https://github.com/JetBrains/bazel-bsp/pull/136)
- Server supports multiple subprojects in one workspace (first step).
  | [#130](https://github.com/JetBrains/bazel-bsp/pull/130)
- `JvmBuildServer` implementation - now tests execution should be working!
  | [#128](https://github.com/JetBrains/bazel-bsp/pull/128)
- Improved heuristics for guessing source roots. It looks directory structures such as `src/java` or `main/java`.
  | [#126](https://github.com/JetBrains/bazel-bsp/pull/126)

### Changes 🔄

- Server filters out non-runtime jars for running apps and tests.
  | [#131](https://github.com/JetBrains/bazel-bsp/pull/131)
- Now server uses Java 11.
  | [#129](https://github.com/JetBrains/bazel-bsp/pull/129)
- Bazel runner allows running a bazel command without positional arguments.
  | [#123](https://github.com/JetBrains/bazel-bsp/pull/123)

### Fixes 🛠️

- Error diagnostics are now also sent for source files, including targets.
  | [#146](https://github.com/JetBrains/bazel-bsp/pull/146)
- Badges in the README work now (and there are even more of them).
  | [#142](https://github.com/JetBrains/bazel-bsp/pull/142)
- Server builds a project during sync in order to fix dependency resolving mechanism.
  | [#137](https://github.com/JetBrains/bazel-bsp/pull/137)
- Server handles aspects properly when multiple projects are in workspace.
  | [#132](https://github.com/JetBrains/bazel-bsp/pull/132)
- Now the project is built using bazel version `3.7.2`, as the rules currently used are no longer supported by bazel.
  | [#141](https://github.com/JetBrains/bazel-bsp/pull/141)

## [1.0.1] - 24.09.2021

### Features 🎉

- [This](CHANGELOG.md) changelog.
  | [#97](https://github.com/JetBrains/bazel-bsp/pull/97)

### Changes 🔄

- Implementation of e2e tests. Have been moved to the `e2e` module and are now based on execution scenarios.
  | [#83](https://github.com/JetBrains/bazel-bsp/pull/83)
- Github actions e2e tests execution - e2e tests are executed in parallel.
  | [#84](https://github.com/JetBrains/bazel-bsp/pull/84)
- Project structure - now it is multi module project.
  | [#87](https://github.com/JetBrains/bazel-bsp/pull/87)
- Github actions unit tests execution - each module has a separate job.
  | [#89](https://github.com/JetBrains/bazel-bsp/pull/89)
- Created [document](docs/dev/BUMPVERSION.md) with release tips.
  | [#91](https://github.com/JetBrains/bazel-bsp/pull/91)

### Removed ✂️

- Old implementation of e2e tests.
  | [#86](https://github.com/JetBrains/bazel-bsp/pull/86)

### Fixes 🛠️

- Warnings generated by the [WORKSPACE file](WORKSPACE).
  | [#81](https://github.com/JetBrains/bazel-bsp/pull/81)
- Semantic versioning parser - now it can parse every valid version.
  | [#93](https://github.com/JetBrains/bazel-bsp/pull/93)
- `exports` attribute propagation to the BSP
  | [#98](https://github.com/JetBrains/bazel-bsp/pull/98)
- Now all `scala_junit_test` based rules (including `scala_specs2_junit_test`) are included in the BSP tests,
  unfortunately without test classes.
  | [#101](https://github.com/JetBrains/bazel-bsp/pull/101)

## [1.0.0] - 23.08.2021

- Everything... 🎉

[Unreleased]: https://github.com/JetBrains/bazel-bsp/compare/2.7.2...HEAD

[2.7.2]: https://github.com/JetBrains/bazel-bsp/compare/2.7.1..2.7.2

[2.7.1]: https://github.com/JetBrains/bazel-bsp/compare/2.7.0..2.7.1

[2.7.0]: https://github.com/JetBrains/bazel-bsp/compare/2.6.1..2.7.0

[2.6.1]: https://github.com/JetBrains/bazel-bsp/compare/2.6.0..2.6.1

[2.6.0]: https://github.com/JetBrains/bazel-bsp/compare/2.5.1..2.6.0

[2.5.1]: https://github.com/JetBrains/bazel-bsp/compare/2.5.0..2.5.1

[2.5.0]: https://github.com/JetBrains/bazel-bsp/compare/2.4.0..2.5.0

[2.4.0]: https://github.com/JetBrains/bazel-bsp/compare/2.3.1...2.4.0

[2.3.1]: https://github.com/JetBrains/bazel-bsp/compare/2.3.0...2.3.1

[2.3.0]: https://github.com/JetBrains/bazel-bsp/compare/2.2.1...2.3.0

[2.2.1]: https://github.com/JetBrains/bazel-bsp/compare/2.2.0...2.2.1

[2.2.0]: https://github.com/JetBrains/bazel-bsp/compare/2.1.0...2.2.0

[2.1.0]: https://github.com/JetBrains/bazel-bsp/compare/2.0.0...2.1.0

[2.0.0]: https://github.com/JetBrains/bazel-bsp/compare/1.1.1...2.0.0

[1.1.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.1...1.1.1

[1.0.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.0...1.0.1

[1.0.0]: https://github.com/JetBrains/bazel-bsp/releases/tag/1.0.0
