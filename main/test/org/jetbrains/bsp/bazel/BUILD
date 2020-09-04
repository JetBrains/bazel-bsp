java_binary(
    name = "bsp-test",
    srcs = ["BazelBspServerTest.java"],
    deps = [
        "//main/src/org/jetbrains/bsp/bazel:bsp",
        "@maven//:com_google_guava_guava",
        "@maven//:ch_epfl_scala_bsp_testkit_2_13",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:org_scala_lang_scala_library",
    ],
    main_class = "org.jetbrains.bsp.bazel.BazelBspServerTest",
)
