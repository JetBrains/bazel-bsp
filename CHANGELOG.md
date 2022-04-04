# Changelog

<!-- Keep a Changelog guide -> https://keepachangelog.com -->

## [Unreleased]

### Features 🎉

- Server now keeps the state of the project between runs.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Improved bazel runner. The BSP Client now receives invoked bazel command,
  its output, duration and exit code.
  | [#198](https://github.com/JetBrains/bazel-bsp/pull/198)
- Scala diagnostics work without a fork (technically it worked already for a while).
  | [#170](https://github.com/JetBrains/bazel-bsp/pull/170)

### Fixes 🛠️

- Fixed extraction of java version and java home for bazel `5.0.0`.
  | [#165](https://github.com/JetBrains/bazel-bsp/pull/165)
- Log messages are no longer trimmed.
  | [#157](https://github.com/JetBrains/bazel-bsp/pull/157)
- Memoize BazelProcess output so that it doesn't get lost.
  | [#154](https://github.com/JetBrains/bazel-bsp/pull/154)

### Changes 🔄

- Project uses bazel `5.1.0`.
  | [#208](https://github.com/JetBrains/bazel-bsp/pull/208)
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

[Unreleased]: https://github.com/JetBrains/bazel-bsp/compare/1.1.1...HEAD

[1.1.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.1...1.1.1

[1.0.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.0...1.0.1

[1.0.0]: https://github.com/JetBrains/bazel-bsp/releases/tag/1.0.0
