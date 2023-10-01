# Bump Version

If you want to bump version (for example, before a release) you should do this in the following places:

- `central-sync/VERSION`
- `maven_coordinates` in `server/src/main/kotlin/org/jetbrains/bsp/bazel/BUILD`
- `VERSION` in `commons/src/main/kotlin/org/jetbrains/bsp/bazel/commons/Constants.java`
