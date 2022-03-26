load("@rules_java//java:defs.bzl", "java_binary")

filegroup(
    name = "resources",
    srcs = [
        "file1.txt",
        "file2.txt",
    ],
)

java_binary(
    name = "java_binary",
    srcs = ["JavaBinary.java"],
    main_class = "target_with_resources.JavaBinary",
    resources = [":resources"],
    visibility = ["//visibility:public"],
)
