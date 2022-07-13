load("@io_bazel_rules_kotlin//kotlin:core.bzl", "define_kt_toolchain")

exports_files([
    "pom.xml",
])

define_kt_toolchain(
    name = "kotlin_toolchain",
    api_version = "1.6",
    jvm_target = "11",
    language_version = "1.6",
)


#================
load("@rules_intellij//intellij:intellij_toolchain.bzl", "setup_intellij_toolchain")
load("@rules_intellij//intellij:intellij_project.bzl", "setup_intellij_project")
load("@rules_intellij//intellij:indexing.bzl", "generate_indexes")
load("@rules_intellij//intellij:run_intellij.bzl", "run_intellij")

setup_intellij_toolchain(
    name = "ide",
    ide_repo = "idea_ultimate",
    plugins = {
        "ijwb": "@bazel_2022.02.23.0.0-api-version-212//file",
    },
)

setup_intellij_project(
    name = "project",
    project_dir = ".ijwb",
)

generate_indexes(
    name = "indexes",
    deps = [
        "//install",
        "//commons/src/main/java/org/jetbrains/bsp/bazel/commons",
        "//install/src/main/java/org/jetbrains/bsp/bazel/install" # 'indexes' tool does not catch 'exports' correctly
    ],
)

run_intellij(
    name = "idea_run",
    jvm_props = { "idea.platform.prefix": "Idea" },
    indexes = ":indexes"
)

