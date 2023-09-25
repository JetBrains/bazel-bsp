load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DependencyTreeTest",
    size = "small",
    src = "DependencyTreeTest.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/dependencytree",
    ],
)
