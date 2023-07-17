load("@rules_python//python:defs.bzl", "py_binary")

py_binary(
    name = "example",
    srcs = ["example.py"],
    python_version = "PY3",
    deps = [
        "//lib:example_library",
        "@requests//:srcs",
    ],
)
