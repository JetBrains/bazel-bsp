# Aspects

## Extensions

### The `extensions.bzl` file
The server generates the `extensions.bzl` file on each sync according to a list of external rules relevant to the project.
See `server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/managers/BazelBspEnvironmentManager.kt` for the implementation.

The file itself contains a declaration of functions invoked in `core.bzl`. Each function is supposed to provide language-specific data.
