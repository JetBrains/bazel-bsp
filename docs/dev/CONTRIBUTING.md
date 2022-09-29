# Want to contribute? Great! Follow these rules.

## currently we are migration codebase to kotlin, so if you are adding a new file it should be written in kotlin!

### java-related rules:

- **This project follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)**. \
  _You can download a formatter for Intellij [here](https://plugins.jetbrains.com/plugin/8527-google-java-format)._ \
  **NOTE: there might be a problem with imports order - static imports have to before other imports. You can change it
  in the IntelliJ settings.**
- Alternatively, you can run:
  ```
    cs launch com.google.googlejavaformat:google-java-format:<version> -- -r @<(git diff --name-only master| grep "[.]java")
  ```
- `io.vavr` provides great collection / control types - use them!
- `null` is bad, if something may be nullable / may not exist use `io.vavr.control.Option`.
- use `io.vavr.collection.List` instead of `java.util.List` - it allows using `.map` without `.stream()`...

### general rules:

- try to avoid throwing exceptions - use `io.vavr.control.Try`.
- remember to use [buildifier](https://github.com/bazelbuild/buildtools/blob/master/buildifier/README.md). \
  _The usage: `buildifier -r .`_
- remember to update the [**changelog**](../../CHANGELOG.md).
- **tests tests tests** - write tests!
