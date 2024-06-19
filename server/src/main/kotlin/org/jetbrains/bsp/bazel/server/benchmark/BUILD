load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "benchmark",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//logger",
        "@maven//:io_opentelemetry_opentelemetry_api",
        "@maven//:io_opentelemetry_opentelemetry_exporter_otlp",
        "@maven//:io_opentelemetry_opentelemetry_extension_kotlin",
        "@maven//:io_opentelemetry_opentelemetry_sdk",
        "@maven//:io_opentelemetry_opentelemetry_sdk_trace",
        "@maven//:io_opentelemetry_semconv_opentelemetry_semconv",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)
