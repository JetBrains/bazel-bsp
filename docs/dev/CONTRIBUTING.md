# Want to contribute? Great! Follow these rules.

- **This project follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)**. \
  _You can download a formatter for Intellij [here](https://plugins.jetbrains.com/plugin/8527-google-java-format)._ \
  **NOTE: there might be a problem with imports order - static imports have to before other imports. You can change it
  in the IntelliJ settings.**
- remember to use [buildifier](https://github.com/bazelbuild/buildtools/blob/master/buildifier/README.md). \
  _The usage: `buildifier -r .`_
- `io.vavr` provides great collection / control types - use them!
- `null` is bad, if something may be nullable / may not exist use `io.vavr.control.Option`.
- try to avoid throwing exceptions - use `io.vavr.control.Try`.
- use `io.vavr.collection.List` instead of `java.util.List` - it allows using `.map` without `.stream()`...
- remember to update the [**changelog**](../../CHANGELOG.md).
- **tests tests tests** - write tests!
