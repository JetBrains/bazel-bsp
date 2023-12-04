# BSP Kotlin

In order to fully have the experience to use BSP over kotlin, make sure that you're using at least
version `0da862aaa11db26f6a113dcec5f6828bfd186ac9` of `rules_kotlin`. This will ensure that compilation diagnostics are
being delivered to you.

```
rules_kotlin_version = "0da862aaa11db26f6a113dcec5f6828bfd186ac9"
rules_kotlin_sha = "6fda2451b9aaf78a8399e6e5d13c31c8ddc558e87de209a7cfd5ddc777ac7877"
http_archive(
    name = "rules_kotlin",
    strip_prefix = "rules_kotlin-0da862aaa11db26f6a113dcec5f6828bfd186ac9",
    urls = ["https://github.com/andrefmrocha/rules_kotlin/archive/%s.tar.gz" % rules_kotlin_version],
    sha256 = rules_kotlin_sha,
)
load("@rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories() # if you want the default
kt_register_toolchains() # to use the default toolchain
```

These changes have not been merged yet and progress can be
accompanied [here](https://github.com/bazelbuild/rules_kotlin/pull/359).