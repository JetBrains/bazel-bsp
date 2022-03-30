load("//:junit5.bzl", "java_junit5_test")

java_junit5_test(
    name = "FileDiagnosticTest",
    size = "small",
    srcs = ["FileDiagnosticTest.java"],
    test_package = "org.jetbrains.bsp.bazel.server.bep.parsers.error",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep/parsers/error",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

java_junit5_test(
    name = "StderrDiagnosticsParserTest",
    size = "small",
    srcs = ["StderrDiagnosticsParserTest.java"],
    test_package = "org.jetbrains.bsp.bazel.server.bep.parsers.error",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep/parsers/error",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
