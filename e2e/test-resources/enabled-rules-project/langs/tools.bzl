load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:utils.bzl", "maybe")

def languages():
    rules_scala_version = "3dd5d8110d56cfc19722532866cbfc039a6a9612"
    maybe(
        http_archive,
        name = "io_bazel_rules_scala",
        sha256 = "d805d4c3e288f87909c0eba177facc2d945f1eeb67f4bd78e96afc51fa25e03c",
        strip_prefix = "rules_scala-%s" % rules_scala_version,
        type = "zip",
        url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip" % rules_scala_version,
    )
