load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = [
        "ProjectViewParserImplTest.java",
        "ProjectViewParserMockTestImpl.java",
    ],
    resources = ["//projectview/src/test/resources/projectview"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview:model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)
