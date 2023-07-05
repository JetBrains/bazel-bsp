def filter(f, xs):
    return [x for x in xs if f(x)]

def map(f, xs):
    return [f(x) for x in xs]

def map_not_none(f, xs):
    rs = [f(x) for x in xs if x != None]
    return [r for r in rs if r != None]

def map_with_resolve_files(f, xs):
    results = []
    resolve_files = []

    for x in xs:
        if x != None:
            res = f(x)
            if res != None:
                a, b = res
                if a != None:
                    results.append(a)
                if b != None:
                    resolve_files += b

    return results, resolve_files

def distinct(xs):
    seen = dict()
    res = []
    for x in xs:
        if x not in seen:
            seen[x] = True
            res.add(x)
    return res

def file_location(file):
    if file == None:
        return None

    return to_file_location(
        file.path,
        file.root.path if not file.is_source else "",
        file.is_source,
        file.owner.workspace_root.startswith("..") or file.owner.workspace_root.startswith("external"),
    )

def _strip_root_exec_path_fragment(path, root_fragment):
    if root_fragment and path.startswith(root_fragment + "/"):
        return path[len(root_fragment + "/"):]
    return path

def _strip_external_workspace_prefix(path):
    if path.startswith("../") or path.startswith("external/"):
        return "/".join(path.split("/")[2:])
    return path

def to_file_location(exec_path, root_exec_path_fragment, is_source, is_external):
    # directory structure:
    # exec_path = (../repo_name)? + (root_fragment)? + relative_path
    relative_path = _strip_external_workspace_prefix(exec_path)
    relative_path = _strip_root_exec_path_fragment(relative_path, root_exec_path_fragment)

    root_exec_path_fragment = exec_path[:-(len("/" + relative_path))] if relative_path != "" else exec_path

    return struct(
        relative_path = relative_path,
        is_source = is_source,
        is_external = is_external,
        root_execution_path_fragment = root_exec_path_fragment,
    )

def get_java_provider(target):
    if hasattr(target, "scala"):
        return target.scala
    if hasattr(target, "kt") and hasattr(target.kt, "outputs"):
        return target.kt
    if JavaInfo in target:
        return target[JavaInfo]
    return None

def get_interface_jars(output):
    if hasattr(output, "compile_jar") and output.compile_jar:
        return [output.compile_jar]
    elif hasattr(output, "ijar") and output.ijar:
        return [output.ijar]
    else:
        return []

def get_source_jars(output):
    if hasattr(output, "source_jars"):
        return output.source_jars
    if hasattr(output, "source_jar"):
        return [output.source_jar]
    return []

def get_generated_jars(provider):
    if hasattr(provider, "java_outputs"):
        return map_with_resolve_files(to_generated_jvm_outputs, provider.java_outputs)

    if hasattr(provider, "annotation_processing") and provider.annotation_processing and provider.annotation_processing.enabled:
        class_jar = provider.annotation_processing.class_jar
        source_jar = provider.annotation_processing.source_jar
        output = struct(
            binary_jars = [file_location(class_jar)],
            source_jars = [file_location(source_jar)],
        )
        resolve_files = [class_jar, source_jar]
        return [output], resolve_files

    return [], []

def to_generated_jvm_outputs(output):
    if output == None or output.generated_class_jar == None:
        return None

    class_jar = output.generated_class_jar
    source_jar = output.generated_source_jar

    output = struct(
        binary_jars = [file_location(class_jar)],
        source_jars = [file_location(source_jar)],
    )
    resolve_files = [class_jar, source_jar]
    return output, resolve_files

def to_jvm_outputs(output):
    if output == None or output.class_jar == None:
        return None

    binary_jars = [output.class_jar]
    interface_jars = get_interface_jars(output)
    source_jars = get_source_jars(output)
    output = struct(
        binary_jars = map(file_location, binary_jars),
        interface_jars = map(file_location, interface_jars),
        source_jars = map(file_location, source_jars),
    )
    resolve_files = binary_jars + interface_jars + source_jars
    return output, resolve_files

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

def extract_kotlin_info(target, ctx):
    # if KtJvmInfo not in target:
    #    return None

    # provider = target[KtJvmInfo]

    if not hasattr(target, "kt"):
        return None

    provider = target.kt

    # Only supports JVM platform now
    if not hasattr(provider, "language_version"):
        return None

    language_version = getattr(provider, "language_version", None)
    api_version = language_version
    associates = getattr(ctx.rule.attr, "associates", [])
    associates_labels = [str(associate.label) for associate in associates]

    # kotlinc_opts_target = getattr(ctx.rule.attr, "kotlinc_opts", None)
    kotlinc_opts = None
    # if kotlinc_opts_target != None and KotlincOptions in kotlinc_opts_target:
    #    kotlinc_opts = kotlinc_opts_target[KotlincOptions]

    kotlin_info = dict(
        language_version = language_version,
        api_version = api_version,
        associates = associates_labels,
    )

    if kotlinc_opts != None:
        kotlin_info["kotlinc_opts"] = kotlinc_opts

    return struct(**kotlin_info)

