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
