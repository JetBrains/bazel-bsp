load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewTargetsSectionParserTest",
    size = "small",
    srcs = ["ProjectViewTargetsSectionParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview:model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
)
