load("//aspects:utils/utils.bzl", "create_proto", "file_location", "is_external", "map", "update_sync_output_groups")

def find_scalac_classpath(runfiles):
    result = []
    found_scala_compiler_jar = False
    for file in runfiles:
        name = file.basename
        if file.extension == "jar" and ("scala3-compiler" in name or "scala-compiler" in name):
            found_scala_compiler_jar = True
            result.append(file)
        elif file.extension == "jar" and ("scala3-library" in name or "scala3-reflect" in name or "scala-library" in name or "scala-reflect" in name):
            result.append(file)
    return result if found_scala_compiler_jar and len(result) >= 2 else []

def extract_scala_toolchain_info(target, ctx, output_groups, **kwargs):
    runfiles = target.default_runfiles.files.to_list()

    classpath = find_scalac_classpath(runfiles)

    if not classpath:
        return None, None

    resolve_files = classpath
    compiler_classpath = map(file_location, classpath)

    if (is_external(target)):
        update_sync_output_groups(output_groups, "external-deps-resolve", depset(resolve_files))

    scala_toolchain_info = struct(compiler_classpath = compiler_classpath)

    return create_proto(target, ctx, scala_toolchain_info, "scala_toolchain_info"), None

def extract_scala_info(target, ctx, output_groups, **kwargs):
    kind = ctx.rule.kind
    if not kind.startswith("scala_") and not kind.startswith("thrift_"):
        return None, None

    SCALA_TOOLCHAIN = "@io_bazel_rules_scala//scala:toolchain_type"

    # check of _scala_toolchain is necessary, because SCALA_TOOLCHAIN will always be present
    if hasattr(ctx.rule.attr, "_scala_toolchain"):
        common_scalac_opts = ctx.toolchains[SCALA_TOOLCHAIN].scalacopts
    else:
        common_scalac_opts = []
    scalac_opts = common_scalac_opts + getattr(ctx.rule.attr, "scalacopts", [])

    scala_info = struct(scalac_opts = scalac_opts)

    return create_proto(target, ctx, scala_info, "scala_target_info"), None