def extract_runtime_jars(target, provider):
    compilation_info = getattr(provider, "compilation_info", None)

    if compilation_info:
        return compilation_info.runtime_classpath

    return getattr(provider, "transitive_runtime_jars", target[JavaInfo].transitive_runtime_jars)

def extract_compile_jars(provider):
    compilation_info = getattr(provider, "compilation_info", None)
    transitive_compile_time_jars = getattr(provider, "transitive_compile_time_jars", [])

    return compilation_info.compilation_classpath if compilation_info else transitive_compile_time_jars

def extract_java_info(target, ctx, output_groups):
    provider = get_java_provider(target)
    if not provider:
        return None

    if hasattr(provider, "java_outputs") and provider.java_outputs:
        java_outputs = provider.java_outputs
    elif hasattr(provider, "outputs") and provider.outputs:
        java_outputs = provider.outputs.jars
    else:
        return None

    resolve_files = []

    jars, resolve_files_jars = map_with_resolve_files(to_jvm_outputs, java_outputs)
    resolve_files += resolve_files_jars

    generated_jars, resolve_files_generated_jars = get_generated_jars(provider)
    resolve_files += resolve_files_generated_jars

    runtime_jars = extract_runtime_jars(target, provider).to_list()
    compile_jars = extract_compile_jars(provider).to_list()
    source_jars = provider.transitive_source_jars.to_list()
    resolve_files += runtime_jars
    resolve_files += compile_jars
    resolve_files += source_jars

    runtime_classpath = map(file_location, runtime_jars)
    compile_classpath = map(file_location, compile_jars)
    source_classpath = map(file_location, source_jars)

    javac_opts = getattr(ctx.rule.attr, "javacopts", [])
    jvm_flags = getattr(ctx.rule.attr, "jvm_flags", [])
    args = getattr(ctx.rule.attr, "args", [])
    main_class = getattr(ctx.rule.attr, "main_class", None)

    update_sync_output_groups(output_groups, "bsp-ide-resolve", depset(resolve_files))

    return create_struct(
        jars = jars,
        generated_jars = generated_jars,
        runtime_classpath = runtime_classpath,
        compile_classpath = compile_classpath,
        source_classpath = source_classpath,
        javac_opts = javac_opts,
        jvm_flags = jvm_flags,
        main_class = main_class,
        args = args,
    )

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

    update_sync_output_groups(output_groups, "bsp-ide-resolve", depset(resolve_files))

    return struct(compiler_classpath = compiler_classpath)

def create_struct(**kwargs):
    d = {name: kwargs[name] for name in kwargs if kwargs[name] != None}
    return struct(**d)

def extract_java_toolchain(target, ctx, dep_targets):
    toolchain = None

    if hasattr(target, "java_toolchain"):
        toolchain = target.java_toolchain
    elif java_common.JavaToolchainInfo != platform_common.ToolchainInfo and \
         java_common.JavaToolchainInfo in target:
        toolchain = target[java_common.JavaToolchainInfo]

    toolchain_info = None
    if toolchain != None:
        java_home = to_file_location(toolchain.java_runtime.java_home, "", False, True) if hasattr(toolchain, "java_runtime") else None
        toolchain_info = create_struct(
            source_version = toolchain.source_version,
            target_version = toolchain.target_version,
            java_home = java_home,
        )
    else:
        for dep in dep_targets:
            if hasattr(dep.bsp_info, "java_toolchain_info"):
                toolchain_info = dep.bsp_info.java_toolchain_info
                break

    if toolchain_info != None:
        return toolchain_info, dict(java_toolchain_info = toolchain_info)
    else:
        return None, dict()

JAVA_RUNTIME_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:runtime_toolchain_type"

