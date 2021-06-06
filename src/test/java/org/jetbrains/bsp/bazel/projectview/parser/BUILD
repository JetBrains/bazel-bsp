load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewParserImplTest",
    size = "small",
    srcs = [
        "ProjectViewParserImplTest.java",
        "ProjectViewParserMockTestImpl.java",
    ],
    resources = ["//src/test/resources/projectview:projectview-resources"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser",
    ],
)
