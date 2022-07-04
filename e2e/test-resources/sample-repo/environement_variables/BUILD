load("@rules_java//java:defs.bzl", "java_library", "java_test")

java_library(
    name = "java_binary",
    srcs = ["JavaEnv.java"],
    env = {
        "foo1": "val1",
        "foo2": "val2",
    },
    main_class = "environment_variables.JavaEnv",
    visibility = ["//visibility:public"],
)

java_test(
    name = "java_test",
    srcs = ["JavaTest.java"],
    env = {
        "foo3": "val3",
        "foo4": "val4",
    },
    env_inherit = [
        "foo1",
        "foo2",
    ],
    test_class = "environment_target.JavaTest",
    visibility = ["//visibility:public"],
)
