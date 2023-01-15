load("@rules_python//python:defs.bzl", "py_binary")

py_binary(
  name = "example_library",
  main = "example.py",
  srcs = ["example.py"],
  deps = ["//lib:calculators"],
  python_version = "PY3"
)
