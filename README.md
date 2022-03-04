[![JetBrains team project](http://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Format](https://github.com/JetBrains/bazel-bsp/actions/workflows/format.yml/badge.svg)](https://github.com/JetBrains/bazel-bsp/actions/workflows/format.yml)
[![Integration tests](https://github.com/JetBrains/bazel-bsp/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/JetBrains/bazel-bsp/actions/workflows/integration-tests.yml)
[![Unit tests](https://github.com/JetBrains/bazel-bsp/actions/workflows/unit-tests.yml/badge.svg)](https://github.com/JetBrains/bazel-bsp/actions/workflows/unit-tests.yml)

# Bazel BSP

An implementation of the [Build Server Protocol](https://github.com/build-server-protocol/build-server-protocol) for
Bazel.

## Status

Below is a list of languages supported over Bazel BSP and their implementation status.

- Language: name of the Language
- Import: Ability to import a project's targets, sources, dependencies, and resources.
- Compilation: Ability to compile
- Run: Ability to run
- Test: Ability to test
- Prerequisites: Any prerequisites needed to properly run the server to its full capabilities

| Language | Import | Compilation | Run | Test | Diagnostics | Prerequisites | Notes | 
| - | - | - | - | - | - | - | - |
| Scala | ✅ | ✅ | ✅ | ✅ | ✅ | [Toolchain Registration](docs/usage/scala.md) | N/A | 
| Java | ✅ | ✅ | ✅ | ✅ | ❌ | N/A | N/A | 
| Kotlin | ✅ | ✅ | ✅ | ✅ | ✅ | Requires [this version](https://github.com/agluszak/rules_kotlin/tree/diagnostics-updated) of rules_kotlin | KotlinJS support is minimal and not advised without further setting changes. Java source files in a kotlin rule will not possess diagnostics. |

## Installation

### Easy way (coursier)

1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Run in the directory where Bazel BSP should be installed:

```
cs launch org.jetbrains.bsp:bazel-bsp:1.1.1 -M org.jetbrains.bsp.bazel.install.Install
```

3. Add bsp generated folders to your `.gitignore`: `.bsp` and `.bazelbsp`

### More difficult way (from sources)

Might be useful during development

#### Using install script

1. Be inside this project
2. Run `./install.sh` if you want to install Bazel BSP in this project or `./install.sh <path to the directory>` if you
   want to install it in a different directory

#### Using coursier

1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Be inside this project
3. **Change** the project version - `maven_coordinates` attribute in
   the `server/src/main/java/org/jetbrains/bsp/bazel/BUILD` file
4. Publish a new version:

```
bazel run --stamp --define "maven_repo=file://$HOME/.m2/repository" //server/src/main/java/org/jetbrains/bsp/bazel:bsp.publish
```

7. Enter directory where Bazel BSP should be installed
8. Install your version:

```
cs launch -r m2Local org.jetbrains.bsp:bazel-bsp:<your version> -M org.jetbrains.bsp.bazel.install.Install
```

## Project Views

In order to work on huge monorepos you might want to specify directories and targets to work on. To address this issue,
Bazel BSP supports (partly) the [Project Views](https://ij.bazel.build/docs/project-views.html) introduced by Google.

Check [project view readme](projectview/README.md) for more info.

## Tests

### End-to-end tests

`e2e` directory contains end-2-end tests that check various scenarios of server usage.

- `bazel run //e2e:all` - to run all tests
- `bazel run //e2e:<specific test>` - to run a specific test (to see all possible tests, check the `e2e/BUILD` file)

### Unit tests

Most modules also have unit tests that can be run using `bazel test //<module>/...` or just `bazel test //...` to run
all tests in the project.

## Contributing

Want to contribute? Great! Follow [these rules](docs/dev/CONTRIBUTING.md).
