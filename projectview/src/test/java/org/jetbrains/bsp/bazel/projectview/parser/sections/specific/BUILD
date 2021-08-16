load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "DirectoriesSectionParserTest",
    size = "small",
    srcs = ["DirectoriesSectionParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview:model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections/specific",
    ],
)

java_test(
    name = "TargetsSectionParserTest",
    size = "small",
    srcs = ["TargetsSectionParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview:model",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections/specific",
    ],
)
