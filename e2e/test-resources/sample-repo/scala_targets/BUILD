load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary", "scala_test")

scala_binary(
    name = "scala_binary",
    srcs = ["ScalaBinary.scala"],
    args = [
        "arg1",
        "arg2",
    ],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
    scalacopts = ["-target:jvm-1.8"],
    visibility = ["//visibility:public"],
)

scala_test(
    name = "scala_test",
    srcs = ["ScalaTest.scala"],
    visibility = ["//visibility:public"],
)
