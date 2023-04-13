workspace(name = "bazel_bsp")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# ======================================================================================================================
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# rules_jvm_external - for maven dependencies

RULES_JVM_EXTERNAL_TAG = "5.1"

RULES_JVM_EXTERNAL_SHA = "8c3b207722e5f97f1c83311582a6c11df99226e65e2471086e296561e57cc954"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-{}".format(RULES_JVM_EXTERNAL_TAG),
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG)
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

scala_config(scala_version = "2.13.6")

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

# ----------------------------------------------------------------------------------------------------------------------
load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories()

# ======================================================================================================================
# io_bazel - for protobuf

IO_BAZEL_TAG = "5.4.0"

IO_BAZEL_SHA = "d9e96269e67d270976a9c4ae6e81b86f54fceb736627bf0a1ba9816ec60d2e78"

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-{}".format(IO_BAZEL_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(IO_BAZEL_TAG),
)

# ======================================================================================================================
# googleapis - for build protos

GOOGLEAPIS_TAG = "5.4.0"

GOOGLEAPIS_SHA = "d9e96269e67d270976a9c4ae6e81b86f54fceb736627bf0a1ba9816ec60d2e78"

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
# junit5

load("//:junit5.bzl", "junit_jupiter_java_repositories", "junit_platform_java_repositories")

JUNIT_JUPITER_VERSION = "5.8.2"

JUNIT_PLATFORM_VERSION = "1.8.2"

junit_jupiter_java_repositories(
    version = JUNIT_JUPITER_VERSION,
)

junit_platform_java_repositories(
    version = JUNIT_PLATFORM_VERSION,
)

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
        "io.vavr:vavr:0.10.4",
        "org.apache.logging.log4j:log4j-api:2.20.0",
        "org.apache.logging.log4j:log4j-core:2.20.0",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4",
        "org.junit.jupiter:junit-jupiter:5.9.2",
        "com.fasterxml.jackson.core:jackson-databind:2.14.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2",
        "ch.epfl.scala:bloop-config_2.13:1.5.0",
        "org.scala-lang:scala-library:2.13.10",
        "com.google.protobuf:protobuf-java:3.22.2",
        "io.grpc:grpc-stub:1.54.0",
        "io.grpc:grpc-netty:1.54.0",
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo.maven.apache.org/maven2",
    ],
)
