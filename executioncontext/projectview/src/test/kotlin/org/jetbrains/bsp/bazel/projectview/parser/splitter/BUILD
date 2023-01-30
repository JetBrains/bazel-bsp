load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "ProjectViewRawSectionsTest",
    size = "small",
    srcs = ["ProjectViewRawSectionsTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser.splitter",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
)

kt_junit5_test(
    name = "ProjectViewSectionSplitterTest",
    size = "small",
    srcs = ["ProjectViewSectionSplitterTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.projectview.parser.splitter",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
)
