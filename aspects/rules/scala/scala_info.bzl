load("//aspects:utils/java_utils.bzl", "get_java_provider")
load("//aspects:utils/utils.bzl", "file_location", "map")

def find_scalac_classpath(runfiles):
    result = []
    found_scala_compiler_jar = False
    for file in runfiles:
        name = file.basename
        if file.extension == "jar" and "scala-compiler" in name:
            found_scala_compiler_jar = True
            result.append(file)
        elif file.extension == "jar" and ("scala-library" in name or "scala-reflect" in name):
            result.append(file)
    return result if found_scala_compiler_jar and len(result) >= 3 else []

def extract_scala_toolchain_info(target, ctx, output_groups):
    runfiles = target.default_runfiles.files.to_list()

    classpath = find_scalac_classpath(runfiles)

    if not classpath:
        return None

    resolve_files = classpath
    compiler_classpath = map(file_location, classpath)

    return struct(compiler_classpath = compiler_classpath)

def extract_scala_info(target, ctx, output_groups):
    provider = get_java_provider(target)
    if not provider:
        return None

    # proper solution, but it requires adding scala_toolchain to the aspect
    # SCALA_TOOLCHAIN = "@io_bazel_rules_scala//scala:toolchain_type"
    # check of _scala_toolchain is necessary, because SCALA_TOOLCHAIN will always be present
    # if hasattr(ctx.rule.attr, "_scala_toolchain"):
    #    common_scalac_opts = ctx.toolchains[SCALA_TOOLCHAIN].scalacopts
    # else:
    #    common_scalac_opts = []
    # scalac_opts = common_scalac_opts + getattr(ctx.rule.attr, "scalacopts", [])

    scalac_opts = []

    # don't bother inspecting non-scala targets
    if hasattr(ctx.rule.attr, "_scala_toolchain"):
        for action in target.actions:
            if action.mnemonic == "Scalac":
                found_scalac_opts = False
                for arg in action.argv:
                    if arg == "--ScalacOpts":
                        found_scalac_opts = True
                    elif found_scalac_opts:
                        # Next worker option appeared.
                        # Currently --ScalacOpts is the last one, but just in case
                        if arg.startswith("--"):
                            break
                        scalac_opts.append(arg)
                break

    scala_info = struct(
        scalac_opts = scalac_opts,
    )
    return scala_info
