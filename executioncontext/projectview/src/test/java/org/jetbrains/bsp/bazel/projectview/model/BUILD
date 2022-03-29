load("//:third_party.bzl", "java_junit5_test")

java_junit5_test(
    name = "ProjectViewTest",
    size = "small",
    srcs = ["ProjectViewTest.java"],
    test_package = "org.jetbrains.bsp.bazel.projectview.model",
    deps = [
        "//executioncontext/projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model",
        "@maven//:com_google_guava_guava",
    ],
)
