load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "dep",
    srcs = glob(["*.java", "*.scala"]),
    deps = ["//dep/deeper:deeper"],
    visibility = ["//visibility:public"],
)
