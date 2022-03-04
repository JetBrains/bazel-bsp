## Extending

In order to extend BSP server to other languages, make sure it can be supported with the current state of
the  [BSP Protocol](https://github.com/build-server-protocol/build-server-protocol/tree/master/docs). Also, make sure
there's a [client](https://build-server-protocol.github.io/docs/implementations.html#build-clients), that will be able
to support those changes.

For any JVM-language, the only needed changes would be for its specific Compiler Options Request (see, for example
the [Scala Options Request](https://github.com/build-server-protocol/build-server-protocol/blob/master/docs/extensions/scala.md#scalac-options-request))
. If that language does not have its own Options Request, it may be possible to mimic the behavior with current Java or
Scala specific requests. Furthermore, make sure the `FILE_EXTENSIONS` constant holds all the relevant extensions for the
given language, as well as the `KNOWN_SOURCE_ROOTS` holds all known patterns for the given language, and, finally,
the `SUPPORTED_LANGUAGES` should hold the LSP compliant name of the language. Anything else, should just work out of the
box.

Any non-JVM language, will also need to look into the `buildTarget/dependencySources` request, since that request only
searches for transitive dependencies in the form of jars.

To support Compilation Diagnostics for the given language, they must be supported in the bazel side. Find the rules'
implementation for the language [here](https://github.com/bazelbuild/). Let
the [diagnostics proto definition](https://github.com/bazelbuild/rules_scala/blob/master/src/protobuf/io/bazel/rules_scala/diagnostics.proto)
live inside its repository and make it so that the compiler writes the diagnostics to the given file. **Make sure the
file is an output of the `ctx.actions.run()` for the compilation action**. The current state also dictates that the file
must have the keyword `diagnostics` in the name. In `BepServer.java`, the mnemonic of the compilation action of the
language must be added to the `SUPPORTED_ACTIONS` constant.

Compilation Diagnostics should receive native support from bazel, accompany the state of
that [here](https://github.com/bazelbuild/bazel/pull/11766).
