load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "ProjectViewListSectionTest",
    size = "small",
    srcs = ["ProjectViewListSectionTest.java"],
    test_package = "org.jetbrains.bsp.bazel.projectview.model.sections",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
)

java_junit5_test(
    name = "ProjectViewSingletonSectionTest",
    size = "small",
    srcs = ["ProjectViewSingletonSectionTest.java"],
    test_package = "org.jetbrains.bsp.bazel.projectview.model.sections",
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
