load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "install",
    visibility = ["//visibility:public"],
    exports = [
        "//install/src/main/java/org/jetbrains/bsp/bazel/install",
    ],
)

java_binary(
    name = "bsp-install",
    main_class = "org.jetbrains.bsp.bazel.install.Install",
    runtime_deps = ["//install/src/main/java/org/jetbrains/bsp/bazel/install"],
)
