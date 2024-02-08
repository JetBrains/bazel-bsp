load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

# This is a workaround; for some reason bazel fails with
#  in deps attribute of kt_jvm_library rule //server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bep:bep:
#    '@googleapis//:google_devtools_build_v1_publish_build_event_proto' does not have mandatory providers: 'JavaInfo'
# if these libraries are depended on directly from a kt_jvm_library.

kt_jvm_library(
    name = "bep",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    resources = ["//log4j_config"],
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//logger",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/diagnostics",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/paths",
        "@bazel_bep//jar",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