def extract_java_runtime(target, ctx, dep_targets):
    runtime = None

    if java_common.JavaRuntimeInfo in target:  # Bazel 5.4.0 way
        runtime = target[java_common.JavaRuntimeInfo]
    elif JAVA_RUNTIME_TOOLCHAIN_TYPE in ctx.toolchains:  # Bazel 6.0.0 way
        runtime = ctx.toolchains[JAVA_RUNTIME_TOOLCHAIN_TYPE].java_runtime
    else:
        runtime_jdk = getattr(ctx.rule.attr, "runtime_jdk", None)
        if runtime_jdk and java_common.JavaRuntimeInfo in runtime_jdk:
            runtime = runtime_jdk[java_common.JavaRuntimeInfo]

    runtime_info = None
    if runtime != None:
        java_home = to_file_location(runtime.java_home, "", False, True) if hasattr(runtime, "java_home") else None
        runtime_info = create_struct(java_home = java_home)
    else:
        for dep in dep_targets:
            if hasattr(dep.bsp_info, "java_runtime_info"):
                runtime_info = dep.bsp_info.java_runtime_info
                break

    if runtime_info != None:
        return runtime_info, dict(java_runtime_info = runtime_info)
    else:
        return None, dict()

def extract_cpp_target_info(target, ctx):
    if CcInfo not in target:
        return None

    return create_struct(
        copts = getattr(ctx.rule.attr, "copts", []),
        defines = getattr(ctx.rule.attr, "defines", []),
        link_opts = getattr(ctx.rule.attr, "linkopts", []),
        link_shared = getattr(ctx.rule.attr, "linkshared", False),
    )

def get_aspect_ids(ctx, target):
    """Returns the all aspect ids, filtering out self."""
    aspect_ids = None
    if hasattr(ctx, "aspect_ids"):
        aspect_ids = ctx.aspect_ids
    elif hasattr(target, "aspect_ids"):
        aspect_ids = target.aspect_ids
    else:
        return None
    return [aspect_id for aspect_id in aspect_ids if "bsp_target_info_aspect" not in aspect_id]

def abs(num):
    if num < 0:
        return -num
    else:
        return num

def update_sync_output_groups(groups_dict, key, new_set):
    update_set_in_dict(groups_dict, key + "-transitive-deps", new_set)
    update_set_in_dict(groups_dict, key + "-outputs", new_set)
    update_set_in_dict(groups_dict, key + "-direct-deps", new_set)

def update_set_in_dict(input_dict, key, other_set):
    input_dict[key] = depset(transitive = [input_dict.get(key, depset()), other_set])

def _collect_target_from_attr(rule_attrs, attr_name, result):
    """Collects the targets from the given attr into the result."""
    if not hasattr(rule_attrs, attr_name):
        return
    attr_value = getattr(rule_attrs, attr_name)
    type_name = type(attr_value)
    if type_name == "Target":
        result.append(attr_value)
    elif type_name == "list":
        result.extend(attr_value)

def is_valid_aspect_target(target):
    return hasattr(target, "bsp_info")

def collect_targets_from_attrs(rule_attrs, attrs):
    result = []
    for attr_name in attrs:
        _collect_target_from_attr(rule_attrs, attr_name, result)
    return [target for target in result if is_valid_aspect_target(target)]

COMPILE = 0
RUNTIME = 1

COMPILE_DEPS = [
    "deps",
    "jars",
    "exports",
]

PRIVATE_COMPILE_DEPS = [
    "_java_toolchain",
    "_scala_toolchain",
    "_scalac",
    "_jvm",
    "runtime_jdk",
]

RUNTIME_DEPS = [
    "runtime_deps",
]

ALL_DEPS = COMPILE_DEPS + PRIVATE_COMPILE_DEPS + RUNTIME_DEPS

def make_dep(dep, dependency_type):
    return struct(
        id = str(dep.bsp_info.id),
        dependency_type = dependency_type,
    )

def make_deps(deps, dependency_type):
    return [make_dep(dep, dependency_type) for dep in deps]

def _is_proto_library_wrapper(target, ctx):
    if not ctx.rule.kind.endswith("proto_library") or ctx.rule.kind == "proto_library":
        return False

    deps = collect_targets_from_attrs(ctx.rule.attr, ["deps"])
    return len(deps) == 1 and deps[0].bsp_info and deps[0].bsp_info.kind == "proto_library"

def _get_forwarded_deps(target, ctx):
    if _is_proto_library_wrapper(target, ctx):
        return collect_targets_from_attrs(ctx.rule.attr, ["deps"])
    return []

