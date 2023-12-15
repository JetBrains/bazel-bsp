load("//aspects:utils/utils.bzl", "abs", "create_struct", "file_location", "get_aspect_ids", "update_sync_output_groups")
load("//aspects:extensions.bzl", "EXTENSIONS", "TOOLCHAINS")

def create_all_extension_info(target, ctx, output_groups, dep_targets):
    info = [create_extension_info(target = target, ctx = ctx, output_groups = output_groups, dep_targets = dep_targets) for create_extension_info in EXTENSIONS]
    return [(file, data) for file, data in info if file != None]

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
    "associates",
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

    srcs_attr = getattr(ctx.rule.attr, "srcs", [])
    sources = []

    if type(srcs_attr) == "list":
        sources = [
            file_location(f)
            for t in srcs_attr
            for f in t.files.to_list()
            if f.is_source
        ]

    resources_attr = getattr(ctx.rule.attr, "resources", [])
    resources = []

    if type(resources_attr) == "list":
        resources = [
            file_location(f)
            for t in resources_attr
            for f in t.files.to_list()
        ]

    aspect_ids = get_aspect_ids(ctx, target)

    result = dict(
        id = str(target.label),
        kind = ctx.rule.kind,
        tags = rule_attrs.tags,
        dependencies = list(all_deps),
        sources = sources,
        resources = resources,
        env = getattr(rule_attrs, "env", {}),
        env_inherit = getattr(rule_attrs, "env_inherit", []),
    )

    extension_info = create_all_extension_info(target, ctx, output_groups, dep_targets)
    extension_exported_properties = dict()
    for (_, data) in extension_info:
        if data != None:
            extension_exported_properties.update(data)

    info_files = [file for (file, _) in extension_info]
    update_sync_output_groups(output_groups, "bsp-target-info", depset(info_files))

    file_name = target.label.name
    file_name = file_name + "-" + str(abs(hash(file_name)))
    if aspect_ids:
        file_name = file_name + "-" + str(abs(hash(".".join(aspect_ids))))
    file_name = "%s.general" % file_name
    file_name = "%s.bsp-info.textproto" % file_name
    info_file = ctx.actions.declare_file(file_name)
    ctx.actions.write(info_file, create_struct(**result).to_proto())
    update_sync_output_groups(output_groups, "bsp-target-info", depset([info_file]))

    exported_properties = dict(
        id = target.label,
        kind = ctx.rule.kind,
        export_deps = export_deps,
        output_groups = output_groups,
        **extension_exported_properties
    )

    return struct(
        bsp_info = struct(**exported_properties),
        output_groups = output_groups,
    )

bsp_target_info_aspect = aspect(
    implementation = _bsp_target_info_aspect_impl,
    required_aspect_providers = [[JavaInfo]],
    attr_aspects = ALL_DEPS,
    toolchains = TOOLCHAINS,
)
