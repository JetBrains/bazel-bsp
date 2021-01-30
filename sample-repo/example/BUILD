load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary", "scala_test")

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

scala_test(
    name = "example-test",
    srcs = ["ExampleTest.scala"],
    main_class = "example.ExampleTest",
    resources = [":resources"],
    visibility = ["//visibility:public"],
    deps = ["//dep"],
)
