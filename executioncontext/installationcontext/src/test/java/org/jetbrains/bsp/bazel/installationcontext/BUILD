load("@//rules/kotlin:junit5.bzl", "kt_test")

kt_test(
    name = "InstallationContextConstructorTest",
    size = "small",
    src = "InstallationContextConstructorTest.kt",
    deps = [
        "//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext",
    ],
)

kt_test(
    name = "InstallationContextDebuggerAddressEntityMapperTest",
    size = "small",
    src = "InstallationContextDebuggerAddressEntityMapperTest.kt",
    associates = ["//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext"],
)

kt_test(
    name = "InstallationContextJavaPathEntityMapperTest",
    size = "small",
    src = "InstallationContextJavaPathEntityMapperTest.kt",
    associates = ["//executioncontext/installationcontext/src/main/java/org/jetbrains/bsp/bazel/installationcontext"],
)
