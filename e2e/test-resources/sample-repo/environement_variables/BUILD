load("@rules_java//java:defs.bzl", "java_library", "java_test")

java_library(
    name = "java_library",
    srcs = ["JavaEnv.java"],
    env = {"foo1": "foo2"},
    visibility = ["//visibility:public"],
)

java_test(
    name = "java_test",
    srcs = ["JavaTest.java"],
    test_class = "environment_target.JavaTest",
    env_inherit = ["foo3"],
    visibility = ["//visibility:public"],
)