def _bsp_target_info_aspect_impl(target, ctx):
    if target.label.name.endswith(".semanticdb"):
        return []

    rule_attrs = ctx.rule.attr

    direct_dep_targets = collect_targets_from_attrs(rule_attrs, COMPILE_DEPS)
    private_direct_dep_targets = collect_targets_from_attrs(rule_attrs, PRIVATE_COMPILE_DEPS)
    direct_deps = make_deps(direct_dep_targets, COMPILE)

    exported_deps_from_deps = []
    for dep in direct_dep_targets:
        exported_deps_from_deps = exported_deps_from_deps + dep.bsp_info.export_deps

    compile_deps = direct_deps + exported_deps_from_deps

    runtime_dep_targets = collect_targets_from_attrs(rule_attrs, RUNTIME_DEPS)
    runtime_deps = make_deps(runtime_dep_targets, RUNTIME)

    all_deps = depset(compile_deps + runtime_deps).to_list()

    # Propagate my own exports
    export_deps = []
    direct_exports = []
    if JavaInfo in target:
        direct_exports = collect_targets_from_attrs(rule_attrs, ["exports"])
        export_deps.extend(make_deps(direct_exports, COMPILE))
        for export in direct_exports:
            export_deps.extend(export.bsp_info.export_deps)
        export_deps = depset(export_deps).to_list()

    forwarded_deps = _get_forwarded_deps(target, ctx) + direct_exports

    dep_targets = direct_dep_targets + private_direct_dep_targets + runtime_dep_targets + direct_exports
    output_groups = dict()
    for dep in dep_targets:
        for k, v in dep.bsp_info.output_groups.items():
            if dep in forwarded_deps:
                output_groups[k] = output_groups[k] + [v] if k in output_groups else [v]
            elif k.endswith("-direct-deps"):
                pass
            elif k.endswith("-outputs"):
                directs = k[:-len("outputs")] + "direct-deps"
                output_groups[directs] = output_groups[directs] + [v] if directs in output_groups else [v]
            else:
                output_groups[k] = output_groups[k] + [v] if k in output_groups else [v]

    for k, v in output_groups.items():
        output_groups[k] = depset(transitive = v)

    sources = [
        file_location(f)
        for t in getattr(ctx.rule.attr, "srcs", [])
        for f in t.files.to_list()
        if f.is_source
    ]

    resources = [
        file_location(f)
        for t in getattr(ctx.rule.attr, "resources", [])
        for f in t.files.to_list()
    ]

    java_target_info = extract_java_info(target, ctx, output_groups) if not "manual" in rule_attrs.tags else None
    scala_toolchain_info = extract_scala_toolchain_info(target, ctx, output_groups) if not "manual" in rule_attrs.tags else None
    scala_target_info = extract_scala_info(target, ctx, output_groups)
    java_toolchain_info, java_toolchain_info_exported = extract_java_toolchain(target, ctx, dep_targets)
    java_runtime_info, java_runtime_info_exported = extract_java_runtime(target, ctx, dep_targets)
    cpp_target_info = extract_cpp_target_info(target, ctx)
    kotlin_target_info = extract_kotlin_info(target, ctx)

    result = dict(
        id = str(target.label),
        kind = ctx.rule.kind,
        tags = rule_attrs.tags,
        dependencies = list(all_deps),
        sources = sources,
        resources = resources,
        scala_target_info = scala_target_info,
        scala_toolchain_info = scala_toolchain_info,
        java_target_info = java_target_info,
        java_toolchain_info = java_toolchain_info,
        java_runtime_info = java_runtime_info,
        cpp_target_info = cpp_target_info,
        kotlin_target_info = kotlin_target_info,
        env = getattr(rule_attrs, "env", {}),
        env_inherit = getattr(rule_attrs, "env_inherit", []),
    )

    file_name = target.label.name
    file_name = file_name + "-" + str(abs(hash(file_name)))
    aspect_ids = get_aspect_ids(ctx, target)
    if aspect_ids:
        file_name = file_name + "-" + str(abs(hash(".".join(aspect_ids))))
    file_name = "%s.bsp-info.textproto" % file_name
    info_file = ctx.actions.declare_file(file_name)
    ctx.actions.write(info_file, create_struct(**result).to_proto())
    update_sync_output_groups(output_groups, "bsp-target-info", depset([info_file]))

    exported_properties = dict(
        id = target.label,
        kind = ctx.rule.kind,
        export_deps = export_deps,
        output_groups = output_groups,
    )
    exported_properties.update(java_toolchain_info_exported)
    exported_properties.update(java_runtime_info_exported)

    return struct(
        bsp_info = struct(**exported_properties),
        output_groups = output_groups,
    )

bsp_target_info_aspect = aspect(
    implementation = _bsp_target_info_aspect_impl,
    required_aspect_providers = [[JavaInfo]],
    attr_aspects = ALL_DEPS,
    toolchains = [JAVA_RUNTIME_TOOLCHAIN_TYPE],
)
