load("@graknlabs_bazel_distribution//maven:rules.bzl", "assemble_maven", "deploy_maven")

java_binary(
    name = "bsp",
    tags = ["maven_coordinates=org.jetbrains.bsp:bazel-bsp:{pom_version}"],
    deps = [
        "@com_google_protobuf//:protobuf_java",
        "@googleapis//:google_devtools_build_v1_build_events_java_proto",
        "@googleapis//:google_devtools_build_v1_publish_build_event_java_proto",
        "@googleapis//:google_devtools_build_v1_publish_build_event_proto",
        "@io_bazel//third_party/grpc:grpc-jar",
        "@io_bazel//src/main/protobuf:analysis_java_proto",
        "@io_bazel//src/main/protobuf:build_java_proto",
        "@maven//:com_google_code_gson_gson",
        "@maven//:com_google_guava_guava",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:org_eclipse_lsp4j_org_eclipse_lsp4j_jsonrpc",
        "@maven//:org_eclipse_xtext_org_eclipse_xtext_xbase_lib",
        "@googleapis//:google_devtools_build_v1_publish_build_event_java_grpc",
        "@io_bazel//src/main/java/com/google/devtools/build/lib/buildeventstream/proto:build_event_stream_java_proto",
	    "@io_bazel_rules_scala//src/protobuf/io/bazel/rules_scala:diagnostics_java_proto",
    ],
    main_class = "org.jetbrains.bsp.Main",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
)

assemble_maven(
    name = "assemble-maven",
    target = "//main/src/org/jetbrains/bsp:bsp",
    package = "{maven_packages}",
    project_name = "Bazel BSP",
    project_description = "Bazel BSP - A Build Server Protocol implementation for Google's build system Bazel",
    project_url = "https://github.com/jetbrains/bazel-bsp",
    scm_url = "https://github.com/jetbrains/bazel-bsp.git",
    version_file = "//central-sync:VERSION",
    developers = {"1": ["name=Andre Rocha", "email=andre.rocha@jetbrains.com", "organization=JetBrains"],
                  "2": ["name=Daniel Wagner-Hall", "email=dawagner@gmail.com", "organization=SeleniumHQ"],},
    license = "mit"
)

deploy_maven(
    name = "deploy-maven",
    target = ":assemble-maven",
    snapshot = "https://oss.sonatype.org/content/repositories/snapshots/",
    release = "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
)
