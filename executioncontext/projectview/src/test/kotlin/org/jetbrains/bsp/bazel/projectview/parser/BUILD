load("@//rules/kotlin:junit5.bzl", "kt_test")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_test(
    name = "DefaultProjectViewParserTest",
    size = "small",
    src = "DefaultProjectViewParserTest.kt",
    resources = ["//executioncontext/projectview/src/test/resources:projectview"],
    deps = [
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview/parser",
        "//executioncontext/projectview/src/test/kotlin/org/jetbrains/bsp/bazel/projectview/parser:parser_test_mock",
    ],
)

kt_jvm_library(
    name = "parser_test_mock",
    testonly = 1,
    srcs = ["ProjectViewParserTestMock.kt"],
    deps = [
        "//executioncontext/projectview/src/main/kotlin/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)
