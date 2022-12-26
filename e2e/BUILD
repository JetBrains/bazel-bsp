# we cannot use `bazel test ...` because testrunner blocks bazel deamon,
# but testing server needs it for queries and etc

sh_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
        "e2e/test-resources/sample-repo",
    ],
)

sh_binary(
    name = "BazelBspActionGraphV1Test",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV1Test",
        "e2e/test-resources/sample-repo",
    ],
)

sh_binary(
    name = "BazelBspActionGraphV2Test",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV2Test",
        "e2e/test-resources/action-graph-v2",
    ],
)

sh_binary(
    name = "BazelBspLocalJdkTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspLocalJdkTest",
        "e2e/test-resources/local-jdk-project",
    ],
)

sh_binary(
    name = "BazelBspRemoteJdkTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspRemoteJdkTest",
        "e2e/test-resources/remote-jdk-project",
    ],
)

sh_binary(
    name = "BazelBspCppProjectTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspCppProjectTest",
        "e2e/test-resources/cpp-project",
    ],
)

sh_binary(
    name = "BazelBspPythonProjectTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspPythonProjectTest",
        "e2e/test-resources/python-project",
    ],
)

sh_binary(
    name = "BazelBspEntireRepositoryImportTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspEntireRepositoryImportTest",
    ],
)

# simply a wrapper for the bash script to keep the convention
sh_binary(
    name = "all",
    srcs = ["runAll.sh"],
)
