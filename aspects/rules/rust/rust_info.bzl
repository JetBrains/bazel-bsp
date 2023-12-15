load("@rules_rust//rust:rust_common.bzl", "BuildInfo", "CrateInfo")
load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "filter", "flatmap")

# This is supposed to be enum, but Starlark does not support enums.
# See bsp_target_info.proto:RustCrateLocation.
WORKSPACE_DIR = 0
EXEC_ROOT = 1

RUST_TOOLCHAIN_TYPE = "@rules_rust//rust:toolchain_type"

def collect_proc_macro_artifacts(target, kind, ext):
    if not hasattr(target, "actions") or kind != "proc-macro":
        return []

    def is_proc_macro_output_with_ext(output):
        return output.path.endswith(ext)

    return filter(
        is_proc_macro_output_with_ext,
        flatmap(
            lambda _action: _action.outputs.to_list(),
            target.actions,
        ),
    )

def extract_rust_crate_info(target, ctx, **kwargs):
    if CrateInfo not in target:
        return None, None

    if RUST_TOOLCHAIN_TYPE not in ctx.toolchains:
        return None, None

    crate_info = target[CrateInfo]
    build_info = None if not BuildInfo in target else target[BuildInfo]
    toolchain = ctx.toolchains[RUST_TOOLCHAIN_TYPE]

    crate_root_path = crate_info.root.path

    def is_same_crate(dep):
        if CrateInfo not in dep:
            return False

        return dep[CrateInfo].root.path == crate_root_path

    crate_is_from_workspace = not crate_info.root.path.startswith("external/")
    crate_is_generated = not crate_info.root.is_source
    crate_is_in_exec_root = not crate_is_from_workspace or crate_is_generated

    deps = [
        dep[CrateInfo].root.path
        for dep in (ctx.rule.attr.deps + ctx.rule.attr.proc_macro_deps)
        if not is_same_crate(dep) and CrateInfo in dep
    ]

    proc_macro_artifacts = collect_proc_macro_artifacts(target, crate_info.type, toolchain.dylib_ext)
    proc_macro_artifacts_paths = [artifact.path for artifact in proc_macro_artifacts]

    # To obtain crate root file, find directory corresponding to
    # `crate_location` and concatenate it with `crate_id` (relative crate root
    # file path); this has be done in bazel-bsp
    # (see rules_rust/tools/rust_analyzer/rust_project.rs:write_rust_project).
    rust_crate_struct = create_struct(
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
        dependencies_crate_ids = deps,
        crate_root = crate_info.root.path,
        version = ctx.rule.attr.version,
        proc_macro_artifacts = proc_macro_artifacts_paths,
    )

    return create_proto(target, ctx, rust_crate_struct, "rust_crate_info"), None
