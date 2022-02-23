# we cannot use `bazel test ...` because testrunner blocks bazel deamon,
# but testing server needs it for queries and etc

sh_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
        "e2e/test-resources/sample-repo",
        "2.x",
        "3.x",
        "4.x",
        "5.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspActionGraphV1Test",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV1Test",
        "e2e/test-resources/sample-repo",
        "1.x",
        "2.x",
        "3.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspActionGraphV2Test",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspActionGraphV2Test",
        "e2e/test-resources/action-graph-v2",
        "4.x",
        "5.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspJava8ProjectTest",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava8ProjectTest",
        "e2e/test-resources/java-8-project",
        "1.x",
        "2.x",
        "3.x",
        "4.x",
        "5.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspJava11ProjectTest",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspJava11ProjectTest",
        "e2e/test-resources/java-11-project",
        "1.x",
        "2.x",
        "3.x",
        "4.x",
        "5.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspCppProjectTest",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspCppProjectTest",
        "e2e/test-resources/cpp-project",
        "1.x",
        "2.x",
        "3.x",
        "4.x",
        "5.x",
    ],
    data = ["runTest.sh"],
)

sh_binary(
    name = "BazelBspEntireRepositoryImportTest",
    srcs = ["runTestForBazelVersions.sh"],
    args = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel:BazelBspEntireRepositoryImportTest",
        ".",
        "5.x",
    ],
    data = ["runTest.sh"],
)

# simply a wrapper for the bash script to keep the convention
sh_binary(
    name = "all",
    srcs = ["runAll.sh"],
)
