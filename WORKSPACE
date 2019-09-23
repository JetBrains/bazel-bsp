load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# For maven:
RULES_JVM_EXTERNAL_TAG = "2.8"
RULES_JVM_EXTERNAL_SHA = "79c9850690d7614ecdb72d68394f994fef7534b292c4867ce5e7dec0aa7bdfad"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
	"com.google.code.gson:gson:2.8.5",
	"com.google.guava:guava:28.1-jre",
	"ch.epfl.scala:bsp4j:2.0.0-M4+10-61e61e87",
	"org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.8.0",
        "org.eclipse.xtext:org.eclipse.xtext.xbase.lib:2.19.0",
	"org.scala-lang:scala-compiler:2.12.10",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)

# For bazel for BEP proto:
http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-97d8af4dc474595af3900dd85cb3a29ad28cc313",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz",
    ],
)
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

http_archive(
    name = "rules_java",
    sha256 = "bc81f1ba47ef5cc68ad32225c3d0e70b8c6f6077663835438da8d5733f917598",
    strip_prefix = "rules_java-7cf3cefd652008d0a64a419c34c13bdca6c8f178",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_java/archive/7cf3cefd652008d0a64a419c34c13bdca6c8f178.zip",
        "https://github.com/bazelbuild/rules_java/archive/7cf3cefd652008d0a64a419c34c13bdca6c8f178.zip",
    ],
)

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "b1dcce395bdb6c620d3142597b5017f7175c527b0f9ae46c456726940876347e",
    strip_prefix = "grpc-java-1.23.0",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.23.0.zip"],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")
grpc_java_repositories()

http_archive(
    name = "io_bazel",
    strip_prefix = "bazel-d2a4746fb403c54a332984df78a564609863f8a7",
    urls = ["https://github.com/illicitonion/bazel/archive/d2a4746fb403c54a332984df78a564609863f8a7.zip"],
    sha256 = "aa04ccddcc6dcf57cf45b23ecccf296c7cabf08f63bcdc0767108ad8482db4f4",
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
    commit = "54d1cb35cd54318d59bf38e52df3e628c07d4bbc",
)

load("@rules_python//python:repositories.bzl", "py_repositories")
py_repositories()

http_archive(
    name = "googleapis",
    strip_prefix = "bazel-d2a4746fb403c54a332984df78a564609863f8a7/third_party/googleapis",
    urls = ["https://github.com/illicitonion/bazel/archive/d2a4746fb403c54a332984df78a564609863f8a7.zip"],
    sha256 = "aa04ccddcc6dcf57cf45b23ecccf296c7cabf08f63bcdc0767108ad8482db4f4",
)

# For rules_scala:
http_archive(
    name = "io_bazel_rules_scala",
    strip_prefix = "rules_scala-4ebd9c0207b1de94cd42d90c5009ea2d07a54cda",
    urls = ["https://github.com/illicitonion/rules_scala/archive/4ebd9c0207b1de94cd42d90c5009ea2d07a54cda.zip"],
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

protobuf_version="09745575a923640154bcf307fba8aedff47f240a"
protobuf_version_sha256="416212e14481cff8fd4849b1c1c1200a7f34808a54377e22d7447efdf54ad758"

http_archive(
    name = "com_google_protobuf",
    url = "https://github.com/protocolbuffers/protobuf/archive/%s.tar.gz" % protobuf_version,
    strip_prefix = "protobuf-%s" % protobuf_version,
    sha256 = protobuf_version_sha256,
)

# bazel-skylib 0.8.0 released 2019.03.20 (https://github.com/bazelbuild/bazel-skylib/releases/tag/0.8.0)
skylib_version = "0.8.0"
http_archive(
    name = "bazel_skylib",
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib.{}.tar.gz".format (skylib_version, skylib_version),
    sha256 = "2ef429f5d7ce7111263289644d233707dba35e39696377ebab8b0bc701f7818e",
)
