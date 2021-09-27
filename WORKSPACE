# tags
BAZEL_SKYLIB_TAG = "1.0.2"

BAZEL_SONATYPE_TAG = "8c4bfd2a4c03c212446da134e0be3ab1ac605289"

COM_GOOGLE_PROTOBUF_TAG = "31ebe2ac71400344a5db91ffc13c4ddfb7589f92"

GOOGLEAPIS_TAG = "caf13559e367da9c791cc5e559d2970400d5478b"

IO_BAZEL_TAG = "8eac729671af6161226f2292325f442e159017a6"

IO_BAZEL_RULES_SCALA_TAG = "d6186617cfe64cef2074b23ca58daac75fe40d42"

IO_GRPC_GRPC_JAVA_TAG = "1.30.2"

RULES_JAVA_TAG = "9eb38ebffbaf4414fa3d2292b28e604a256dd5a5"

RULES_JVM_EXTERNAL_TAG = "4.0"

RULES_PKG_TAG = "0.2.0"

RULES_PROTO_TAG = "486aaf1808a15b87f1b6778be6d30a17a87e491a"

# sha256
BAZEL_SKYLIB_SHA = "97e70364e9249702246c0e9444bccdc4b847bed1eb03c5a3ece4f83dfe6abc44"

BAZEL_SONATYPE_SHA = "58b616d7ef1d28bc627c1b945c704cb2fe4cf49deb87fc0ce074a452c457ec2b"

COM_GOOGLE_PROTOBUF_SHA = "0e8e32d44c9d4572975f43591b51cd3c77392661e4ded17fdfab81e8460344e8"

GOOGLEAPIS_SHA = "4e5d2467df2994b13b2caaa0422985bedff804c3ae640fba23e63903172345ff"

IO_BAZEL_SHA = "7ce1e69e1447db53ba27d5807053c0c602bafa8e66e5d70b06db3903bf1d5b68"

IO_BAZEL_RULES_SCALA_SHA = "1a19bdedae7c62e9541315476c506c8e7a92c3ce0e7cbbfb57f12a429849f19d"

IO_GRPC_GRPC_JAVA_SHA = "849780c41b7a251807872a00b752cc965da483e0c345b25f78ed163e878b9b2c"

RULES_JAVA_SHA = "7f4772b0ee2b46a042870c844e9c208e8a0960a953a079236a4bbd785e471275"

RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

RULES_PKG_SHA = "5bdc04987af79bd27bc5b00fe30f59a858f77ffa0bd2d8143d5b31ad8b1bd71c"

RULES_PROTO_SHA = "dedb72afb9476b2f75da2f661a00d6ad27dfab5d97c0460cf3265894adfaf467"

# ------------------------------
workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

http_archive(
    name = "bazel_skylib",
    sha256 = BAZEL_SKYLIB_SHA,
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-%s" % IO_BAZEL_RULES_SCALA_TAG,
    url = "https://github.com/andrefmrocha/rules_scala/archive/%s.tar.gz" % IO_BAZEL_RULES_SCALA_TAG,
)

load("@io_bazel_rules_scala//:version.bzl", "bazel_version")

bazel_version(name = "bazel_version")

load("@bazel_bsp//:third_party.bzl", "dependencies")

dependencies()

# For bazel for BEP proto:
http_archive(
    name = "rules_proto",
    sha256 = RULES_PROTO_SHA,
    strip_prefix = "rules_proto-%s" % RULES_PROTO_TAG,
    url = "https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_TAG,
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

http_archive(
    name = "rules_java",
    sha256 = RULES_JAVA_SHA,
    strip_prefix = "rules_java-%s" % RULES_JAVA_TAG,
    url =
        "https://github.com/bazelbuild/rules_java/archive/%s.zip" % RULES_JAVA_TAG,
)

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = IO_GRPC_GRPC_JAVA_SHA,
    strip_prefix = "grpc-java-%s" % IO_GRPC_GRPC_JAVA_TAG,
    url = "https://github.com/grpc/grpc-java/archive/v%s.zip" % IO_GRPC_GRPC_JAVA_TAG,
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-%s" % IO_BAZEL_TAG,
    url = "https://github.com/andrefmrocha/bazel/archive/%s.zip" % IO_BAZEL_TAG,
)

http_archive(
    name = "rules_pkg",
    sha256 = RULES_PKG_SHA,
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/rules_pkg-%s.tar.gz" % RULES_PKG_TAG,
        "https://github.com/bazelbuild/rules_pkg/releases/download/{}/rules_pkg-{}.tar.gz".format(RULES_PKG_TAG, RULES_PKG_TAG),
    ],
)

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "rules_python",
    commit = "32e964842b4139645417d0a8c24c807c163d7cfc",
    remote = "https://github.com/bazelbuild/rules_python.git",
)

load("@rules_python//python:repositories.bzl", "py_repositories")

py_repositories()

http_archive(
    name = "googleapis",
    sha256 = GOOGLEAPIS_SHA,
    strip_prefix = "bazel-%s/third_party/googleapis" % GOOGLEAPIS_TAG,
    url = "https://github.com/bazelbuild/bazel/archive/%s.zip" % GOOGLEAPIS_TAG,
)

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories((
    "2.12.11",
    {
        "scala_compiler": "e901937dbeeae1715b231a7cfcd547a10d5bbf0dfb9d52d2886eae18b4d62ab6",
        "scala_library": "dbfe77a3fc7a16c0c7cb6cb2b91fecec5438f2803112a744cb1b187926a138be",
        "scala_reflect": "5f9e156aeba45ef2c4d24b303405db259082739015190b3b334811843bd90d6a",
    },
))

http_archive(
    name = "com_google_protobuf",
    sha256 = COM_GOOGLE_PROTOBUF_SHA,
    strip_prefix = "protobuf-%s" % COM_GOOGLE_PROTOBUF_TAG,
    url = "https://github.com/protocolbuffers/protobuf/archive/%s.tar.gz" % COM_GOOGLE_PROTOBUF_TAG,
)

http_archive(
    name = "bazel_sonatype",
    sha256 = BAZEL_SONATYPE_SHA,
    strip_prefix = "bazel-sonatype-%s" % BAZEL_SONATYPE_TAG,
    url = "https://github.com/JetBrains/bazel-sonatype/archive/%s.zip" % BAZEL_SONATYPE_TAG,
)

load("@bazel_sonatype//:defs.bzl", "sonatype_dependencies")

sonatype_dependencies()
