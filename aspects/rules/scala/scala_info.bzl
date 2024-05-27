load("//aspects:utils/utils.bzl", "file_location", "is_external", "map", "update_sync_output_groups")

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

def extract_scala_info(target, ctx, output_groups, **kwargs):
    kind = ctx.rule.kind
    if not kind.startswith("scala_") and not kind.startswith("thrift_"):
        return None, None

    SCALA_TOOLCHAIN = "@io_bazel_rules_scala//scala:toolchain_type"

    scala_info = {}

    # check of _scala_toolchain is necessary, because SCALA_TOOLCHAIN will always be present
    if hasattr(ctx.rule.attr, "_scala_toolchain"):
        common_scalac_opts = ctx.toolchains[SCALA_TOOLCHAIN].scalacopts
        if hasattr(ctx.rule.attr, "_scalac"):
            scalac = ctx.rule.attr._scalac
            compiler_classpath = find_scalac_classpath(scalac.default_runfiles.files.to_list())
            if compiler_classpath:
                scala_info["compiler_classpath"] = map(file_location, compiler_classpath)
                if is_external(scalac):
                    update_sync_output_groups(output_groups, "external-deps-resolve", depset(compiler_classpath))
    else:
        common_scalac_opts = []
    scala_info["scalac_opts"] = common_scalac_opts + getattr(ctx.rule.attr, "scalacopts", [])

    return dict(scala_target_info = struct(**scala_info)), None
