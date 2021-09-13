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
