load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")
load("@rules_java//java:defs.bzl", "java_binary")

scala_binary(
    name = "manual_target",
    srcs = ["TestScalaFile.scala"],
    args = [
        "arg1",
        "arg2",
    ],
    main_class = "example.Example",

    visibility = ["//visibility:public"],
    deps = ["//dep"],
    tags = ["manual"],
)

java_binary(
    name = "manual_target_testing",
    srcs = glob(["*.java"]),
    main_class = "example.Example",
    visibility = ["//visibility:public"],
    tags = ["manual"],
)

