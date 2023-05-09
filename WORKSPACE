workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# rules_jvm_external - for maven dependencies

RULES_JVM_EXTERNAL_TAG = "5.2"

RULES_JVM_EXTERNAL_SHA = "f86fd42a809e1871ca0aabe89db0d440451219c3ce46c58da240c7dcdc00125f"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-{}".format(RULES_JVM_EXTERNAL_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

# ======================================================================================================================
# kotlin

IO_BAZEL_KOTLIN_RULES_TAG = "v1.7.1"

IO_BAZEL_KOTLIN_RULES_SHA = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3"

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

BAZEL_SKYLIB_TAG = "1.4.1"

BAZEL_SKYLIB_SHA = "b8a1527901774180afc798aeb28c4634bdccf19c4d98e7bdd1ce79d1fe9aaad7"

http_archive(
    name = "bazel_skylib",
    sha256 = BAZEL_SKYLIB_SHA,
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib-{}.tar.gz".format(BAZEL_SKYLIB_TAG, BAZEL_SKYLIB_TAG),
)

# ======================================================================================================================
# io_bazel_rules_scala - required by bazel_sonatype

IO_BAZEL_RULES_SCALA_TAG = "5.0.0"

IO_BAZEL_RULES_SCALA_SHA = "141a3919b37c80a846796f792dcf6ea7cd6e7b7ca4297603ca961cd22750c951"

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
# todo: merge into the bsp repo?

git_repository(
    name = "testkit",
    commit = "36bb3467d3df351780d0092dadfd9f3313787e8d",
    patch_args = ["-p1"],
    patches = ["//e2e:testkit.patch"],
    remote = "https://github.com/agluszak/bsp-testkit2.git",
)

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# maven

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.google.code.gson:gson:2.8.9",
        "com.google.guava:guava:31.0.1-jre",
        "ch.epfl.scala:bsp4j:2.1.0-M4",
        "ch.epfl.scala:bsp-testkit_2.13:2.0.0",
        "commons-cli:commons-cli:jar:1.5.0",
        # TODO: we need to remove it
        "io.vavr:vavr:0.10.4",
        "org.apache.logging.log4j:log4j-api:2.20.0",
        "org.apache.logging.log4j:log4j-core:2.20.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0",
        "org.junit.jupiter:junit-jupiter:5.9.3",
        "com.fasterxml.jackson.core:jackson-databind:2.15.0",
        "com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0",
        "ch.epfl.scala:bloop-config_2.13:1.5.0",
        "org.scala-lang:scala-library:2.13.10",
        "com.google.protobuf:protobuf-java:3.23.0",
        "io.grpc:grpc-stub:1.55.1",
        "io.grpc:grpc-netty:1.55.1",

        # tests
        "org.junit.jupiter:junit-jupiter-api:5.9.3",
        "org.junit.jupiter:junit-jupiter-engine:5.9.3",
        "org.junit.jupiter:junit-jupiter-params:5.9.3",
        "org.junit.platform:junit-platform-console:1.9.3",
        "io.kotest:kotest-assertions-api-jvm:5.6.1",
        "io.kotest:kotest-assertions-core-jvm:5.6.1",
        "io.kotest:kotest-assertions-shared-jvm:5.6.1",
        "io.kotest:kotest-common-jvm:5.6.1",
        "org.assertj:assertj-core:3.24.2",
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo.maven.apache.org/maven2",
    ],
)
