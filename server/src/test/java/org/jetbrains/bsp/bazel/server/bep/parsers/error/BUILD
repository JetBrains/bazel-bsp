load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "FileDiagnosticTest",
    size = "small",
    srcs = ["FileDiagnosticTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep/parsers/error",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

java_test(
    name = "StderrDiagnosticsParserTest",
    size = "small",
    srcs = ["StderrDiagnosticsParserTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/bep/parsers/error",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
