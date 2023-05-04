load("@rules_rust//rust:rust_common.bzl", "CrateInfo", "BuildInfo")

def filter(f, xs):
    return [x for x in xs if f(x)]

def map(f, xs):
    return [f(x) for x in xs]

def flatten(xss):
    return [x for xs in xss for x in xs]

def flatmap(f, xs):
    return flatten(map(f, xs))

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

    scalac_opts = getattr(ctx.rule.attr, "scalacopts", [])

    scala_info = struct(
        scalac_opts = scalac_opts,
    )
    return scala_info

def extract_runtime_jars(target, provider):
    compilation_info = getattr(provider, "compilation_info", None)

    if compilation_info:
        return compilation_info.runtime_classpath

    return getattr(provider, "transitive_runtime_jars", target[JavaInfo].transitive_runtime_jars)

def extract_compile_jars(provider):
    compilation_info = getattr(provider, "compilation_info", None)

    return compilation_info.compilation_classpath if compilation_info else provider.transitive_compile_time_jars

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

# This is supposed to be enum, but Starlark does not support enums.
# See bsp_target_info.proto:RustCrateLocation.
WORKSPACE_DIR = 0
EXEC_ROOT = 1

RUST_TOOLCHAIN_TYPE = "@rules_rust//rust:toolchain_type"
RUST_ANALYZER_TOOLCHAIN_TYPE = "@rules_rust//rust/rust_analyzer:toolchain_type"
RUST_TOOLCHAINS_TYPES = [RUST_TOOLCHAIN_TYPE, RUST_ANALYZER_TOOLCHAIN_TYPE]

def collect_proc_macro_artifacts(target, kind, ext):
    if not hasattr(target, "actions") or kind != "proc-macro":
        return []

    def is_proc_macro_output_with_ext(output):
        return output.path.endswith(ext)

    return filter(
        is_proc_macro_output_with_ext,
        flatmap(
            lambda _action: _action.outputs.to_list(),
            target.actions
        )
    )

def extract_rust_version(sysroot_dir):
    split = sysroot_dir.partition("/")
    for path_fragment in split:
        if path_fragment.startswith("rust_analyzer_") and path_fragment.endswith("_tools"):
            strip_prefix = len("rust_analyzer_")
            strip_suffix = len("_tools")
            return path_fragment[strip_prefix:-strip_suffix]
    return None


def rust_analyzer_detect_sysroot(rust_analyzer_toolchain):
    if not rust_analyzer_toolchain.rustc_srcs:
        fail(
            "Current Rust-Analyzer toolchain doesn't contain rustc sources in `rustc_srcs` attribute.",
            "These are needed by rust-analyzer. If you are using the default Rust toolchain, add `rust_repositories(include_rustc_srcs = True, ...).` to your WORKSPACE file.",
        )

    rustc_srcs = rust_analyzer_toolchain.rustc_srcs

    sysroot_src = rustc_srcs.label.package
    if rustc_srcs.label.workspace_root:
        sysroot_src = rustc_srcs.label.workspace_root + "/" + sysroot_src

    rustc = rust_analyzer_toolchain.rustc
    sysroot_dir, _, bin_dir = rustc.dirname.rpartition("/")
    if bin_dir != "bin":
        fail("The rustc path is expected to be relative to the sysroot as `bin/rustc`. Instead got: {}".format(
            rustc.path,
        ))

    version = extract_rust_version(sysroot_dir)

    toolchain_info = {
        "sysroot": sysroot_dir,
        "sysroot_src": sysroot_src,
        "version": version,
    }

    return toolchain_info

