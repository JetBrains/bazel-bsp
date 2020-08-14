load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")

filegroup(
    name = "resources",
    srcs = ["file.txt", "file2.txt"]
)

scala_binary(
    name = "example",
    srcs = ["Example.scala"],
    deps = ["//dep:dep"],
    main_class = "example.Example",
    visibility = ["//visibility:public"],
    resources = [":resources"]
)
