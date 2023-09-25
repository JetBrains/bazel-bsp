load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "BazelBspEnvironmentManagerTest",
    size = "small",
    src = "BazelBspEnvironmentManagerTest.kt",
    deps = [
        "//commons",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/managers",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)
