def _print_aspect_impl(target, ctx):
    if hasattr(ctx.rule.attr, 'srcjar'):
        srcjar = ctx.rule.attr.srcjar
        if srcjar != None:
            for f in srcjar.files.to_list():
                if f != None:
                    print(f.path)
    return []

print_aspect = aspect(
    implementation = _print_aspect_impl,
    attr_aspects = ['deps'],
)

def _scala_compiler_classpath_impl(target, ctx):
    files = depset()
    if hasattr(ctx.rule.attr, 'jars'):
        for target in ctx.rule.attr.jars:
            files = depset(transitive=[files, target.files])

    compiler_classpath_file = ctx.actions.declare_file("%s.textproto" % target.label.name)
    ctx.actions.write(compiler_classpath_file, struct(files = [file.path for file in files.to_list()]).to_proto())

    return [
        OutputGroupInfo(scala_compiler_classpath_files = [compiler_classpath_file]),
    ]

scala_compiler_classpath_aspect = aspect(
    implementation = _scala_compiler_classpath_impl,
)
