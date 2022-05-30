load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary", "scala_library", "scala_test")
load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

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
    main_class = "manual_target.TestJavaBinary",
    tags = ["manual"],
)

scala_test(
    name = "scala_test",
    srcs = glob(["*.scala"]),
    main_class = "manual_target.ScalaTest",
)

java_test(
    name = "java_test",
    srcs = glob(["*.java"]),
    main_class = "manual_target.JavaTest",
)
