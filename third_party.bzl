load("@rules_java//java:defs.bzl", "java_test")
load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@io_bazel_rules_scala//scala:scala_cross_version.bzl", "default_maven_server_urls")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

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
    _dependency("com.google.code.gson:gson:2.8.9"),
    _dependency("com.google.guava:guava:31.0.1-jre"),
    _dependency("ch.epfl.scala:bsp4j:2.0.0"),
    _dependency("ch.epfl.scala:bsp-testkit_2.13:2.0.0"),
    _dependency("commons-cli:commons-cli:jar:1.5.0"),
    _dependency("io.vavr:vavr:0.10.4"),
    _dependency("org.apache.logging.log4j:log4j-api:2.17.2"),
    _dependency("org.apache.logging.log4j:log4j-core:2.17.2"),
    _dependency("org.apache.commons:commons-collections4:jar:4.4"),
    _dependency("org.junit.jupiter:junit-jupiter:5.8.2"),  # TODO remove
]

def dependencies():
    maven_install(
        artifacts = _deps,
        repositories = ["https://oss.sonatype.org/content/repositories/snapshots"] + default_maven_server_urls(),
        fetch_sources = True,
    )

# External dependencies & java_junit5_test rule

JUNIT_JUPITER_GROUP_ID = "org.junit.jupiter"
JUNIT_JUPITER_ARTIFACT_ID_LIST = [
    "junit-jupiter-api",
    "junit-jupiter-engine",
    "junit-jupiter-params",
]

JUNIT_PLATFORM_GROUP_ID = "org.junit.platform"
JUNIT_PLATFORM_ARTIFACT_ID_LIST = [
    "junit-platform-commons",
    "junit-platform-console",
    "junit-platform-engine",
    "junit-platform-launcher",
    "junit-platform-suite-api",
]

JUNIT_EXTRA_DEPENDENCIES = [
    ("org.apiguardian", "apiguardian-api", "1.0.0"),
    ("org.opentest4j", "opentest4j", "1.1.1"),
]

def junit_jupiter_java_repositories(
        version = "5.8.2"):
    """Imports dependencies for JUnit Jupiter"""
    for artifact_id in JUNIT_JUPITER_ARTIFACT_ID_LIST:
        jvm_maven_import_external(
            name = _format_maven_jar_name(JUNIT_JUPITER_GROUP_ID, artifact_id),
            artifact = "%s:%s:%s" % (
                JUNIT_JUPITER_GROUP_ID,
                artifact_id,
                version,
            ),
            server_urls = ["https://repo1.maven.org/maven2"],
            licenses = ["notice"],  # EPL 2.0 License
        )

    for t in JUNIT_EXTRA_DEPENDENCIES:
        jvm_maven_import_external(
            name = _format_maven_jar_name(t[0], t[1]),
            artifact = "%s:%s:%s" % t,
            server_urls = ["https://repo1.maven.org/maven2"],
            licenses = ["notice"],  # EPL 2.0 License
        )

def junit_platform_java_repositories(
        version = "1.8.2"):
    """Imports dependencies for JUnit Platform"""
    for artifact_id in JUNIT_PLATFORM_ARTIFACT_ID_LIST:
        jvm_maven_import_external(
            name = _format_maven_jar_name(JUNIT_PLATFORM_GROUP_ID, artifact_id),
            artifact = "%s:%s:%s" % (
                JUNIT_PLATFORM_GROUP_ID,
                artifact_id,
                version,
            ),
            server_urls = ["https://repo1.maven.org/maven2"],
            licenses = ["notice"],  # EPL 2.0 License
        )

def java_junit5_test(name, srcs, test_package, deps = [], runtime_deps = [], **kwargs):
    FILTER_KWARGS = [
        "main_class",
        "use_testrunner",
        "args",
    ]

    for arg in FILTER_KWARGS:
        if arg in kwargs.keys():
            kwargs.pop(arg)

    junit_console_args = []
    if test_package:
        junit_console_args += ["--select-package", test_package]
    else:
        fail("must specify 'test_package'")

    java_test(
        name = name,
        srcs = srcs,
        use_testrunner = False,
        main_class = "org.junit.platform.console.ConsoleLauncher",
        args = junit_console_args,
        deps = deps + [
            _format_maven_jar_dep_name(JUNIT_JUPITER_GROUP_ID, artifact_id)
            for artifact_id in JUNIT_JUPITER_ARTIFACT_ID_LIST
        ] + [
            _format_maven_jar_dep_name(JUNIT_PLATFORM_GROUP_ID, "junit-platform-suite-api"),
        ] + [
            _format_maven_jar_dep_name(t[0], t[1])
            for t in JUNIT_EXTRA_DEPENDENCIES
        ],
        runtime_deps = runtime_deps + [
            _format_maven_jar_dep_name(JUNIT_PLATFORM_GROUP_ID, artifact_id)
            for artifact_id in JUNIT_PLATFORM_ARTIFACT_ID_LIST
        ],
        **kwargs
    )

def _format_maven_jar_name(group_id, artifact_id):
    return ("%s_%s" % (group_id, artifact_id)).replace(".", "_").replace("-", "_")

def _format_maven_jar_dep_name(group_id, artifact_id):
    return "@%s//jar" % _format_maven_jar_name(group_id, artifact_id)
