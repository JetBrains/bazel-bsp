load("@rules_rust//rust:defs.bzl", "rust_binary")

rust_binary(
    name = "example",
    srcs = ["main.rs"],
    deps = [
        "@crate_index//:itertools",
        "//example-lib:example_lib",
    ],
    visibility = ["//visibility:public"],
)

rust_binary(
    name = "example_foo",
    srcs = [
        "main.rs",
        "sub.rs"
    ],
    deps = [
        "@crate_index//:itertools",
        "@crate_index//:itoa",
        "//example-lib:example_lib",
    ],
    crate_features = ["foo"],
    visibility = ["//visibility:public"],
)
