load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "StoredBazelInfoTest",
    size = "small",
    src = "StoredBazelInfoTest.kt",
    deps = [
        "//bazelrunner/src/main/kotlin/org/jetbrains/bsp/bazel/bazelrunner",
    ],
)

kt_test(
    name = "BazelReleaseTest",
    size = "small",
    src = "BazelReleaseTest.kt",
    resources = ["//bazelrunner/src/test/resources:workspace"],
    deps = [
        "//bazelrunner/src/main/kotlin/org/jetbrains/bsp/bazel/bazelrunner",
    ],
)