# TODO: remove this debug function.
# Generated using regex
def print_toolchain(toolchain):
    print('all_files', toolchain.all_files)
    print('binary_ext', toolchain.binary_ext)
    print('cargo', toolchain.cargo)
    print('clippy_driver', toolchain.clippy_driver)
    print('compilation_mode_opts', toolchain.compilation_mode_opts)
    print('crosstool_files', toolchain.crosstool_files)
    print('default_edition', toolchain.default_edition)
    print('dylib_ext', toolchain.dylib_ext)
    print('env', toolchain.env)
    print('exec_triple', toolchain.exec_triple)
    print('libstd_and_allocator_ccinfo', toolchain.libstd_and_allocator_ccinfo)
    print('llvm_cov', toolchain.llvm_cov)
    print('llvm_profdata', toolchain.llvm_profdata)
    print('make_variables', toolchain.make_variables)
    print('os', toolchain.os)
    print('rust_doc', toolchain.rust_doc)
    print('rust_std', toolchain.rust_std)
    print('rust_std_paths', toolchain.rust_std_paths)
    print('rustc', toolchain.rustc)
    print('rustc_lib', toolchain.rustc_lib)
    print('rustfmt', toolchain.rustfmt)
    print('staticlib_ext', toolchain.staticlib_ext)
    print('stdlib_linkflags', toolchain.stdlib_linkflags)
    print('extra_rustc_flags', toolchain.extra_rustc_flags)
    print('extra_exec_rustc_flags', toolchain.extra_exec_rustc_flags)
    print('sysroot', toolchain.sysroot)
    print('sysroot_short_path', toolchain.sysroot_short_path)
    print('target_arch', toolchain.target_arch)
    print('target_flag_value', toolchain.target_flag_value)
    print('target_json', toolchain.target_json)
    print('target_triple', toolchain.target_triple)
    print('_rename_first_party_crates', toolchain._rename_first_party_crates)
    print('_third_party_dir', toolchain._third_party_dir)
    print('_pipelined_compilation', toolchain._pipelined_compilation)
    print('_experimental_use_cc_common_link', toolchain._experimental_use_cc_common_link)
    print('=' * 250)

