load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "BuildRuleAttributeExtractorTest",
    size = "small",
    srcs = ["BuildRuleAttributeExtractorTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:org_assertj_assertj_core",
    ],
)

java_test(
    name = "SourceRootGuesserTest",
    size = "small",
    srcs = ["SourceRootGuesserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)

java_test(
    name = "InternalAspectsResolverTest",
    size = "small",
    srcs = ["InternalAspectsResolverTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//commons",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bsp/utils",
    ],
)
