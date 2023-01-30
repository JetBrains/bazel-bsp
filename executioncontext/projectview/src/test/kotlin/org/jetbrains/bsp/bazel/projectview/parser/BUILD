load("//:junit5.bzl", "kt_junit5_test")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_junit5_test(
    name = "DefaultProjectViewParserTest",
    size = "small",
    srcs = ["DefaultProjectViewParserTest.kt"],
    resources = ["//executioncontext/projectview/src/test/resources:projectview"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:parser_test_mock",
    ],
)

kt_jvm_library(
    name = "parser_test_mock",
    testonly = 1,
    srcs = ["ProjectViewParserTestMock.kt"],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)
