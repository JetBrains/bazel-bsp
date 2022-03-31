load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "ProjectViewListSectionParserTest",
    size = "small",
    srcs = ["ProjectViewListSectionParserTest.java"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser.sections",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
)

java_junit5_test(
    name = "ProjectViewSingletonSectionParserTest",
    size = "small",
    srcs = ["ProjectViewSingletonSectionParserTest.java"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser.sections",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
    ],
)
