# we cannot use `bazel test ...` because testrunner blocks bazel deamon,
# but testing server needs it for queries and etc

sh_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["runTestForAllBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
        "e2e/test-resources/sample-repo",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspActionGraphV1Test",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV1Test",
        "3.7.2",
        "e2e/test-resources/sample-repo",
    ],
)

sh_binary(
    name = "BazelBspActionGraphV2Test",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV2Test",
        "4.0.0",
        "e2e/test-resources/action-graph-v2",
    ],
)

sh_binary(
    name = "BazelBspJava8ProjectTest",
    srcs = ["runTestForAllBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava8ProjectTest",
        "e2e/test-resources/java-8-project",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspJava11ProjectTest",
    srcs = ["runTestForAllBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava11ProjectTest",
        "e2e/test-resources/java-11-project",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspCppProjectTest",
    srcs = ["runTestForAllBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspCppProjectTest",
        "e2e/test-resources/cpp-project",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspEntireRepositoryImportTest",
    srcs = ["runTest.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspEntireRepositoryImportTest",
        "3.7.2",
    ],
)

# simply a wrapper for the bash script to keep the convention
sh_binary(
    name = "all",
    srcs = ["runAll.sh"],
)
