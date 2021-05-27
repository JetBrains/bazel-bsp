load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@io_bazel_rules_scala//scala:scala_cross_version.bzl", "default_maven_server_urls")

def _dependency(coordinates, exclusions = None):
    artifact = parse.parse_maven_coordinate(coordinates)
    return maven.artifact(
        group = artifact["group"],
        artifact = artifact["artifact"],
        packaging = artifact.get("packaging"),
        classifier = artifact.get("classifier"),
        version = artifact["version"],
        exclusions = exclusions,
    )

_deps = [
    _dependency("com.google.code.gson:gson:2.8.5"),
    _dependency("com.google.guava:guava:28.1-jre"),
    _dependency("ch.epfl.scala:bsp4j:2.0.0-M13+46-d5591706-SNAPSHOT"),
    _dependency("ch.epfl.scala:bsp-testkit_2.13:2.0.0-M13+46-d5591706-SNAPSHOT"),
    _dependency("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.8.0"),
    _dependency("org.eclipse.xtext:org.eclipse.xtext.xbase.lib:2.19.0"),
    _dependency("commons-cli:commons-cli:jar:1.4"),
    _dependency("org.jetbrains:annotations:jar:20.1.0"),
    _dependency("io.vavr:vavr:0.9.0"),
    _dependency("org.apache.logging.log4j:log4j-api:2.6.1"),
    _dependency("org.apache.logging.log4j:log4j-core:2.6.1"),
    _dependency("junit:junit:4.12"),
]

def dependencies():
    maven_install(
        artifacts = _deps,
        repositories = ["https://oss.sonatype.org/content/repositories/snapshots"] + default_maven_server_urls(),
        fetch_sources = True,
    )
