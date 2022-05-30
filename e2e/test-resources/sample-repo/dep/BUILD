load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "dep",
    srcs = glob([
        "*.java",
        "*.scala",
    ]),
    visibility = ["//visibility:public"],
    deps = ["//dep:deeper-export"],
)

java_binary(
    name = "java-dep",
    srcs = glob(["*.java"]),
    javacopts = [
        "-Werror",
        "-Xlint:all",
    ],
    main_class = "dep.JavaTest",
    visibility = ["//visibility:public"],
)

java_library(
    name = "deeper-export",
    visibility = ["//visibility:public"],
    exports = [
        "//dep/deeper",
    ],
)
