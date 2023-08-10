load("@io_bazel_rules_kotlin//kotlin:core.bzl", "define_kt_toolchain")

define_kt_toolchain(
    name = "kotlin_toolchain",
    api_version = "1.8",
    jvm_target = "17",
    language_version = "1.8",
)

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_compiler_plugin")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")

kt_compiler_plugin(
    name = "serialization_plugin",
    compile_phase = True,
    id = "org.jetbrains.kotlin.serialization",
    stubs_phase = True,
    deps = [
        "@com_github_jetbrains_kotlin//:kotlinx-serialization-compiler-plugin",
    ],
)

# This you would put in your tree and maybe forbid depending on the raw rule.
# Using square's bazel_maven_repository, you could use a build-snippet with
# kt_jvm_import instead.
kt_jvm_library(
    name = "kotlin_serialization",
    srcs = [],
    exported_compiler_plugins = [":serialization_plugin"],
    exports = [
        "@maven//:org_jetbrains_kotlinx_kotlinx_serialization_core_jvm",
    ],
    visibility = ["//visibility:public"],
)
