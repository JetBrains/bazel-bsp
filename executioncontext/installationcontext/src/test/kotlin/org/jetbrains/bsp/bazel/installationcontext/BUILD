load("//:junit5.bzl", "kt_junit5_test")

kt_junit5_test(
    name = "InstallationContextConstructorTest",
    size = "small",
    srcs = ["InstallationContextConstructorTest.kt"],
    test_package = "org.jetbrains.bsp.bazel.installationcontext",
    deps = [
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
    ],
)

kt_junit5_test(
    name = "InstallationContextDebuggerAddressEntityMapperTest",
    size = "small",
    srcs = ["InstallationContextDebuggerAddressEntityMapperTest.kt"],
    associates = ["//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext"],
    test_package = "org.jetbrains.bsp.bazel.installationcontext",
)

kt_junit5_test(
    name = "InstallationContextJavaPathEntityMapperTest",
    size = "small",
    srcs = ["InstallationContextJavaPathEntityMapperTest.kt"],
    associates = ["//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext"],
    test_package = "org.jetbrains.bsp.bazel.installationcontext",
)
