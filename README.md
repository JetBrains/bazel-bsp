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

| Language | Import | Compilation | Run | Test | Prerequisites | Notes |
| - | - | - | - | - | - | - |
| Scala | ✅ | ✅ | ❌ | ❌ | [Toolchain Registration](docs/scala.md) | N/A |
| Java | ✅ | ✅ | ❌ | ❌ | N/A | Compilation does not offer diagnostics |
| Kotlin | ✅ | ✅ | ❌ | ❌ | [Version](docs/kotlin.md) | KotlinJS support is minimal and not advised without further setting changes. Java source files in a kotlin rule will not possess diagnostics. |

## Installation
1. Please make sure to have followed the prerequisites guide before proceeding.
2. Have [coursier](https://get-coursier.io/docs/cli-installation) installed
3. Run
`coursier launch -r sonatype:snapshots org.jetbrains.bsp:bazel-bsp:0.1.0-SNAPSHOT -M org.jetbrains.bsp.bazel.Install`
4. Add bsp generated folders to your `gitignore`: `.bsp` and `.bazelbsp`

## Extending
In order to extend BSP server to other languages, make sure it can be supported with the current state of the  [BSP Protocol](https://github.com/build-server-protocol/build-server-protocol/tree/master/docs). Also, make sure there's a [client](https://build-server-protocol.github.io/docs/implementations.html#build-clients), that will be able to support those changes.

The file `BazelBspServer.java`, holds the implementation of the server itself. For any JVM-language, the only needed changes would be for its specific Compiler Options Request (see, for example the [Scala Options Request](https://github.com/build-server-protocol/build-server-protocol/blob/master/docs/extensions/scala.md#scalac-options-request)). If that language does not have its own Options Request, it may be possible to mimic the behavior with current Java or Scala specific requests. Furthermore, make sure the `FILE_EXTENSIONS` constant holds all the relevant extensions for the given language, as well as the `KNOWN_SOURCE_ROOTS` holds all known patterns for the given language, and, finally, the `SUPPORTED_LANGUAGES` should hold the LSP compliant name of the language. Anything else, should just work out of the box.

Any non-JVM language, will also need to look into the `buildTarget/dependencySources` request, since that request only searches for transitive dependencies in the form of jars.

To support Compilation Diagnostics for the given language, they must be supported in the bazel side. Find the rules' implementation for the language [here](https://github.com/bazelbuild/). Let the [diagnostics proto definition](https://github.com/bazelbuild/rules_scala/blob/master/src/protobuf/io/bazel/rules_scala/diagnostics.proto) live inside its repository and make it so that the compiler writes the diagnostics to the given file. **Make sure the file is an output of the `ctx.actions.run()` for the compilation action**. The current state also dictates that the file must have the keyword `diagnostics` in the name. In `BepServer.java`, the mnemonic of the compilation action of the language must be added to the `SUPPORTED_ACTIONS` constant. 

Compilation Diagnostics should receive native support from bazel, accompany the state of that [here](https://github.com/bazelbuild/bazel/pull/11766).

## Contributing
This project follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html). You can download a formatter plugin for Intellij [here](https://plugins.jetbrains.com/plugin/8527-google-java-format).
