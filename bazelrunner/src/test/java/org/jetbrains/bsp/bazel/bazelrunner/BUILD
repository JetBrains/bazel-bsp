load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "StoredBazelInfoTest",
    size = "small",
    srcs = ["StoredBazelInfoTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.bazelrunner",
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner",
        "//utils:dope",
    ],
)

kt_junit5_test(
    name = "BazelReleaseTest",
    size = "small",
    srcs = ["BazelReleaseTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.bazelrunner",
    deps = [
        "//bazelrunner/src/main/java/org/jetbrains/bsp/bazel/bazelrunner",
        "//utils:dope",
    ],
)
