load("@rules_rust//rust:defs.bzl", "rust_binary")

rust_binary(
    name = "example",
    srcs = ["main.rs"],
    visibility = ["//visibility:public"],
    deps = [
        "//example-lib:example_lib",
        "@crate_index//:itertools",
    ],
)

rust_binary(
    name = "example_foo",
    srcs = [
        "main.rs",
        "sub.rs",
    ],
    crate_features = ["foo"],
    visibility = ["//visibility:public"],
    deps = [
        "//example-lib:example_lib",
        "@crate_index//:itertools",
        "@crate_index//:itoa",
    ],
)
