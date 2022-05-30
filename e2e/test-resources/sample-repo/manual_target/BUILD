load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library", "scala_binary")
load("@rules_java//java:defs.bzl", "java_library", "java_binary")

scala_library(
    name = "scala_library",
    srcs = glob(["*.scala"]),
    tags = ["manual"],
)

java_library(
    name = "java_library",
    srcs = glob(["*.java"]),
    tags = ["manual"],
)

scala_binary(
    name = "scala_binary",
    srcs = glob(["*.scala"]),
    main_class = "manual_target.TestScalaBinary",
    tags = ["manual"],
)

java_binary(
    name = "java_binary",
    srcs = glob(["*.java"]),
    main_class ="manual_target.TestJavaBinary",
    tags = ["manual"],
)
