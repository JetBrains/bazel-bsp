load("@bazel_binaries//:defs.bzl", "bazel_binaries")
load("@rules_bazel_integration_test//bazel_integration_test:defs.bzl", "bazel_integration_test", "bazel_integration_tests", "integration_test_utils")

def bazel_integration_test_all_versions(name, test_runner, workspace_path, env = {}, additional_env_inherit = []):
    # test projects are too old for bazel 7
    # bazel_versions = bazel_binaries.versions.all
    bazel_versions = ["5.3.2", "6.4.0"]

    bazel_integration_tests(
        name = name,
        timeout = "eternal",
        bazel_versions = bazel_versions,
        test_runner = test_runner,
        workspace_path = workspace_path,
        env = env,
        additional_env_inherit = additional_env_inherit,
    )

    native.test_suite(
        name = name,
        tags = integration_test_utils.DEFAULT_INTEGRATION_TEST_TAGS,
        tests = integration_test_utils.bazel_integration_test_names(name, bazel_versions),
    )

    for old_test_name in integration_test_utils.bazel_integration_test_names(name, bazel_versions):
        new_name = _calculate_new_version_name(old_test_name)

        native.test_suite(
            name = new_name,
            tags = integration_test_utils.DEFAULT_INTEGRATION_TEST_TAGS,
            tests = [old_test_name],
        )

def _calculate_new_version_name(old_name):
    if old_name.endswith(".bazelversion"):
        return old_name.removesuffix(".bazelversion") + "current"
    else:
        name_of_target_only_with_major, _, _ = old_name.rsplit("_", 2)
        return name_of_target_only_with_major + "_x"

def bazel_integration_test_current_version(name, test_runner, workspace_path, env = {}, additional_env_inherit = []):
    bazel_integration_test(
        name = name,
        timeout = "eternal",
        bazel_version = bazel_binaries.versions.current,
        test_runner = test_runner,
        workspace_path = workspace_path,
        env = env,
        additional_env_inherit = additional_env_inherit,
    )
