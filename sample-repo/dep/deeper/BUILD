load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "deeper",
    srcs = glob([
        "*.java",
        "*.scala",
    ]),
    visibility = ["//visibility:public"],
    deps = ["@maven//:com_google_guava_guava"],
)
