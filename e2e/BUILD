load("@//rules/bazel_integration_test:defs.bzl", "bazel_integration_test_all_versions", "bazel_integration_test_current_version")
load("@rules_bazel_integration_test//bazel_integration_test:defs.bzl", "bazel_integration_test", "integration_test_utils")

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

bazel_integration_test(
    name = "kotlin_project_test",
    timeout = "eternal",
    bazel_version = "6.4.0",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspKotlinProjectTest",
    workspace_path = "test-resources/kotlin-project",
)

bazel_integration_test_all_versions(
    name = "enabled_rules_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspScalaProjectTest",
    workspace_path = "test-resources/enabled-rules-project",
)

bazel_integration_test(
    name = "server_downloads_bazelisk_test",
    timeout = "eternal",
    bazel_version = "6.4.0",
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

bazel_integration_test_current_version(
    name = "android_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspAndroidProjectTest",
    workspace_path = "test-resources/android-project",
)

bazel_integration_test_current_version(
    name = "android_kotlin_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspAndroidKotlinProjectTest",
    workspace_path = "test-resources/android-kotlin-project",
)

bazel_integration_test_current_version(
    name = "go_project_test",
    test_runner = "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel:BazelBspGoProjectTest",
    workspace_path = "test-resources/go-project",
)

test_suite(
    name = "all_tests",
    tags = integration_test_utils.DEFAULT_INTEGRATION_TEST_TAGS,
    # we need to include all of them manually because of the "manual" tag
    tests = [
        ":android_kotlin_project_test",
        ":android_project_test",
        ":enabled_rules_test",
        ":local_jdk_test",
        ":python_project_test",
        ":remote_jdk_test",
        ":rust_project_test",
        ":sample_repo_test",
        ":server_downloads_bazelisk_test",
        ":go_project_test",
    ],
)
