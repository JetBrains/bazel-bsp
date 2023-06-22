load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "ProjectStorageTest",
    size = "small",
    src = "ProjectStorageTest.kt",
    deps = [
        "//logger",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "JUnit5TestParserTest",
    size = "small",
    src = "JUnit5TestParserTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)
