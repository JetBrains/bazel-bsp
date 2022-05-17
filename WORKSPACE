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
# kotlin

IO_BAZEL_KOTLIN_RULES_TAG = "v1.5.0"

IO_BAZEL_KOTLIN_RULES_SHA = "12d22a3d9cbcf00f2e2d8f0683ba87d3823cb8c7f6837568dd7e48846e023307"

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

BAZEL_SKYLIB_TAG = "1.2.1"

BAZEL_SKYLIB_SHA = "f7be3474d42aae265405a592bb7da8e171919d74c16f082a5457840f06054728"

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

IO_BAZEL_TAG = "5.1.0"

IO_BAZEL_SHA = "a394a99cae2d28179e1afca5f5e867fe36143478b81ccb5713d003dd827cc0fe"

http_archive(
    name = "io_bazel",
    sha256 = IO_BAZEL_SHA,
    strip_prefix = "bazel-{}".format(IO_BAZEL_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(IO_BAZEL_TAG),
)

# ======================================================================================================================
# googleapis - for build protos

GOOGLEAPIS_TAG = "5.1.0"

GOOGLEAPIS_SHA = "a394a99cae2d28179e1afca5f5e867fe36143478b81ccb5713d003dd827cc0fe"

http_archive(
    name = "googleapis",
    sha256 = GOOGLEAPIS_SHA,
    strip_prefix = "bazel-{}/third_party/googleapis".format(GOOGLEAPIS_TAG),
    url = "https://github.com/bazelbuild/bazel/archive/{}.zip".format(GOOGLEAPIS_TAG),
)

# ======================================================================================================================
# rules_python - required by com_google_protobuf

RULES_PYTHON_TAG = "0.8.0"

RULES_PYTHON_SHA = "9fcf91dbcc31fde6d1edb15f117246d912c33c36f44cf681976bd886538deba6"

http_archive(
    name = "rules_python",
    sha256 = RULES_PYTHON_SHA,
    strip_prefix = "rules_python-{}".format(RULES_PYTHON_TAG),
    url = "https://github.com/bazelbuild/rules_python/archive/{}.tar.gz".format(RULES_PYTHON_TAG),
)

# ======================================================================================================================
# zlib - required by com_google_protobuf

ZLIB_TAG = "1.2.12"

ZLIB_SHA = "91844808532e5ce316b3c010929493c0244f3d37593afd6de04f71821d5136d9"

http_archive(
    name = "zlib",
    build_file = "@com_google_protobuf//:third_party/zlib.BUILD",
    sha256 = ZLIB_SHA,
    strip_prefix = "zlib-{}".format(ZLIB_TAG),
    urls = [
        "http://mirror.tensorflow.org/zlib.net/zlib-{}.tar.gz".format(ZLIB_TAG),
        "https://zlib.net/zlib-{}.tar.gz".format(ZLIB_TAG),
    ],
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

BAZEL_SONATYPE_TAG = "0.0.1"

BAZEL_SONATYPE_SHA = "f7889b745694478a2d6a3618315a3e5f9bf1843aabfd7aa4429c4503ff39f397"

http_archive(
    name = "bazel_sonatype",
    sha256 = BAZEL_SONATYPE_SHA,
    strip_prefix = "bazel-sonatype-{}".format(BAZEL_SONATYPE_TAG),
    url = "https://github.com/JetBrains/bazel-sonatype/archive/{}.zip".format(BAZEL_SONATYPE_TAG),
)

# ----------------------------------------------------------------------------------------------------------------------
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
# ----------------------------------------------------------------------------------------------------------------------
# ======================================================================================================================
# maven

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.google.code.gson:gson:2.8.9",
        "com.google.guava:guava:31.0.1-jre",
        "ch.epfl.scala:bsp4j:2.0.0",
        "ch.epfl.scala:bsp-testkit_2.13:2.0.0",
        "commons-cli:commons-cli:jar:1.5.0",
        "io.vavr:vavr:0.10.4",
        "org.apache.logging.log4j:log4j-api:2.17.2",
        "org.apache.logging.log4j:log4j-core:2.17.2",
        "org.apache.commons:commons-collections4:jar:4.4",
        "org.junit.jupiter:junit-jupiter:5.8.2",
        "com.fasterxml.jackson.core:jackson-databind:2.13.2.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2",
        "io.vavr:vavr-jackson:0.10.3",
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo.maven.apache.org/maven2",
    ],
)
