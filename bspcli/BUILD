load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")
load("@rules_python//python:defs.bzl", "py_binary")
load(":integration_test.bzl", "bsp_cli_on_large_project", "synthetic_bazel_project")

LOG4J_CONFIG = "log4j2_bspcli.xml"

filegroup(
    name = "bspcli_resources",
    srcs = ["src/main/resources/{}".format(LOG4J_CONFIG)],
)

kt_jvm_binary(
    name = "bspcli",
    srcs = ["src/main/kotlin/org/jetbrains/bsp/cli/Main.kt"],
    jvm_flags = [
        "-Xmx4g",
        "-Dlog4j.configurationFile=classpath:{}".format(LOG4J_CONFIG),
    ],
    main_class = "org.jetbrains.bsp.cli.MainKt",
    resources = [":bspcli_resources"],
    visibility = ["//visibility:public"],
    deps = [
        "//executioncontext/workspacecontext",
        "//install/src/main/kotlin/org/jetbrains/bsp/bazel/install",
        "//protocol",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server",
        "//server/src/main/kotlin/org/jetbrains/bsp/bazel/server/benchmark",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

py_binary(
    name = "generator",
    srcs = glob(["*.py"]),
    imports = ["."],
    main = "gen.py",
    deps = [],
)

synthetic_bazel_project(
    name = "synthetic_project_medium",
    project_directory = "project_medium",
    project_size = 1000,
)

bsp_cli_on_large_project(
    name = "benchmark_medium",
    bspcli = "//bspcli:bspcli",
    project = ":synthetic_project_medium",
)
