load("@bazel_binaries//:defs.bzl", "bazel_binaries")
load("@rules_bazel_integration_test//bazel_integration_test:defs.bzl", "bazel_integration_tests")

bazel_integration_tests(
    name = "sample_repo_test",
    timeout = "eternal",
    bazel_versions = bazel_binaries.versions.all,
    env = {
        "foo1": "invalid_val1",
        "foo2": "invalid_val2",
        "foo3": "val3",
        "foo4": "val4",
    },
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
    workspace_path = "test-resources/sample-repo",
)

bazel_integration_tests(
    name = "local_jdk_test",
    timeout = "eternal",
    additional_env_inherit = ["JAVA_HOME"],
    bazel_versions = bazel_binaries.versions.all,
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspLocalJdkTest",
    workspace_path = "test-resources/local-jdk-project",
)

bazel_integration_tests(
    name = "remote_jdk_test",
    timeout = "eternal",
    bazel_versions = bazel_binaries.versions.all,
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspRemoteJdkTest",
    workspace_path = "test-resources/remote-jdk-project",
)

bazel_integration_tests(
    name = "cpp_project_test",
    timeout = "eternal",
    bazel_versions = bazel_binaries.versions.all,
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspCppProjectTest",
    workspace_path = "test-resources/cpp-project",
)

bazel_integration_tests(
    name = "python_project_test",
    timeout = "eternal",
    bazel_versions = bazel_binaries.versions.all,
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspPythonProjectTest",
    workspace_path = "test-resources/python-project",
)
