def _animals_library_impl(ctx):
    tc = ctx.toolchains["@bazel_tools//tools/jdk:toolchain_type"]
    cat_java = ctx.actions.declare_file("Cat.java")
    ctx.actions.write(cat_java, """
                      package animals;
                      public class Cat {};
                      """)
    cat_jar = ctx.actions.declare_file("Cat.jar")
    java_common.compile(ctx, output = cat_jar, source_files = [cat_java], java_toolchain = tc.java)
    return [
        JavaInfo(output_jar = cat_jar, compile_jar = cat_jar),
    ]

animals_library = rule(
    implementation = _animals_library_impl,
    fragments = ["java"],
    toolchains = ["@bazel_tools//tools/jdk:toolchain_type"],
    attrs = {
        "_java_toolchain": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_toolchain"),
        ),
    },
)
