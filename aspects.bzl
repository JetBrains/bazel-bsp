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
