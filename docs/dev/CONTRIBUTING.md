# Want to contribute? Great! Follow these rules.

- write new files in kotlin
- if you add new kotlin library use `kt_jvm_library` from `@rules_kotlin//kotlin:jvm.bzl`
- if you add new test (junit 5) please use `kt_test` from `@//rules/kotlin:junit5.bzl`, with 1-1 test file to test target mapping
- remember to use [buildifier](https://github.com/bazelbuild/buildtools/blob/master/buildifier/README.md). _usage: `buildifier -r .`_
