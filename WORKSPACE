workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# rules_jvm_external - for maven dependencies

RULES_JVM_EXTERNAL_TAG = "4.2"

RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-{}".format(RULES_JVM_EXTERNAL_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/{}.zip".format(RULES_JVM_EXTERNAL_TAG),
)

# ======================================================================================================================
# bazel_skylib - starlark functions

BAZEL_SKYLIB_TAG = "1.2.0"

BAZEL_SKYLIB_SHA = "af87959afe497dc8dfd4c6cb66e1279cb98ccc84284619ebfec27d9c09a903de"

http_archive(
    name = "bazel_skylib",
    sha256 = BAZEL_SKYLIB_SHA,
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

# ======================================================================================================================
# io_bazel_rules_scala - required by bazel_sonatype

IO_BAZEL_RULES_SCALA_TAG = "20220201"

IO_BAZEL_RULES_SCALA_SHA = "77a3b9308a8780fff3f10cdbbe36d55164b85a48123033f5e970fdae262e8eb2"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-{}".format(IO_BAZEL_RULES_SCALA_TAG),
    url = "https://github.com/bazelbuild/rules_scala/releases/download/{}/rules_scala-{}.zip".format(IO_BAZEL_RULES_SCALA_TAG, IO_BAZEL_RULES_SCALA_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories()

# ======================================================================================================================
# io_bazel - for protobuf

IO_BAZEL_TAG = "5.0.0"

IO_BAZEL_SHA = "ce1b391335bd417b5f7ec99e9049aee751b24f4a0e61a6dad3535f0e108bc182"

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-{}".format(IO_BAZEL_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(IO_BAZEL_TAG),
)

# ======================================================================================================================
# googleapis - for build protos

GOOGLEAPIS_TAG = "5.0.0"

GOOGLEAPIS_SHA = "ce1b391335bd417b5f7ec99e9049aee751b24f4a0e61a6dad3535f0e108bc182"

http_archive(
    name = "googleapis",
    sha256 = GOOGLEAPIS_SHA,
    strip_prefix = "bazel-{}/third_party/googleapis".format(GOOGLEAPIS_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(GOOGLEAPIS_TAG),
)

# ======================================================================================================================
# rules_python - required by com_google_protobuf

RULES_PYTHON_TAG = "0.6.0"

RULES_PYTHON_SHA = "a30abdfc7126d497a7698c29c46ea9901c6392d6ed315171a6df5ce433aa4502"

http_archive(
    name = "rules_python",
    sha256 = RULES_PYTHON_SHA,
    strip_prefix = "rules_python-{}".format(RULES_PYTHON_TAG),
    url = "https://github.com/bazelbuild/rules_python/archive/{}.tar.gz".format(RULES_PYTHON_TAG),
)

# ======================================================================================================================
# zlib - required by com_google_protobuf

ZLIB_TAG = "1.2.11"

ZLIB_SHA = "c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1"

http_archive(
    name = "zlib",
    build_file = "@com_google_protobuf//:third_party/zlib.BUILD",
    sha256 = ZLIB_SHA,
    strip_prefix = "zlib-{}".format(ZLIB_TAG),
    url = "https://zlib.net/zlib-{}.tar.gz".format(ZLIB_TAG),
)

# ======================================================================================================================
# com_google_protobuf - for :protobuf_java

COM_GOOGLE_PROTOBUF_TAG = "3.19.4"

COM_GOOGLE_PROTOBUF_SHA = "3bd7828aa5af4b13b99c191e8b1e884ebfa9ad371b0ce264605d347f135d2568"

http_archive(
    name = "com_google_protobuf",
    sha256 = COM_GOOGLE_PROTOBUF_SHA,
    strip_prefix = "protobuf-{}".format(COM_GOOGLE_PROTOBUF_TAG),
    url = "https://github.com/protocolbuffers/protobuf/archive/v{}.tar.gz".format(COM_GOOGLE_PROTOBUF_TAG),
)

# ======================================================================================================================
# bazel_sonatype - for publish

BAZEL_SONATYPE_TAG = "544b27e5ef7493ae2f961df0930f4a96d3cb6b24"

BAZEL_SONATYPE_SHA = "c1dca68543662588c1d35dae5840f7381895e0fe341bf38d612586cef024dc82"

http_archive(
    name = "bazel_sonatype",
    #sha256 = BAZEL_SONATYPE_SHA,
    strip_prefix = "bazel-sonatype-%s" % BAZEL_SONATYPE_TAG,
    url = "https://github.com/abrams27/bazel-sonatype/archive/%s.zip" % BAZEL_SONATYPE_TAG,
)

# ----------------------------------------------------------------------------------------------------------------------
load("@bazel_sonatype//:defs.bzl", "sonatype_dependencies")

sonatype_dependencies()

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================

load("@bazel_bsp//:third_party.bzl", "dependencies")

dependencies()
