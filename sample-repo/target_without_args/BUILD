load("@io_bazel_rules_scala//scala:scala.bzl", "scala_binary")

scala_binary(
    name = "binary",
    srcs = ["Example.scala"],
    jvm_flags = ["-Xms2G -Xmx5G"],
    main_class = "example.Example",
)
