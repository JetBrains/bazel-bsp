load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DefaultProjectViewGeneratorTest",
    size = "small",
    src = "DefaultProjectViewGeneratorTest.kt",
    deps = [
        "//executioncontext/projectview:parser",
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/generator",
        "//utils:dope",
    ],
)
