[![JetBrains team project](http://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Integration test](https://github.com/JetBrains/bazel-bsp/actions/workflows/integration-test.yml/badge.svg)](https://github.com/JetBrains/bazel-bsp/actions/workflows/integration-test.yml)

# Bazel BSP
An implementation of the [Build Server Protocol](https://github.com/build-server-protocol/build-server-protocol) for Bazel.


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
| Scala | ✅ | ✅ | ✅ | ✅ | ✅ | [Toolchain Registration](docs/scala.md) | N/A |
| Java | ✅ | ✅ | ✅ | ✅ | ❌ | N/A | N/A |
| Kotlin | ✅ | ✅ | ✅ | ✅ | ✅ | Requires [this version](https://github.com/agluszak/rules_kotlin/tree/diagnostics-updated) of rules_kotlin | KotlinJS support is minimal and not advised without further setting changes. Java source files in a kotlin rule will not possess diagnostics. |


## Installation
### Easy way (coursier)
1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Run in the  direcotry where Bazel BSP should be installed: 
```
cs launch --no-default -r m2Local -r central org.jetbrains.bsp:bazel-bsp:0.1.0 -M org.jetbrains.bsp.bazel.install.Install
```
3. Add bsp generated folders to your `.gitignore`: `.bsp` and `.bazelbsp`

### More difficult way (from sources)
Might be useful during development
#### Using install script
1. Be inside this project
2. Run `./install.sh` if you want to install Bazel BSP in this project or `./install.sh <path to the directory>` if you want to install it in a different directory

#### Using coursier
1. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
2. Be inside this project
3. Change project version - `maven_coordinates` attribute in the `src/main/java/org/jetbrains/bsp/bazel/BUILD` file
4. Publish a new version: 
```
bazel run --stamp --define "maven_repo=file://$HOME/.m2/repository" //src/main/java/org/jetbrains/bsp/bazel:bsp.publish
```
7. Enter directory where Bazel BSP should be installed
8. Install your version:
```
cs launch --no-default -r m2Local -r central org.jetbrains.bsp:bazel-bsp:<your version> -M org.jetbrains.bsp.bazel.install.Install
```


## Project Views
In order to work on huge monorepos you might want to specify directories and targets to work on. To address this issue, Bazel BSP supports part of the [Project Views](https://ij.bazel.build/docs/project-views.html) introduced by Google. Currently you can use following rules: `directories`, `targets` and `import`. 

Simply create a `projectview.bazelproject` file, specify rules inside and run the server. If no such files will be found, by default entire project will be loaded. 


## Extending
In order to extend BSP server to other languages, make sure it can be supported with the current state of the  [BSP Protocol](https://github.com/build-server-protocol/build-server-protocol/tree/master/docs). Also, make sure there's a [client](https://build-server-protocol.github.io/docs/implementations.html#build-clients), that will be able to support those changes.

For any JVM-language, the only needed changes would be for its specific Compiler Options Request (see, for example the [Scala Options Request](https://github.com/build-server-protocol/build-server-protocol/blob/master/docs/extensions/scala.md#scalac-options-request)). If that language does not have its own Options Request, it may be possible to mimic the behavior with current Java or Scala specific requests. Furthermore, make sure the `FILE_EXTENSIONS` constant holds all the relevant extensions for the given language, as well as the `KNOWN_SOURCE_ROOTS` holds all known patterns for the given language, and, finally, the `SUPPORTED_LANGUAGES` should hold the LSP compliant name of the language. Anything else, should just work out of the box.

Any non-JVM language, will also need to look into the `buildTarget/dependencySources` request, since that request only searches for transitive dependencies in the form of jars.

To support Compilation Diagnostics for the given language, they must be supported in the bazel side. Find the rules' implementation for the language [here](https://github.com/bazelbuild/). Let the [diagnostics proto definition](https://github.com/bazelbuild/rules_scala/blob/master/src/protobuf/io/bazel/rules_scala/diagnostics.proto) live inside its repository and make it so that the compiler writes the diagnostics to the given file. **Make sure the file is an output of the `ctx.actions.run()` for the compilation action**. The current state also dictates that the file must have the keyword `diagnostics` in the name. In `BepServer.java`, the mnemonic of the compilation action of the language must be added to the `SUPPORTED_ACTIONS` constant. 

Compilation Diagnostics should receive native support from bazel, accompany the state of that [here](https://github.com/bazelbuild/bazel/pull/11766).


## Contributing
This project follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). You can download a formatter plugin for Intellij [here](https://plugins.jetbrains.com/plugin/8527-google-java-format).
