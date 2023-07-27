load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")

kt_jvm_library(
    name = "install",
    visibility = ["//visibility:public"],
    exports = [
        "//install/src/main/java/org/jetbrains/bsp/bazel/install",
    ],
)

kt_jvm_binary(
    name = "install-server",
    main_class = "org.jetbrains.bsp.bazel.install.Install",
    resources = ["//log4j_config"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//commons",
        "//install",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server",
    ],
)
