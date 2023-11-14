load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "BazelBspLanguageExtensionsGeneratorTest",
    size = "small",
    src = "BazelBspLanguageExtensionsGeneratorTest.kt",
    deps = [
        "//commons",
        "//install:install-lib",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/managers",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
    ],
)

kt_test(
    name = "StructuredFilesUtilsTests",
    size = "small",
    src = "StructuredFilesUtilsTests.kt",
    deps = [
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/bsp/managers",
    ],
)
