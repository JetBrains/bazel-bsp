load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")

kt_jvm_library(
    name = "install-lib",
    visibility = ["//visibility:public"],
    exports = [
        "//install/src/main/kotlin/org/jetbrains/bsp/bazel/install",
    ],
)

kt_jvm_binary(
    name = "install",
    main_class = "org.jetbrains.bsp.bazel.install.Install",
    resources = ["//log4j_config"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//commons",
        "//install:install-lib",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server",
        "@maven//:org_slf4j_slf4j_nop",  # fixes BAZEL-806
    ],
)
