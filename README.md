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

| Language | Import | Compilation | Run | Test | Diagnostics |
| - | - | - | - | - | - |
| Scala | ✅ | ✅ | ✅ | ✅ | ✅ |
| Java | ✅ | ✅ | ✅ | ✅ | ✅ |
| Kotlin | ✅ | ✅ | ✅ | ✅ | ✅ |

## Installation

### Easy way (coursier)

1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Run in the directory where Bazel BSP should be installed:

```shell
cs launch org.jetbrains.bsp:bazel-bsp:2.3.1 -M org.jetbrains.bsp.bazel.install.Install
```

3. Add bsp generated folders to your `.gitignore`: `.bsp` and `.bazelbsp`

### More difficult way (from sources)

Might be useful during development

#### Using install script

1. Be inside this project
2. Run `./install.sh <installer flags>` (`--help` is available)

#### Using coursier

1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Be inside this project
3. **Change** the project version - `maven_coordinates` attribute in
   the `server/src/main/java/org/jetbrains/bsp/bazel/BUILD` file
4. Publish a new version:

```shell
bazel run --stamp --define "maven_repo=file://$HOME/.m2/repository" //server/src/main/java/org/jetbrains/bsp/bazel:bsp.publish
```

7. Enter directory where Bazel BSP should be installed
8. Install your version:

```shell
cs launch -r m2Local org.jetbrains.bsp:bazel-bsp:<your version> -M org.jetbrains.bsp.bazel.install.Install
```

### Using Bloop

By default Bazel BSP runs as a BSP server and invokes Bazel to compile, test and run targets. 
This provides the most accurate build results at the expense of  
compile/test/run latency.  Bazel BSP can optionally be configured to use [Bloop](https://scalacenter.github.io/bloop/) 
as the BSP server instead. Bloop provides a much lower latency with the trade-off that the Bloop model
may not perfectly represent the Bazel configuration.

#### Installing with Bloop

The instructions above will work in Bloop mode as well, simply pass ``--use_bloop`` as an installation option.
However, when using Bloop mode Bazel BSP can also install itself outside the source root directory.  This can
be useful in large shared repositories where it's undesirable to keep the Bazel BSP projects inside the 
repository itself.

In the examples below, we'll use ``~/src/my-repo`` as the "repository root" and ``~/bazel-bsp-projects`` as the 
"Bazel BSP project root", however both can be any directory.

To install Bazel BSP outside the repository root:

1) Change directories into the repository root: ``cd ~/src/my-repo``
2) Invoke the Bazel BSP installer as described above (via Coursier or run the installer JAR directly), passing in:
   1) ``--use-bloop``
   2) ``-d ~/bazel-bsp-projects/my-repo-project``
   
For example, using Coursier:

```shell
cd ~/src/my-repository
cs launch org.jetbrains.bsp:bazel-bsp:2.3.1 -M org.jetbrains.bsp.bazel.install.Install \
  -- \
  --use-bloop \
  -t //my-targets/... \
  -d ~/bazel-bsp-projects/my-targets-project 
```

This will create a set of BSP and Bloop projects in ``~/bazel-bsp-projects/my-targets-project`` which can then be opened 
in IntelliJ or any other IDE that supports BSP.  

## Project Views

In order to work on huge monorepos you might want to specify directories and targets to work on. To address this issue,
Bazel BSP supports (partly) the [Project Views](https://ij.bazel.build/docs/project-views.html) introduced by Google.

Check [project view readme](executioncontext/projectview/README.md) for more info.

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
