load("@rules_rust//rust:defs.bzl", "rust_library")

rust_library(
    name = "example_lib",
    srcs = ["lib.rs"],
    visibility = ["//visibility:public"],
    deps = [
        "@crate_index//:instant",
    ],
)
