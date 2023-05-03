load("@rules_rust//rust:defs.bzl", "rust_library")

rust_library(
    name = "example_lib",
    srcs = ["lib.rs"],
    deps = [
        "@crate_index//:serde",
        "@crate_index//:serde_json",
    ],
    visibility = ["//visibility:public"],
)
