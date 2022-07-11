load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "bazelrunner",
    size = "small",
    srcs = glob(["*.kt"]),
    test_package = "org.jetbrains.bsp.bazel.bazelrunner",
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner",
        "//utils:dope",
    ],
)
