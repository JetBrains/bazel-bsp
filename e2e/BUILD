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

sh_binary(
    name = "BazelBspActionGraphV2Test",
    srcs = ["runTest.sh"],
    args = [
        "e2e/test-resources/sample-repo",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV2Test",
    ],
)

sh_binary(
    name = "BazelBspJava8ProjectTest",
    srcs = ["runTest.sh"],
    args = [
        "e2e/test-resources/sample-repo",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava8ProjectTest",
    ],
)

sh_binary(
    name = "BazelBspJava11ProjectTest",
    srcs = ["runTest.sh"],
    args = [
        "e2e/test-resources/sample-repo",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava11ProjectTest",
    ],
)
