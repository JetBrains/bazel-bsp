
# Changelog

<!-- Keep a Changelog guide -> https://keepachangelog.com -->


## [Unreleased]

### Changes 🔄

- Add async and sync output processors.
  | [#288](https://github.com/JetBrains/bazel-bsp/pull/288)
- Pass originId into diagnostics.
  | [#291](https://github.com/JetBrains/bazel-bsp/pull/291)
- Pass originId into server responses.
  | [#289](https://github.com/JetBrains/bazel-bsp/pull/289)

### Fixes 🛠️

- `JVMLanguagePluginParser.calculateJVMSourceRoot` does not throw 
  an exception for package longer than the path.
  | [#294](https://github.com/JetBrains/bazel-bsp/pull/294)
- Fix transitive target failure check in bloop export.
  | [#287](https://github.com/JetBrains/bazel-bsp/pull/287)

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

[Unreleased]: https://github.com/JetBrains/bazel-bsp/compare/2.2.1...HEAD

[2.2.1]: https://github.com/JetBrains/bazel-bsp/compare/2.2.0...2.2.1

[2.2.0]: https://github.com/JetBrains/bazel-bsp/compare/2.1.0...2.2.0

[2.1.0]: https://github.com/JetBrains/bazel-bsp/compare/2.0.0...2.1.0

[2.0.0]: https://github.com/JetBrains/bazel-bsp/compare/1.1.1...2.0.0

[1.1.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.1...1.1.1

[1.0.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.0...1.0.1

[1.0.0]: https://github.com/JetBrains/bazel-bsp/releases/tag/1.0.0
