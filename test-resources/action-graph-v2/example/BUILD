load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")

scala_binary(
    name = "example",
    srcs = ["Example.scala"],
    args = [
        "arg1",
        "arg2",
    ],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
    scalacopts = ["-target:jvm-1.8"],
    visibility = ["//visibility:public"],
    deps = ["//dep"],
)
