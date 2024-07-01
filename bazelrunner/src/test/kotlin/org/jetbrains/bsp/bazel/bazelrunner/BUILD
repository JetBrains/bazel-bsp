load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "BazelReleaseTest",
    size = "small",
    src = "BazelReleaseTest.kt",
    resources = ["//bazelrunner/src/test/resources:workspace"],
    deps = [
        "//bazelrunner",
    ],
)
