# Toolchain Registration for Scala

In order to fully use Bazel BSP for Scala projects make sure to use at least version
`e7a948ad1948058a7a5ddfbd9d1629d6db839933` of `rules_scala`:

```python
IO_BAZEL_RULES_SCALA_VERSION = "e7a948ad1948058a7a5ddfbd9d1629d6db839933"
IO_BAZEL_RULES_SCALA_SHA = "76e1abb8a54f61ada974e6e9af689c59fd9f0518b49be6be7a631ce9fa45f236"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-{}".format(IO_BAZEL_RULES_SCALA_VERSION),
    url = "https://github.com/bazelbuild/rules_scala/archive/{}.zip".format(IO_BAZEL_RULES_SCALA_VERSION),
)
```

or at least `20220201` tag of `rules_scala`:

```python
IO_BAZEL_RULES_SCALA_TAG = "20220201"
IO_BAZEL_RULES_SCALA_SHA = "77a3b9308a8780fff3f10cdbbe36d55164b85a48123033f5e970fdae262e8eb2"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-{}".format(IO_BAZEL_RULES_SCALA_TAG),
    url = "https://github.com/bazelbuild/rules_scala/releases/download/{}/rules_scala-{}.zip".format(IO_BAZEL_RULES_SCALA_TAG, IO_BAZEL_RULES_SCALA_TAG),
)
```

--- 

Make sure that your registered toolchain has **Compilation Diagnostics** enabled:

- At a `BUILD` file:

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

- At the `WORKSPACE` file:

```
register_toolchains(
    "<label_to_build_file>:diagnostics_reporter_toolchain"
)
```
