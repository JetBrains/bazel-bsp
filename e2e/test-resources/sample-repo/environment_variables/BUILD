load("@rules_java//java:defs.bzl", "java_binary", "java_test")

java_binary(
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
        "foo1": "val1",
        "foo2": "val2",
    },
    env_inherit = [
        "foo3",
        "foo4",
    ],
    test_class = "environment_variables.JavaTest",
    visibility = ["//visibility:public"],
)
