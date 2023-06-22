load("@rules_python//python:defs.bzl", "py_library")
load("@pip_deps//:requirements.bzl", "requirement")

py_library(
    name = "example_library",
    srcs = ["example_lib.py"],
    visibility = ["//visibility:public"],
    deps = [
        requirement("numpy"),
    ],
)
