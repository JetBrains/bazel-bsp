load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")
load("@rules_java//java:defs.bzl", "java_binary")

scala_binary(
    name = "manual_target_testing",
    srcs = ["Example.scala"],
    args = [
        "arg1",
        "arg2",
    ],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
    resources = [":resources"],
    scalacopts = ["-target:jvm-1.8"],
    visibility = ["//visibility:public"],
    deps = ["//dep"],
)

java_binary(
    name = "manual_target_testing",
    srcs = glob(["*.java"]),
    javacopts = [
        "-Werror",
        "-Xlint:all",
    ],
    main_class = "example.Example",
    visibility = ["//visibility:public"],
)

