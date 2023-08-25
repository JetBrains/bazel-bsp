load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary", "scala_library", "scala_test")
load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

scala_library(
    name = "scala_library",
    srcs = ["TestScalaFile.scala"],
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

java_library(
    name = "java_library",
    srcs = ["TestJavaFile.java"],
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

scala_binary(
    name = "scala_binary",
    srcs = ["TestScalaBinary.scala"],
    main_class = "manual_target.TestScalaBinary",
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

java_binary(
    name = "java_binary",
    srcs = ["TestJavaBinary.java"],
    main_class = "manual_target.TestJavaBinary",
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

scala_test(
    name = "scala_test",
    srcs = ["ScalaTest.scala"],
    tags = ["manual"],
    visibility = ["//visibility:public"],
)

java_test(
    name = "java_test",
    srcs = ["JavaTest.java"],
    tags = ["manual"],
    test_class = "manual_target.JavaTest",
    visibility = ["//visibility:public"],
)
