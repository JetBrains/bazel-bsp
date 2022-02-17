workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# rules_jvm_external

RULES_JVM_EXTERNAL_TAG = "4.2"

RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-{}".format(RULES_JVM_EXTERNAL_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/{}.zip".format(RULES_JVM_EXTERNAL_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# bazel_skylib

BAZEL_SKYLIB_TAG = "1.2.0"

BAZEL_SKYLIB_SHA = "af87959afe497dc8dfd4c6cb66e1279cb98ccc84284619ebfec27d9c09a903de"

http_archive(
    name = "bazel_skylib",
    sha256 = BAZEL_SKYLIB_SHA,
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# io_bazel_rules_scala

# IO_BAZEL_RULES_SCALA_TAG = "20220201"
IO_BAZEL_RULES_SCALA_TAG = "d6186617cfe64cef2074b23ca58daac75fe40d42"

# IO_BAZEL_RULES_SCALA_SHA = "77a3b9308a8780fff3f10cdbbe36d55164b85a48123033f5e970fdae262e8eb2"
IO_BAZEL_RULES_SCALA_SHA = "1a19bdedae7c62e9541315476c506c8e7a92c3ce0e7cbbfb57f12a429849f19d"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-{}".format(IO_BAZEL_RULES_SCALA_TAG),
    url = "https://github.com/andrefmrocha/rules_scala/archive/{}.tar.gz".format(IO_BAZEL_RULES_SCALA_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# rules_proto

RULES_PROTO_TAG = "4.0.0"

RULES_PROTO_SHA = "66bfdf8782796239d3875d37e7de19b1d94301e8972b3cbd2446b332429b4df1"

http_archive(
    name = "rules_proto",
    # sha256 = RULES_PROTO_SHA,
    strip_prefix = "rules_proto-{}".format(RULES_PROTO_TAG),
    url = "https://github.com/bazelbuild/rules_proto/archive/{}.tar.gz".format(RULES_PROTO_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# rules_java

RULES_JAVA_TAG = "5.0.0"

RULES_JAVA_SHA = "bf21724043eb21b19aa9b2253f8fb7d25c66896bc63037319d1d6cc2100d71f1"

http_archive(
    name = "rules_java",
    sha256 = RULES_JAVA_SHA,
    strip_prefix = "rules_java-{}".format(RULES_JAVA_TAG),
    url = "https://github.com/bazelbuild/rules_java/archive/{}.zip".format(RULES_JAVA_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# io_bazel

IO_BAZEL_TAG = "5.0.0"

IO_BAZEL_SHA = "ce1b391335bd417b5f7ec99e9049aee751b24f4a0e61a6dad3535f0e108bc182"

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-{}".format(IO_BAZEL_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(IO_BAZEL_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# googleapis

GOOGLEAPIS_TAG = "5.0.0"

GOOGLEAPIS_SHA = "ce1b391335bd417b5f7ec99e9049aee751b24f4a0e61a6dad3535f0e108bc182"

http_archive(
    name = "googleapis",
    sha256 = GOOGLEAPIS_SHA,
    strip_prefix = "bazel-{}/third_party/googleapis".format(GOOGLEAPIS_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(GOOGLEAPIS_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# com_google_protobuf

COM_GOOGLE_PROTOBUF_TAG = "3.19.4"

COM_GOOGLE_PROTOBUF_SHA = "3bd7828aa5af4b13b99c191e8b1e884ebfa9ad371b0ce264605d347f135d2568"

http_archive(
    name = "com_google_protobuf",
    sha256 = COM_GOOGLE_PROTOBUF_SHA,
    strip_prefix = "protobuf-{}".format(COM_GOOGLE_PROTOBUF_TAG),
    url = "https://github.com/protocolbuffers/protobuf/archive/v{}.tar.gz".format(COM_GOOGLE_PROTOBUF_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
# bazel_sonatype

BAZEL_SONATYPE_TAG = "d14a12150204c6a2645d6b065076a9ba30a391fc"

BAZEL_SONATYPE_SHA = "c1dca68543662588c1d35dae5840f7381895e0fe341bf38d612586cef024dc82"

http_archive(
    name = "bazel_sonatype",
    sha256 = BAZEL_SONATYPE_SHA,
    strip_prefix = "bazel-sonatype-%s" % BAZEL_SONATYPE_TAG,
    url = "https://github.com/JetBrains/bazel-sonatype/archive/%s.zip" % BAZEL_SONATYPE_TAG,
)

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//:version.bzl", "bazel_version")

bazel_version(name = "bazel_version")

# ----------------------------------------------------------------------------------------------------------------------
load("@bazel_bsp//:third_party.bzl", "dependencies")

dependencies()

# ----------------------------------------------------------------------------------------------------------------------
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories((
    "2.12.11",
    {
        "scala_compiler": "e901937dbeeae1715b231a7cfcd547a10d5bbf0dfb9d52d2886eae18b4d62ab6",
        "scala_library": "dbfe77a3fc7a16c0c7cb6cb2b91fecec5438f2803112a744cb1b187926a138be",
        "scala_reflect": "5f9e156aeba45ef2c4d24b303405db259082739015190b3b334811843bd90d6a",
    },
))

# ----------------------------------------------------------------------------------------------------------------------
load("@bazel_sonatype//:defs.bzl", "sonatype_dependencies")

sonatype_dependencies()
