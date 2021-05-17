def _print_aspect_impl(target, ctx):
    if hasattr(ctx.rule.attr, "srcjar"):
        srcjar = ctx.rule.attr.srcjar
        if srcjar != None:
            for f in srcjar.files.to_list():
                if f != None:
                    print(f.path)
    return []

print_aspect = aspect(
    implementation = _print_aspect_impl,
    attr_aspects = ["deps"],
)

def _scala_compiler_classpath_impl(target, ctx):
    files = depset()
    if hasattr(ctx.rule.attr, "jars"):
        for target in ctx.rule.attr.jars:
            files = depset(transitive = [files, target.files])

    compiler_classpath_file = ctx.actions.declare_file("%s.textproto" % target.label.name)
    ctx.actions.write(compiler_classpath_file, struct(files = [file.path for file in files.to_list()]).to_proto())

    return [
        OutputGroupInfo(scala_compiler_classpath_files = [compiler_classpath_file]),
    ]

scala_compiler_classpath_aspect = aspect(
    implementation = _scala_compiler_classpath_impl,
)

def _fetch_cpp_compiler(target, ctx):
    if cc_common.CcToolchainInfo in target:
        toolchain_info = target[cc_common.CcToolchainInfo]
        print(toolchain_info.compiler)
        print(toolchain_info.compiler_executable)
    return []

fetch_cpp_compiler = aspect(
    implementation = _fetch_cpp_compiler,
    fragments = ["cpp"],
    attr_aspects = ["_cc_toolchain"],
    required_aspect_providers = [[CcInfo]],
)

def _fetch_java_target_version(target, ctx):
    if hasattr(ctx.rule.attr, "target_version"):
        print(ctx.rule.attr.target_version)
    return []

fetch_java_target_version = aspect(
    implementation = _fetch_java_target_version,
    attr_aspects = ["_java_toolchain"],
)

def _get_target_info(ctx, field_name):
    fields = getattr(ctx.rule.attr, field_name, [])
    fields = [ctx.expand_location(field) for field in fields]
    fields = [ctx.expand_make_variables(field_name, field, {}) for field in fields]

    return fields

def _print_fields(fields):
    separator = ","
    print(separator.join(fields))

def _get_cpp_target_info(target, ctx):
    if CcInfo not in target:
        return []

    #TODO: Get copts from semantics
    copts = _get_target_info(ctx, "copts")
    defines = _get_target_info(ctx, "defines")
    linkopts = _get_target_info(ctx, "linkopts")

    linkshared = False
    if hasattr(ctx.rule.attr, "linkshared"):
        linkshared = ctx.rule.attr.linkshared

    _print_fields(copts)
    _print_fields(defines)
    _print_fields(linkopts)
    print(linkshared)

    return []

get_cpp_target_info = aspect(
    implementation = _get_cpp_target_info,
    fragments = ["cpp"],
    required_aspect_providers = [[CcInfo]],
)
