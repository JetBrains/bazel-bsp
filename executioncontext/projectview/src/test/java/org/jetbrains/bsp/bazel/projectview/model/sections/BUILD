load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewSingletonSectionTest",
    size = "small",
    srcs = ["ProjectViewSingletonSectionTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
)

java_test(
    name = "ProjectViewListSectionTest",
    size = "small",
    srcs = ["ProjectViewListSectionTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "@maven//:org_assertj_assertj_core",
    ],
)

java_test(
    name = "ProjectViewExcludableListSectionTest",
    size = "small",
    srcs = ["ProjectViewExcludableListSectionTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
)
