load("@rules_java//java:defs.bzl", "java_library", "java_test")

java_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = ["ProjectViewParserImplTest.java"],
    resources = ["//projectview/src/test/resources:projectview"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "//projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser:parser_mock_test_impl",
    ],
)

java_library(
    name = "parser_mock_test_impl",
    testonly = 1,
    srcs = ["ProjectViewParserMockTestImpl.java"],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
    ],
)
