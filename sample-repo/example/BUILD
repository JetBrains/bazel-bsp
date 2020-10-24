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
    main_class = "example.Example",
    resources = [":resources"],
    visibility = ["//visibility:public"],
    deps = ["//dep"],
)
