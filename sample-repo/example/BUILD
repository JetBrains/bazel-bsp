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
    args = [
        "arg1",
        "arg2",
    ],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
    resources = [":resources"],
    scalacopts = ["-target:jvm-1.8"],
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
