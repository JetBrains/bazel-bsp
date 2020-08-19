# Toolchain Registration for Scala
In order to fully use Bazel BSP for Scala projects make sure to use at least version
 ``d8bce245a96ca9ab721324bc8daa984aa13fa0f7`` of `rules_scala`:
```
http_archive(
    name = "io_bazel_rules_scala",
    url = "https://github.com/andrefmrocha/rules_scala/archive/d6186617cfe64cef2074b23ca58daac75fe40d42.tar.gz",
    strip_prefix = "rules_scala-d6186617cfe64cef2074b23ca58daac75fe40d42",
)

load("@io_bazel_rules_scala//:version.bzl", "bazel_version")
bazel_version(name = "bazel_version")
```

Make sure that your registered toolchain has Compilation Diagnostics enabled:
- At a BUILD file:
```
load("@io_bazel_rules_scala//scala:scala_toolchain.bzl", "scala_toolchain")

scala_toolchain(
    name = "diagnostics_reporter_toolchain_impl",
    enable_diagnostics_report = True,
    visibility = ["//visibility:public"],
)

toolchain(
    name = "diagnostics_reporter_toolchain",
    toolchain = "diagnostics_reporter_toolchain_impl",
    toolchain_type = "@io_bazel_rules_scala//scala:toolchain_type",
    visibility = ["//visibility:public"],
)
```
- At the WORKSPACE file:
```
register_toolchains(
    "<label_to_build_file>:diagnostics_reporter_toolchain"
)
```

