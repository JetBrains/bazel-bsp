load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DiagnosticsServiceTest",
    size = "small",
    src = "DiagnosticsServiceTest.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/diagnostics",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)
