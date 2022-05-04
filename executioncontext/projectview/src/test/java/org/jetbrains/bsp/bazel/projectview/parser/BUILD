load("//:junit5.bzl", "kt_junit5_test")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_junit5_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = ["ProjectViewParserImplTest.kt"],
    resources = ["//executioncontext/projectview/src/test/resources:projectview"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "//executioncontext/projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:parser_mock_test_impl",
    ],
)

kt_jvm_library(
    name = "parser_mock_test_impl",
    testonly = 1,
    srcs = ["ProjectViewParserMockTestImpl.kt"],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)
