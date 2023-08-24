load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "base",
    srcs = glob(["*.kt"]),
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    runtime_deps = [
        "//install",
    ],
    deps = [
        "//install/src/main/java/org/jetbrains/bsp/bazel/install",
        "@maven//:ch_epfl_scala_bsp4j_2_13",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@testkit//:bazel-testkit-client",
    ],
)
