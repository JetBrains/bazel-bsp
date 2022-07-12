load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "DiagnosticsServiceTest",
    size = "small",
    srcs = ["DiagnosticsServiceTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.diagnostics",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
