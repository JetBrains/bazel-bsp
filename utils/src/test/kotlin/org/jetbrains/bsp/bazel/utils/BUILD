load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "DopeFilesTest",
    size = "small",
    srcs = ["DopeFilesTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.utils",
    deps = [
        "//utils/src/main/kotlin/org/jetbrains/bsp/bazel/utils:dope_files",
    ],
)
