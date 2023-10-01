load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "SourceRootGuesserTest",
    size = "small",
    src = "SourceRootGuesserTest.kt",
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)

kt_test(
    name = "InternalAspectsResolverTest",
    size = "small",
    src = "InternalAspectsResolverTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/info",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)
