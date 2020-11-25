load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "bsp-test",
    srcs = glob(["*.java"]),
    main_class = "org.jetbrains.bsp.bazel.BazelBspServerTestRunner",
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/server",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:ch_epfl_scala_bsp_testkit_2_13",
        "@maven//:com_google_guava_guava",
        "@maven//:org_scala_lang_scala_library",
        "@maven//:io_vavr_vavr",
    ],
)
