load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DependencyMapperTest",
    size = "small",
    src = "DependencyMapperTest.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync",
    ],
)
