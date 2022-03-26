load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "base",
    srcs = glob(["*.java"]),
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:ch_epfl_scala_bsp_testkit_2_13",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_scala_lang_scala_library",
        "@testkit//:testkit-client",
    ],
)