def extract_rust_crate_info(target, ctx):

    # TODO: remove DEBUG and related functions.
    #   we keep them for now, as they are useful for target analysis
    DEBUG = True

    if CrateInfo not in target:
        return None

    if RUST_TOOLCHAIN_TYPE not in ctx.toolchains:
        return None

    crate_info = target[CrateInfo]
    build_info = None if not BuildInfo in target else target[BuildInfo]
    toolchain = ctx.toolchains[RUST_TOOLCHAIN_TYPE]
    cargo_bin_path = toolchain.cargo.path
    rustc_host = toolchain.target_flag_value

    if DEBUG:
        print('Analyzing target:', target.label)
        print('toolchain info')
        print_toolchain(toolchain)

    if RUST_ANALYZER_TOOLCHAIN_TYPE in ctx.toolchains:
        rust_analyzer_toolchain = ctx.toolchains[RUST_ANALYZER_TOOLCHAIN_TYPE]
        sysroot = rust_analyzer_detect_sysroot(rust_analyzer_toolchain)
        proc_macro_srv = rust_analyzer_toolchain.proc_macro_srv.path
        rustc_sysroot = sysroot["sysroot"]
        rustc_src_sysroot = sysroot["sysroot_src"]
        rustc_version = sysroot["version"]
    else:
        rustc_sysroot = None
        rustc_src_sysroot = None
        proc_macro_srv = None
        rustc_version = None

    # Uncomment the following code fragment to print everything that
    # can be extracted from the public API.
    #
    # That's too much and too little at the same time for our use case.
    #
    # Some fields (like `crate_rustc_env`) apply directly to the `rustc`
    # compiler and do not have a meaningful translation to `cargo metadata`
    # fields.
    #
    # On the other hand, some cargo-specific data (like the existence of
    # packages with many targets, `features` and `use_default_features` in
    # dependencies, etc.) do not exist in Bazel and low-level `rustc`.
    #
    # debug_struct_ = create_struct(
    #      attributes = ctx.rule.attr,
    #      crate_deps = crate_info.deps,
    #      crate_aliases = crate_info.aliases,
    #      crate_compile_data = crate_info.compile_data,
    #      crate_compile_data_targets = crate_info.compile_data_targets,
    #      crate_edition = crate_info.edition,
    #      crate_is_test = crate_info.is_test,
    #      crate_name = crate_info.name,
    #      crate_output = crate_info.output,
    #      crate_owner = crate_info.owner,
    #      crate_proc_macro_deps = crate_info.owner,
    #      crate_root = crate_info.root,
    #      crate_rustc_env = crate_info.rustc_env,
    #      crate_rustc_env_files = crate_info.rustc_env_files,
    #      crate_type = crate_info.type,
    #      crate_wrapped_crate_type = crate_info.wrapped_crate_type,
    #  )
    #
    # print(debug_struct_)

    crate_root_path = crate_info.root.path

    def is_same_crate(dep):
        if CrateInfo not in dep:
            return False

        return dep[CrateInfo].root.path == crate_root_path

    def wrap_dependency(dep):
      return struct(
        crate_id = dep[CrateInfo].root.path,
        rename = "", # TODO: Without a public `DepInfo` provider, we *cannot* get a rename.
      )

    crate_is_from_workspace = not crate_info.root.path.startswith("external/")
    crate_is_generated = not crate_info.root.is_source
    crate_is_in_exec_root = not crate_is_from_workspace or crate_is_generated

    deps = [wrap_dependency(dep)
            for dep in (ctx.rule.attr.deps + ctx.rule.attr.proc_macro_deps)
            if not is_same_crate(dep) and CrateInfo in dep]

    proc_macro_artifacts = collect_proc_macro_artifacts(target, crate_info.type, toolchain.dylib_ext)
    proc_macro_artifacts_paths = [artifact.path for artifact in proc_macro_artifacts]

    # To obtain crate root file, find directory corresponding to
    # `crate_location` and concatenate it with `crate_id` (relative crate root
    # file path); this has be done in bazel-bsp
    # (see rules_rust/tools/rust_analyzer/rust_project.rs:write_rust_project).
    rust_crate_struct = struct(
        # The `crate-id` field must be unique. The deduplication has to be done
        # in bazel-bsp
        # (see rules_rust/tools/rust_analyzer/aquery.rs:consolidate_crate_specs).
        crate_id = crate_info.root.path,
        location = EXEC_ROOT if crate_is_in_exec_root else WORKSPACE_DIR,
        from_workspace = crate_is_from_workspace,
        name = crate_info.name,
        kind = crate_info.type,
        edition = crate_info.edition,
        out_dir = "" if build_info == None else build_info.out_dir.path,
        crate_features = ctx.rule.attr.crate_features,
        dependencies = deps,
        crate_root = crate_info.root.path,
        version = ctx.rule.attr.version,
        proc_macro_artifacts = proc_macro_artifacts_paths,
        proc_macro_srv = proc_macro_srv,
        rustc_sysroot = rustc_sysroot,
        rustc_src_sysroot = rustc_src_sysroot,
        cargo_bin_path = cargo_bin_path,
        rustc_version = rustc_version,
        rustc_host = rustc_host,
    )

    if DEBUG:
        print(rust_crate_struct)
        print("=" * 120)

    return rust_crate_struct

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
    "proc_macro_deps",
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
    rust_crate_info = extract_rust_crate_info(target, ctx)
    java_toolchain_info, java_toolchain_info_exported = extract_java_toolchain(target, ctx, dep_targets)
    java_runtime_info, java_runtime_info_exported = extract_java_runtime(target, ctx, dep_targets)
    cpp_target_info = extract_cpp_target_info(target, ctx)

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
        rust_crate_info = rust_crate_info,
        cpp_target_info = cpp_target_info,
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
    toolchains = [JAVA_RUNTIME_TOOLCHAIN_TYPE] + RUST_TOOLCHAINS_TYPES,
)

