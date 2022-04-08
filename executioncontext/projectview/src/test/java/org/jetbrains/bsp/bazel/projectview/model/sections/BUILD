load("//:junit5.bzl", "java_junit5_test", "kt_junit5_test")

kt_junit5_test(
    name = "ProjectViewListSectionTest",
    size = "small",
    srcs = ["ProjectViewListSectionTest.kt"],
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
    ],
)

kt_junit5_test(
    name = "ProjectViewExcludableListSectionTest",
    size = "small",
    srcs = ["ProjectViewExcludableListSectionTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.projectview.model.sections",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
    ],
)
