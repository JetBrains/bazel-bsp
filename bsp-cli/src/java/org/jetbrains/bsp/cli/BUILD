load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

kt_jvm_binary(
    name = "bspcli",
    srcs = ["Main.kt"],
    main_class = "org.jetbrains.bsp.cli.MainKt",
    resources = [],
    visibility = ["//visibility:public"],
    deps = [
        "//executioncontext/workspacecontext",
        "//install/src/main/java/org/jetbrains/bsp/bazel/install",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
