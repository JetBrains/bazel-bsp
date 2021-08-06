load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "install",
    srcs = glob(["*.java"]),
    resources = ["aspects.bzl"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_cli_commons_cli",
    ],
)

java_binary(
    name = "bsp-install",
    main_class = "org.jetbrains.bsp.bazel.install.Install",
    runtime_deps = ["//src/main/java/org/jetbrains/bsp/bazel/install"],
)
