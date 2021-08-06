load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "DirectoriesSectionParserTest",
    size = "small",
    srcs = ["DirectoriesSectionParserTest.java"],
    resources = ["//src/test/resources:bsp-test-resources"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections/specific",
    ],
)

java_test(
    name = "TargetsSectionParserTest",
    size = "small",
    srcs = ["TargetsSectionParserTest.java"],
    resources = ["//src/test/resources:bsp-test-resources"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections/specific",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections",
        "//src/main/java/org/jetbrains/bsp/bazel/projectview/parser/sections/specific",
    ],
)
