load("@//rules/bazel_integration_test:defs.bzl", "bazel_integration_test_all_versions", "bazel_integration_test_current_version")

bazel_integration_test_all_versions(
    name = "sample_repo_test",
    env = {
        "foo1": "invalid_val1",
        "foo2": "invalid_val2",
        "foo3": "val3",
        "foo4": "val4",
    },
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspSampleRepoTest",
    workspace_path = "test-resources/sample-repo",
)

bazel_integration_test_all_versions(
    name = "local_jdk_test",
    additional_env_inherit = ["JAVA_HOME"],
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspLocalJdkTest",
    workspace_path = "test-resources/local-jdk-project",
)

bazel_integration_test_all_versions(
    name = "remote_jdk_test",
    test_runner = "@//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspRemoteJdkTest",
    workspace_path = "test-resources/remote-jdk-project",
)

bazel_integration_test_all_versions(
    name = "cpp_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspCppProjectTest",
    workspace_path = "test-resources/cpp-project",
)

bazel_integration_test_all_versions(
    name = "python_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspPythonProjectTest",
    workspace_path = "test-resources/python-project",
)

bazel_integration_test_all_versions(
    name = "enabled_rules_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspScalaProjectTest",
    workspace_path = "test-resources/enabled-rules-project",
)

bazel_integration_test_current_version(
    name = "server_downloads_bazelisk_test",
    env = {
        "PATH": "",  # To ensure that the server won't find Bazel in PATH
    },
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:ServerDownloadsBazeliskTest",
    workspace_path = "test-resources/sample-repo",
)

bazel_integration_test_all_versions(
    name = "rust_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspRustProjectTest",
    workspace_path = "test-resources/rust-project",
)
