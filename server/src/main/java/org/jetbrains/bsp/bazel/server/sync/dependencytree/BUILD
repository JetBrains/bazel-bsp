load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "dependencytree",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    exports = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@maven//:io_vavr_vavr",
    ],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@maven//:io_vavr_vavr",
    ],
)
