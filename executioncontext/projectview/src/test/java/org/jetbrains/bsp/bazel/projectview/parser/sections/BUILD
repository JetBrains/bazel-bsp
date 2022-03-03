load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewListSectionParserTest",
    size = "small",
    srcs = ["ProjectViewListSectionParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
)

java_test(
    name = "ProjectViewSingletonSectionParserTest",
    size = "small",
    srcs = ["ProjectViewSingletonSectionParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
)
