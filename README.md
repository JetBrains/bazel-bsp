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
| Kotlin | ✅ | ✅ | ❌ | ❌ | N/A | N/A |

## Installation
1. Please make sure to have followed the prerequisites guide before proceeding.
2. Clone this project and enter its folder:     
``git clone https://github.com/andrefmrocha/bazel-bsp.git && cd bazel-bsp``
3. Build the BSP Server:        
``bazel build //main/src/org/jetbrains/bsp:bsp_deploy.jar``
4. Note the current path to the project:        
``bazel_bsp=$(pwd)``
5. Change into the directory of your project
6. Install the BSP Server:      
``
java -cp $bazel_bsp/bazel-bin/main/src/org/jetbrains/bsp/bsp_deploy.jar org.jetbrains.bsp.Main install
``

## Extending
In order to extend BSP server to other languages, make sure it can be supported with the current state of the  [BSP Protocol](https://github.com/build-server-protocol/build-server-protocol/tree/master/docs). Also, make sure there's a [client](https://build-server-protocol.github.io/docs/implementations.html#build-clients), that will be able to support those changes.

The file `BazelBspServer.java`, holds the implementation of the server itself. For any JVM-language, the only needed changes would be for its specific Compiler Options Request (see, for example the [Scala Options Request](https://github.com/build-server-protocol/build-server-protocol/blob/master/docs/extensions/scala.md#scalac-options-request)). Anything else, should just work out of the box.

Any non-JVM language, will also need to look into the `buildTarget/dependencySources` request, since that request only searches for transitive dependencies in the form of jars.

To support Compilation Diagnostics for the given language, they must be supported in the bazel side. Find the rules' implementation for the language [here](https://github.com/bazelbuild/). Let the [diagnostics proto definition](https://github.com/bazelbuild/rules_scala/blob/master/src/protobuf/io/bazel/rules_scala/diagnostics.proto) live inside its repository and make it so that the compiler writes the diagnostics to the given file. **Make sure the file is an output of the `ctx.actions.run()` for the compilation action**. The current state also dictates that the file must have the keyword `diagnostics` in the name. In `BepServer.java#processActionDiagnostics`, the name of the compilation action of the language must be added as an option as well.

Compilation Diagnostics should receive native support from bazel, accompany the state of that [here](https://github.com/bazelbuild/bazel/pull/11766).