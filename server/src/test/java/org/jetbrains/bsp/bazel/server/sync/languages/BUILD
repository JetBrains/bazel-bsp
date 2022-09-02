load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "JVMLanguagePluginParserTest",
    size = "small",
    srcs = ["JVMLanguagePluginParserTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages",
    deps = [
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)

kt_junit5_test(
    name = "LanguagePluginServiceTest",
    size = "small",
    srcs = ["LanguagePluginServiceTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.server.sync.languages",
    deps = [
        "//bazelrunner",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server/sync",
    ],
)
