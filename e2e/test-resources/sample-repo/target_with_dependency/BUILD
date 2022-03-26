load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "java_binary",
    srcs = ["JavaBinary.java"],
    main_class = "target_with_dependency.JavaBinary",
    visibility = ["//visibility:public"],
    deps = [
        "//java_targets:java_library_exported",
        "@guava",
    ],
)
