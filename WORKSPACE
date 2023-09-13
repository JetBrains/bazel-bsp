workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# rules_jvm_external - for maven dependencies

RULES_JVM_EXTERNAL_TAG = "5.3"

RULES_JVM_EXTERNAL_SHA = "d31e369b854322ca5098ea12c69d7175ded971435e55c18dd9dd5f29cc5249ac"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-{}".format(RULES_JVM_EXTERNAL_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

# ======================================================================================================================
# kotlin

IO_BAZEL_KOTLIN_RULES_TAG = "v1.8.1"

IO_BAZEL_KOTLIN_RULES_SHA = "a630cda9fdb4f56cf2dc20a4bf873765c41cf00e9379e8d59cd07b24730f4fde"

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = IO_BAZEL_KOTLIN_RULES_SHA,
    url = "https://github.com/bazelbuild/rules_kotlin/releases/download/{}/rules_kotlin_release.tgz".format(IO_BAZEL_KOTLIN_RULES_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

kotlin_repositories()

# ----------------------------------------------------------------------------------------------------------------------

register_toolchains("//:kotlin_toolchain")

# ======================================================================================================================
# bazel_skylib - starlark functions

BAZEL_SKYLIB_TAG = "1.4.2"

BAZEL_SKYLIB_SHA = "66ffd9315665bfaafc96b52278f57c7e2dd09f5ede279ea6d39b2be471e7e3aa"

http_archive(
    name = "bazel_skylib",
    sha256 = BAZEL_SKYLIB_SHA,
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

# ======================================================================================================================
# io_bazel_rules_scala - required by bazel_sonatype

IO_BAZEL_RULES_SCALA_TAG = "6.1.0"

IO_BAZEL_RULES_SCALA_SHA = "cc590e644b2d5c6a87344af5e2c683017fdc85516d9d64b37f15d33badf2e84c"

http_archive(
    name = "io_bazel_rules_scala",
    sha256 = IO_BAZEL_RULES_SCALA_SHA,
    strip_prefix = "rules_scala-{}".format(IO_BAZEL_RULES_SCALA_TAG),
    url = "https://github.com/bazelbuild/rules_scala/archive/refs/tags/v{}.tar.gz".format(IO_BAZEL_RULES_SCALA_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//:scala_config.bzl", "scala_config")

scala_config(scala_version = "2.13.6")

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories()

# ======================================================================================================================
# io_bazel - for protobuf

IO_BAZEL_TAG = "5.4.1"

IO_BAZEL_SHA = "5463df80d3a6ea0872ff7da2049f0284f28d01fd76dfc66838eceea78cf5be57"

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-{}".format(IO_BAZEL_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(IO_BAZEL_TAG),
)

# ======================================================================================================================
# googleapis - for build protos

GOOGLEAPIS_TAG = "5.4.1"

GOOGLEAPIS_SHA = "5463df80d3a6ea0872ff7da2049f0284f28d01fd76dfc66838eceea78cf5be57"

http_archive(
    name = "googleapis",
    sha256 = GOOGLEAPIS_SHA,
    strip_prefix = "bazel-{}/third_party/googleapis".format(GOOGLEAPIS_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(GOOGLEAPIS_TAG),
)

# ======================================================================================================================
# rules_proto

RULES_PROTO_TAG = "4.0.0-3.20.0"

RULES_PROTO_SHA = "e017528fd1c91c5a33f15493e3a398181a9e821a804eb7ff5acdd1d2d6c2b18d"

http_archive(
    name = "rules_proto",
    sha256 = RULES_PROTO_SHA,
    strip_prefix = "rules_proto-{}".format(RULES_PROTO_TAG),
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/refs/tags/{}.tar.gz".format(RULES_PROTO_TAG),
    ],
)

# ----------------------------------------------------------------------------------------------------------------------
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

# ======================================================================================================================
# bazel_sonatype - for publish

BAZEL_SONATYPE_TAG = "1.1.1"

BAZEL_SONATYPE_SHA = "6d1bc7da15dae958274df944eb46e9757e14187cda6decd66fc1aeeb1ea21758"

http_archive(
    name = "bazel_sonatype",
    sha256 = BAZEL_SONATYPE_SHA,
    strip_prefix = "bazel-sonatype-{}".format(BAZEL_SONATYPE_TAG),
    url = "https://github.com/JetBrains/bazel-sonatype/archive/v{}.zip".format(BAZEL_SONATYPE_TAG),
)

# --------------------------------------------------------------------------------------------------------------------
load("@bazel_sonatype//:defs.bzl", "sonatype_dependencies")

sonatype_dependencies()

# ======================================================================================================================
# the new testkit

git_repository(
    name = "testkit",
    commit = "ee5970a56498ff511f36a278e3c9a6bbc8f9fc6f",
    remote = "https://github.com/build-server-protocol/bsp-testkit2.git",
)

# ======================================================================================================================
# e2e tests

RULES_BAZEL_INTEGRATION_TEST_TAG = "0.17.0"

RULES_BAZEL_INTEGRATION_TEST_SHA = "d6dada79939533a8127000d2aafa125f29a4a97f720e01c050fdeb81b1080b08"

http_archive(
    name = "rules_bazel_integration_test",
    sha256 = RULES_BAZEL_INTEGRATION_TEST_SHA,
    url = "https://github.com/bazel-contrib/rules_bazel_integration_test/releases/download/v{}/rules_bazel_integration_test.v{}.tar.gz"
        .format(RULES_BAZEL_INTEGRATION_TEST_TAG, RULES_BAZEL_INTEGRATION_TEST_TAG),
)

# --------------------------------------------------------------------------------------------------------------------
load("@rules_bazel_integration_test//bazel_integration_test:deps.bzl", "bazel_integration_test_rules_dependencies")

bazel_integration_test_rules_dependencies()

# --------------------------------------------------------------------------------------------------------------------
load("@cgrindel_bazel_starlib//:deps.bzl", "bazel_starlib_dependencies")

bazel_starlib_dependencies()

# --------------------------------------------------------------------------------------------------------------------
load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# --------------------------------------------------------------------------------------------------------------------
load("@rules_bazel_integration_test//bazel_integration_test:defs.bzl", "bazel_binaries")

bazel_binaries(versions = [
    "6.3.2",
    "5.3.2",
])

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# maven

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.google.code.gson:gson:2.10.1",
        "com.google.guava:guava:31.0.1-jre",
        "ch.epfl.scala:bsp4j_2.13:2.1.0-M6.alpha",
        "commons-cli:commons-cli:jar:1.5.0",
        "org.apache.logging.log4j:log4j-api:2.20.0",
        "org.apache.logging.log4j:log4j-core:2.20.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3",
        "org.junit.jupiter:junit-jupiter:5.10.0",
        "com.fasterxml.jackson.core:jackson-databind:2.15.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2",
        "com.google.protobuf:protobuf-java:3.24.3",
        "io.grpc:grpc-stub:1.58.0",

        # tests
        "org.junit.jupiter:junit-jupiter-api:5.10.0",
        "org.junit.jupiter:junit-jupiter-engine:5.10.0",
        "org.junit.jupiter:junit-jupiter-params:5.10.0",
        "org.junit.platform:junit-platform-console:1.10.0",
        "io.kotest:kotest-assertions-api-jvm:5.7.2",
        "io.kotest:kotest-assertions-core-jvm:5.7.2",
        "io.kotest:kotest-assertions-shared-jvm:5.7.2",
        "io.kotest:kotest-common-jvm:5.7.2",
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo.maven.apache.org/maven2",
    ],
)
