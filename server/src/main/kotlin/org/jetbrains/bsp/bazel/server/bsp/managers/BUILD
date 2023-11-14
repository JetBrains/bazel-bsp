load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "managers",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//server:__subpackages__"],
    deps = [
        "//bazelrunner",
        "//commons",
        "//executioncontext/workspacecontext",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bep",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/utils",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/diagnostics",
        "@googleapis//:google_devtools_build_v1_build_events_java_proto",
        "@io_bazel//src/main/java/com/google/devtools/build/lib/buildeventstream/proto:build_event_stream_java_proto",
        "@io_bazel//third_party/grpc:grpc-jar_checked_in",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_velocity_velocity_engine_core",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
