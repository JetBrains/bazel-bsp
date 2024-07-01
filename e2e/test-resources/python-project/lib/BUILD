load("@pip_deps//:requirements.bzl", "requirement")
load("@rules_python//python:defs.bzl", "py_library")

py_library(
    name = "example_library",
    srcs = ["example_lib.py"],
    visibility = ["//visibility:public"],
    deps = [
        requirement("numpy"),
    ],
)
