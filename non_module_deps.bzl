load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

BAZEL_BEP_VERSION = "5.4.1"

def _bazel_bep(_ctx):
    http_jar(
        name = "bazel_bep",
        url = "https://github.com/agluszak/bazel-bep-proto/releases/download/%s/bazel-bep-protobuf.jar" % BAZEL_BEP_VERSION,
    )

bazel_bep = module_extension(implementation = _bazel_bep)
