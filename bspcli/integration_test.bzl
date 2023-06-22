SyntheticProjectInfo = provider(
    fields = {
        "directory": "string value",
    },
)

def _generate_synthetic_project_impl(ctx):
    out_dir = ctx.actions.declare_directory(ctx.attr.project_directory)
    executable = ctx.executable._generator
    ctx.actions.run(
        outputs = [out_dir],
        executable = executable,
        arguments = [out_dir.path, str(ctx.attr.project_size)],
    )
    return [
        DefaultInfo(files = depset([out_dir])),
        SyntheticProjectInfo(directory = out_dir),
    ]

synthetic_bazel_project = rule(
    implementation = _generate_synthetic_project_impl,
    attrs = {
        "_generator": attr.label(
            default = Label("//bspcli:generator"),
            executable = True,
            cfg = "exec",
        ),
        "project_directory": attr.string(mandatory = True),
        "project_size": attr.int(mandatory = True),
    },
)

script_template = """
#!/bin/bash
"{executable}" "{project_path}"
"""

def _bsp_cli_on_large_project_impl(ctx):
    executable = ctx.actions.declare_file("benchmark-%s" % ctx.label.name)
    project_path = ctx.attr.project[SyntheticProjectInfo].directory
    bspcli_executable = ctx.attr.bspcli[DefaultInfo].files_to_run.executable
    script_content = script_template.format(
        executable = bspcli_executable.short_path,
        project_path = project_path.short_path,
    )
    ctx.actions.write(executable, script_content, is_executable = True)
    runfiles = ctx.attr.bspcli[DefaultInfo].default_runfiles.merge(ctx.runfiles([project_path]))
    return [
        DefaultInfo(executable = executable, runfiles = runfiles),
    ]

bsp_cli_on_large_project = rule(
    implementation = _bsp_cli_on_large_project_impl,
    executable = True,
    attrs = {
        "project": attr.label(mandatory = True),
        "bspcli": attr.label(mandatory = True),
    },
)
