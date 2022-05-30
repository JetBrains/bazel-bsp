load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_binary(
    name = "java_binary",
    srcs = ["JavaBinary.java"],
    javacopts = [
        "-Werror",
        "-Xlint:all",
    ],
    main_class = "java_targets.JavaBinary",
    visibility = ["//visibility:public"],
)

java_library(
    name = "java_library",
    srcs = ["JavaLibrary.java"],
    visibility = ["//visibility:public"],
)

java_library(
    name = "java_library_exported",
    visibility = ["//visibility:public"],
    exports = [
        "//java_targets/subpackage:java_library",
    ],
)
