load("@io_bazel_rules_scala//scala:scala.bzl", "java_binary", "scala_library")

scala_library(
    name = "dep",
    srcs = glob([
        "*.java",
        "*.scala",
    ]),
    visibility = ["//visibility:public"],
    deps = ["//dep/deeper"],
)

java_binary(
    name = "java-dep",
    srcs = glob(["*.java"]),
    javacopts = [
        "-Werror",
        "-Xlint:all",
    ],
    visibility = ["//visibility:public"],
)
