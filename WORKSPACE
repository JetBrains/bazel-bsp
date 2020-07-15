load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# For maven:
RULES_JVM_EXTERNAL_TAG = "3.3"
RULES_JVM_EXTERNAL_SHA = "d85951a92c0908c80bd8551002d66cb23c3434409c814179c0ff026b53544dab"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
# For rules_scala
http_archive(
    name = "io_bazel_rules_scala",
    url = "https://github.com/andrefmrocha/rules_scala/archive/2eff852880ecebc95919279990aff263fbf7ac62.tar.gz",
    strip_prefix = "rules_scala-2eff852880ecebc95919279990aff263fbf7ac62",
)

load("@io_bazel_rules_scala//scala:scala_cross_version.bzl", "default_maven_server_urls")



maven_install(
    artifacts = [
	"com.google.code.gson:gson:2.8.5",
	"com.google.guava:guava:28.1-jre",
	"ch.epfl.scala:bsp4j:2.0.0-M11",
	"org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.8.0",
        "org.eclipse.xtext:org.eclipse.xtext.xbase.lib:2.19.0",
	"org.scala-lang:scala-compiler:2.12.10",
    ],
    repositories = default_maven_server_urls(),
    fetch_sources = True,
)

# For bazel for BEP proto:
http_archive(
    name = "rules_proto",
    sha256 = "dedb72afb9476b2f75da2f661a00d6ad27dfab5d97c0460cf3265894adfaf467",
    strip_prefix = "rules_proto-486aaf1808a15b87f1b6778be6d30a17a87e491a",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/486aaf1808a15b87f1b6778be6d30a17a87e491a.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/486aaf1808a15b87f1b6778be6d30a17a87e491a.tar.gz",
    ],
)
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

http_archive(
    name = "rules_java",
    sha256 = "7f4772b0ee2b46a042870c844e9c208e8a0960a953a079236a4bbd785e471275",
    strip_prefix = "rules_java-9eb38ebffbaf4414fa3d2292b28e604a256dd5a5",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_java/archive/9eb38ebffbaf4414fa3d2292b28e604a256dd5a5.zip",
        "https://github.com/bazelbuild/rules_java/archive/9eb38ebffbaf4414fa3d2292b28e604a256dd5a5.zip",
    ],
)

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "849780c41b7a251807872a00b752cc965da483e0c345b25f78ed163e878b9b2c",
    strip_prefix = "grpc-java-1.30.2",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.30.2.zip"],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")
grpc_java_repositories()

http_archive(
    name = "io_bazel",
    strip_prefix = "bazel-8eac729671af6161226f2292325f442e159017a6",
    urls = ["https://github.com/andrefmrocha/bazel/archive/8eac729671af6161226f2292325f442e159017a6.zip"],
    sha256 = "7ce1e69e1447db53ba27d5807053c0c602bafa8e66e5d70b06db3903bf1d5b68",
)

http_archive(
    name = "rules_pkg",
    sha256 = "5bdc04987af79bd27bc5b00fe30f59a858f77ffa0bd2d8143d5b31ad8b1bd71c",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/rules_pkg-0.2.0.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.2.0/rules_pkg-0.2.0.tar.gz",
    ],
)

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "rules_python",
    remote = "https://github.com/bazelbuild/rules_python.git",
    commit = "32e964842b4139645417d0a8c24c807c163d7cfc",
)

load("@rules_python//python:repositories.bzl", "py_repositories")
py_repositories()

http_archive(
    name = "googleapis",
    strip_prefix = "bazel-5ebe41f2900d71a99be08f7a675a79228506aec6/third_party/googleapis",
    urls = ["https://github.com/andrefmrocha/bazel/archive/5ebe41f2900d71a99be08f7a675a79228506aec6.zip"],
    sha256 = "a02d861fac93196fe020fd36ec2ad698d34e54c2394741be82b60c6c2334a4bf",
)



load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
scala_register_toolchains()

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories((
    "2.12.10",
    {
       "scala_compiler": "cedc3b9c39d215a9a3ffc0cc75a1d784b51e9edc7f13051a1b4ad5ae22cfbc0c",
       "scala_library": "0a57044d10895f8d3dd66ad4286891f607169d948845ac51e17b4c1cf0ab569d",
       "scala_reflect": "56b609e1bab9144fb51525bfa01ccd72028154fc40a58685a1e9adcbe7835730",
    }
))

protobuf_version="31ebe2ac71400344a5db91ffc13c4ddfb7589f92"
protobuf_version_sha256="0e8e32d44c9d4572975f43591b51cd3c77392661e4ded17fdfab81e8460344e8"

http_archive(
    name = "com_google_protobuf",
    url = "https://github.com/protocolbuffers/protobuf/archive/%s.tar.gz" % protobuf_version,
    strip_prefix = "protobuf-%s" % protobuf_version,
    sha256 = protobuf_version_sha256,
)

# bazel-skylib 1.0.2 released 2019.03.20 (https://github.com/bazelbuild/bazel-skylib/releases/tag/1.0.2)
skylib_version = "1.0.2"
http_archive(
    name = "bazel_skylib",
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib.{}.tar.gz".format (skylib_version, skylib_version),
    sha256 = "97e70364e9249702246c0e9444bccdc4b847bed1eb03c5a3ece4f83dfe6abc44",
)
