load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "dependencytree",
    srcs = glob(["*.kt"]),
    visibility = ["//visibility:public"],
    exports = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
    ],
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/sync/proto:bsp_target_info_java_proto",
        "@com_google_protobuf//:protobuf_java",
    ],
)
