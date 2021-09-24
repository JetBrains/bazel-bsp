load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "bsp-integration-test",
    srcs = glob(["*.java"]),
    main_class = "org.jetbrains.bsp.bazel.BazelBspServerTestRunner",
    resources = ["//src/test/resources:bsp-test-resources"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/server",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:ch_epfl_scala_bsp_testkit_2_13",
        "@maven//:com_google_guava_guava",
        "@maven//:io_vavr_vavr",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_scala_lang_scala_library",
    ],
)
