load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DependencyGraphTest",
    size = "small",
    src = "DependencyGraphTest.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/dependencygraph",
    ],
)
