load("@rules_rust//rust:rust_common.bzl", "CrateInfo", "BuildInfo")
load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "map", "filter")

def flatten(xss):
 return [x for xs in xss for x in xs]

def flatmap(f, xs):
 return flatten(map(f, xs))

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
     # This may not be the most elegant way, but rust providers does not
     #   provide toolchain version. Standard library sources are hidden in
     #   `rust_analyzer_<version>_tools`, so we extract the version from
     #   the path rust-analyzer aspect provides us.
     #   Would be nice to use Regex here, but **Starlark**.
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

def extract_rust_crate_info(target, ctx, **kwargs):
 if CrateInfo not in target:
     return None, None

 if RUST_TOOLCHAIN_TYPE not in ctx.toolchains:
     return None, None

 crate_info = target[CrateInfo]
 build_info = None if not BuildInfo in target else target[BuildInfo]
 toolchain = ctx.toolchains[RUST_TOOLCHAIN_TYPE]
 cargo_bin_path = toolchain.cargo.path
 rustc_host = toolchain.target_flag_value

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

 return create_proto(target, ctx, rust_crate_struct, "rust_crate_info"), None