java_binary(
    name = "bsp-test",
    srcs = ["BazelBspServerTest.java"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspServerTest",
    deps = [
        "//main/src/org/jetbrains/bsp/bazel:bsp",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:ch_epfl_scala_bsp_testkit_2_13",
        "@maven//:com_google_guava_guava",
        "@maven//:org_scala_lang_scala_library",
    ],
)
