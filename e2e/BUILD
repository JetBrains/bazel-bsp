# we cannot use `bazel test ...` because testrunner blocks bazel deamon,
# but testing server needs it for queries and etc

sh_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["runTest.sh"],
    args = [
        "e2e/test-resources/sample-repo",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
    ],
)

sh_binary(
    name = "BazelBspActionGraphV1Test",
    srcs = ["runTest.sh"],
    args = [
        "e2e/test-resources/sample-repo",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV1Test",
    ],
)
