load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "DiagnosticsServiceTest",
    size = "small",
    src = "DiagnosticsServiceTest.kt",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/diagnostics",
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
    ],
)
