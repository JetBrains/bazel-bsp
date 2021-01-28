load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")

filegroup(
    name = "resources",
    srcs = [
        "file.txt",
        "file2.txt",
    ],
)

scala_binary(
    name = "example",
    srcs = ["Example.scala"],
    args = [
        "arg1",
        "arg2",
    ],
    main_class = "example.Example",
    resources = [":resources"],
    scalacopts = ["nowarn"],
    visibility = ["//visibility:public"],
    deps = ["//dep"],
)
