load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "ProjectStorageTest",
    size = "small",
    src = "ProjectStorageTest.kt",
    deps = [
        "//logger",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_test(
    name = "DependencyMapperTest",
    size = "small",
    src = "DependencyMapperTest.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)
