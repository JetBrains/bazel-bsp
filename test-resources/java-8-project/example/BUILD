load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "example",
    srcs = ["Example.java"],
    args = [
        "arg1",
        "arg2",
    ],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
    visibility = ["//visibility:public"],
)
