# Changelog


## [Unreleased]

## [3.2.0] - 10.04.2024

### Features 🎉

- `workspace/directories` implementation
  | [43abdfb](https://github.com/JetBrains/bazel-bsp/commit/43abdfbb2c31c386905a7539d04c051ae8d06100)
- Experimental Bazel JVM target debugging through BSP.
  | [94f2af7](https://github.com/JetBrains/bazel-bsp/commit/94f2af710dd9c5bc45aa0d577ee27a12e415cb5e)
- Add `workspace/invalidTargets` endpoint.
  | [14a117f](https://github.com/JetBrains/bazel-bsp/commit/14a117ff96311255bddbe931d7fbd5bfdb2ac484)
- The server will now download bazelisk if Bazel is not found in the PATH.
  | [89beaf7](https://github.com/JetBrains/bazel-bsp/commit/89beaf714d11420c05f5321d794af59691a407a6)
- Support bzlmod in rules detection mechanism.
  | [47cdfa8](https://github.com/JetBrains/bazel-bsp/commit/47cdfa8b472f1fb111c86965360ad990d6c4670f)
- Support kotlin stdlibs in bzlmod project.
  | [24aa30d](https://github.com/JetBrains/bazel-bsp/commit/24aa30d1f6377e5966eaf0243071dc314f3e69fc)
- Experimental Rust support.
  | [ec66172](https://github.com/JetBrains/bazel-bsp/commit/ec66172246bb921d60759158408dccee9f2a33da)
- Provide the `android.jar` library for Android targets.
  | [c4aaf5e](https://github.com/JetBrains/bazel-bsp/commit/c4aaf5eb45eb2e04e020a0309a108de50c5a7672)
- Assign value of javac -target flag to `JvmBuildTarget`'s `javaVersion`.
  | [9098181](https://github.com/JetBrains/bazel-bsp/commit/9098181b09ea3916d504389fa1081d45d8fc7f89)
- `workspace/inverseSources` with now uses query.
  | [26ef246](https://github.com/JetBrains/bazel-bsp/commit/26ef246d18acb1697e84a3ca1387ed0144b1717c)
- Add an option to override detected rules.
  | [8230406](https://github.com/JetBrains/bazel-bsp/commit/8230406bf4815b1ce74e329b8a2e301179648b23)
- Server creates an empty project view file if it doesn't exist instead of failing.
  | [660d9cd](https://github.com/JetBrains/bazel-bsp/commit/660d9cd7ad9d05db88065a71a2aae1c34288adbb)
- Add Android resources to the project model.
  | [33760f0](https://github.com/JetBrains/bazel-bsp/commit/33760f0cbb3b642ebb19d703bf5f9c6ff7648f7e)
- Add manifest and resourceFolders to `AndroidBuildTarget`.
  | [2989f70](https://github.com/JetBrains/bazel-bsp/commit/2989f7045ce53eff984bbd048f33d0457be634d9)
- Filtering out libraries with no interface jars.
  | [ec41697](https://github.com/JetBrains/bazel-bsp/commit/ec416974a9049133a4c754403f3e5f3210ae5751)
- Add the `mobileInstall` request.
  | [a4dc63c](https://github.com/JetBrains/bazel-bsp/commit/a4dc63c2410a10f51dfb6e25c6ed36126b74eb2b)
- `aar_import` support.
  | [3037649](https://github.com/JetBrains/bazel-bsp/commit/30376490f2cbbd51534b3458b987a1ad33f7bbbf)
- `android_local_test` support.
  | [ebb4676](https://github.com/JetBrains/bazel-bsp/commit/ebb46765f747adef5cb4aa9fcafe3180a1018b05)
- Jetpack Compose preview support.
  | [dc830f8](https://github.com/JetBrains/bazel-bsp/commit/dc830f86ba881c9fb0e832822b8be2c3d71cb3da)
- Add "build and get build targets" BSP endpoint for build & resync action.
  | [774e427](https://github.com/JetBrains/bazel-bsp/commit/774e4270c84eb1d97e8acd0105adc3ebb3968812)
- Add support for `buildTarget/jvmCompileClasspath`.
  | [cd00ed5](https://github.com/JetBrains/bazel-bsp/commit/cd00ed5f3993a17ead460cc13e160fa82800153d)
- Add flag `ide_java_home_override` to project view to override the java home to work with local IDE. 
  | [c0de197](https://github.com/JetBrains/bazel-bsp/commit/c0de197e60fd1bac748df85230c378252976982e)
- Add `dependencyModules` request support.
  | [e62f321](https://github.com/JetBrains/bazel-bsp/commit/e62f3215e3ccc4fc5a30e43a34cb50a54b270074)
- Fix task workflow.
  | [73008c5](https://github.com/JetBrains/bazel-bsp/commit/73008c5a49233c693d83be73a85f5e936e77713f)
- AIDL import support.
  | [7571695](https://github.com/JetBrains/bazel-bsp/commit/75716956a6daad80cd2530e1e6dd0f7dc978854f)

### Fixes 🛠️

- Server logs non-existing files instead of failing.
  | [54a1fecc](https://github.com/JetBrains/bazel-bsp/commit/54a1fecc62ce09dbd741ff17bb773e7f5148fccf)
- Server collects more jars from `rules_kotlin`'s `KtJvmInfo` to handle the lack of generated jars.
  | [e7faadc](https://github.com/JetBrains/bazel-bsp/commit/e7faadcdc25f2bb2355b609ea38cd42a674a2f25)
- Server allows aspects to be injected normally when users enable bzlmod.
  | [84c4f9a](https://github.com/JetBrains/bazel-bsp/commit/84c4f9a7f5f9c179bb4e61893fd960b04a7dec45)
- Project cache correctly deserializes kotlin modules.
  | [cd64dbb](https://github.com/JetBrains/bazel-bsp/commit/cd64dbb349c16c9cd09d97d09c313665dd4b4356)
- Remove build_flags from non-build calls.
  | [242995b](https://github.com/JetBrains/bazel-bsp/commit/242995b140f9827c91918f7ae79db04b052db187)
- Add fallback for custom bazel version.
  | [9ad762b](https://github.com/JetBrains/bazel-bsp/commit/9ad762bc31627f903a411770f9fde3354d05109b)
- Python extension is always loaded and projects created using native python rules get their Python data collected.
  | [74ba7c9](https://github.com/JetBrains/bazel-bsp/commit/74ba7c949a6988e93929d9713216cfb905de950b)
- Resources should check type too.
  | [02a9e01](https://github.com/JetBrains/bazel-bsp/commit/02a9e01c6296a8b84cf3f753f52df031517893e1)
- Compute classpath lazily in `JvmEnvironment` requests.
  | [f6823e7](https://github.com/JetBrains/bazel-bsp/commit/f6823e719d6e1daa0dea22f7a3c90dc6db9bf1cc)
- Read classpath on demand in javacOptions and scalacOptions requests handlers.
  | [a10ecb0](https://github.com/JetBrains/bazel-bsp/commit/a10ecb0674fc78815cceac05a7b6ca4623da3fd3)
- Kotlin stdlibs now are obtained from toolchain instead of classpath.
  | [e6ad2a0](https://github.com/JetBrains/bazel-bsp/commit/e6ad2a0f29386fa3c81450eb46c222c000eb2ea5)
- Kotlin is set to have higher precedence than Java to always return Kotlin data object if exists.
  | [9915acf](https://github.com/JetBrains/bazel-bsp/commit/9915acf14e6f19c605939518eefe0079a2ab940a)
- Compiler errors should be parsed properly and handle bytestream provider uri.
  | [80ec82c](https://github.com/JetBrains/bazel-bsp/commit/80ec82c989362382088a20ecb16c803133afef06)
- File system in the installer is now closed after use.
  | [2f86f1d](https://github.com/JetBrains/bazel-bsp/commit/2f86f1dfef01038b6ab2b175a3b86b1cca3fd4f9)
- Server does not build the project on sync for newer Bazel versions.
  | [6ec65a8](https://github.com/JetBrains/bazel-bsp/commit/6ec65a8a50b30ef418db4643b3894ad3e0422c22)
- `CARGO_BAZEL_REPIN` flag is used only during sync.
  | [cac0b01](https://github.com/JetBrains/bazel-bsp/commit/cac0b011b2a0243c5e8cd14e4d3768c206400820)
- Fix issue if classpath query returns more results.
  | [67692aa](https://github.com/JetBrains/bazel-bsp/commit/67692aa670b4f66353d1f0dd73e944fcbc2119e3)
- Server handles properly remote artefacts after sync.
  | [0cc0014](https://github.com/JetBrains/bazel-bsp/commit/0cc0014ec7a81cf48955b62b7c0030f2f28804c9)
- `canDebug` flag is now set to true if target is runnable.
  | [55e4faa](https://github.com/JetBrains/bazel-bsp/commit/55e4faaa6d2aebd1aa5f3e8059642f31dd5574f1)
- Fix the `@@` prefix issue if `bazel.module` and `WORKSPACE` are used together.
  | [86fd20b](https://github.com/JetBrains/bazel-bsp/commit/86fd20bc825d01ebc7035d17fd41e3960eb44167)
- Avoid `SLF4J` "Failed to load class" warning.
  | [a1ef51f](https://github.com/JetBrains/bazel-bsp/commit/a1ef51fd630800beccb6aae57bd9fd92c39042bb)
- `bazel-<workspace-name>` symlink exclusion uses real name of the workspace instead of sanitized one.
  | [b9598d9](https://github.com/JetBrains/bazel-bsp/commit/b9598d9f9c105560292d6075234acbded12eab24)
- ijars are included in libraries.
  | [c66358e](https://github.com/JetBrains/bazel-bsp/commit/c66358e0e847bfdd5c35c98adf7e2adb6460a84f)
- The spurious "too many open files" issue has been fixed.
  | [28797b6](https://github.com/JetBrains/bazel-bsp/commit/28797b6894440462b319537eff462a4e231b21e6)
- android features are enabled only if `rules_android` are in `enabled_rules`.
  | [33daf98](https://github.com/JetBrains/bazel-bsp/commit/33daf98247a0d84b94c06bfe7fa9695976755a41)
- `enabled_rules` section is reread on each sync.
  | [b1655d9](https://github.com/JetBrains/bazel-bsp/commit/b1655d9ec6015849263bd63310f979a06f81dd0c)
- Test targets are always included in targets, not libraries.
  | [b117aec](https://github.com/JetBrains/bazel-bsp/commit/b117aeccbd035cad7fb6052903e0661e255552c3)
- Fix showing Scala 3 diagnostics.
  | [3629a3f](https://github.com/JetBrains/bazel-bsp/commit/3629a3faa88d9a4d87df5fa917286b02805db558)
- Use Android flags for run and test to avoid cache invalidation
  | [ade3e68](https://github.com/JetBrains/bazel-bsp/commit/ade3e6825d1e7cae2fb78915d230965ba2e86154)


## [3.1.0] - 18.09.2023

### Security 🚨

- Bsp cli uses the right permissions for bspcli tmp directory.
  | [7fded76](https://github.com/JetBrains/bazel-bsp/commit/7fded7603a53931c3b50a43e5e6345f683d4a4be)
- Server does not use TCP sockets to connect with BES.
  | [aac294d](https://github.com/JetBrains/bazel-bsp/commit/aac294d7c9d41d4ace2c01aba33d1bfe8cfabd0b)

### Features 🎉

- The server generates `extensions.bzl` based on languages (external rules) relevant to the project.
  | [a29c1da](https://github.com/JetBrains/bazel-bsp/commit/a29c1dab6a91888a5358557414f48a0bb0c44c9d)
- Adding available sources to libraries for workspace/libraries call.
  | [2e17ea0](https://github.com/JetBrains/bazel-bsp/commit/2e17ea0ee212d362b1569631e6b979a73ba1e013)
- Enhance Kotlinc Opts support.
  | [6b45eb6](https://github.com/JetBrains/bazel-bsp/commit/6b45eb61fb90c143b845cae69e2f69c6c1b4460d)
- Include libraries defined in `jdeps` files during sync.
  | [bb47e49](https://github.com/JetBrains/bazel-bsp/commit/bb47e493fc595ddf21438f454cee7a6cd756fc0b)
- Add support for buildTarget/jvmCompileClasspath
  | [f7f2662](https://github.com/JetBrains/bazel-bsp/commit/f7f26623ce3254b2f1ecda95329b665d05862109)

### Fixes 🛠️

- Avoid using execroot if there are alternative options.
  | [6a8a7ac](https://github.com/JetBrains/bazel-bsp/commit/6a8a7ac4d3b823695fd58312f96bcb683582b55f)
- Fixed `resolveOutput` implementation - now should work with projects without symlinks like bazel-bin, bazel-out.
  | [eb5df0a](https://github.com/JetBrains/bazel-bsp/commit/eb5df0a90555cf4e99d3332de1f0cbc749d95f0c)
- Aspects don't fail if target contains another target as `srcs` attribute.
  | [637f0d9](https://github.com/JetBrains/bazel-bsp/commit/637f0d966ab96dc92ad32b028c82d2c207be8288)
- Server can obtain scala 3 compiler.
  | [c4a6701](https://github.com/JetBrains/bazel-bsp/commit/c4a67011f76d76ab3de37e8e1592ebbece76b04b)
- Correctly find symlink names even after folder renaming.
  | [f4fb71b](https://github.com/JetBrains/bazel-bsp/commit/f4fb71b019515574376f3a10c297f300acde4701)
- Server does not omit targets that contain dots.
  | [1c51f02](https://github.com/JetBrains/bazel-bsp/commit/1c51f02a4331c331a0d7d4cc412bfd1e36daf77e)
- Server adds sources to generated libs.
  | [eaa5161](https://github.com/JetBrains/bazel-bsp/commit/eaa5161fe4193268c21c324f27786f5f17f79afd)
- Support Scala 3 diagnostics.
  | [744735f](https://github.com/JetBrains/bazel-bsp/commit/744735ff30c96f218414514e99019c8ffc700dfe)

## [3.0.0] - 09.08.2023

### BREAKING CHANGES 🚨

- The server no longer builds the project on initial sync -
  in order to generate and collect generated sources build + resync is required.
  | [ba44564](https://github.com/JetBrains/bazel-bsp/commit/ba445645a3b6d41777dc7ce43f1e821e323f4045)
- Project view `targets` has its default value changed from all targets (`//...`) to no targets.
  | [779fade](https://github.com/JetBrains/bazel-bsp/commit/779fade5dadf2c411a4147412c017e25c316e5eb).
- Bloop support has been dropped.
  | [3d23205](https://github.com/JetBrains/bazel-bsp/commit/3d23205d82e5a877a31c452c27fc89370780657b)
- Project view file is obligatory now! Server requires path to the file in `argv` in `.bsp/bazelbsp.json`.
  Debugger address (`debugger_address`), java path (`java_path`) and flag for trace log (`produce_trace_log`) are *no 
  longer* fields in project view files! They can be set *only* using installer flags (check [README](install/README.md)).
  | [f2423bb](https://github.com/JetBrains/bazel-bsp/commit/f2423bb8093ad186ac4b7eff925e50818348ca72)
- Project view `bazel_path` has been renamed to `bazel_binary`
  (now it's compatible with https://ij.bazel.build/docs/project-views.html#bazel_binary).
  | [5bd2a06](https://github.com/JetBrains/bazel-bsp/commit/5bd2a060d9f0172c91904fe9464900048ab93834)

### Features 🎉

- Support for Python targets, including `buildTarget/pythonOptions` endpoint
  (a big thank you to the students of the University of Warsaw!).
  | [8152cc2](https://github.com/JetBrains/bazel-bsp/commit/8152cc23b4ea26bfe6180b77ae8e45ae48e157e4)
- Enhance support for Kotlin by providing Kotlin target's specific info
  | [c06acc6](https://github.com/JetBrains/bazel-bsp/commit/c06acc6a577f4d146a63d81d529627aaaeb66fee)
- Experimental `workspace/libraries` endpoint that returns list of external libraries.
  | [3360353](https://github.com/JetBrains/bazel-bsp/commit/3360353d9a2d12931972ea3f6bf80e2b04cfa237)

### Fixes 🛠️
- Now we report the failure of the whole test target and binaries are reporting stdout.
  | [224c1ec](https://github.com/JetBrains/bazel-bsp/commit/224c1ec08fc1111f90431926bf7d6dd1aa7f6ab3)
- Aspects don't throw an exception for kotlin rules if an attr doesn't exist.
  | [f08fe8b](https://github.com/JetBrains/bazel-bsp/commit/f08fe8bc2e1862b2c8de0a213b2d1a3042e250d2)
- Fix 'this.bepLogger is null' error on project sync.
  | [956bc00](https://github.com/JetBrains/bazel-bsp/commit/956bc000455b10466fb2f84f8c54dfe28109501b)

### Other changes 🔁
- Aspects now are more structured - each rule support has been extracted to separate files.
  | [b11e3a1](https://github.com/JetBrains/bazel-bsp/commit/b11e3a1e1c5206689cda8d68cf7eb4d14ff6340f)

## [2.7.2]

### Fixes 🛠️
- Collect scalac options from the toolchain
  | [#433](https://github.com/JetBrains/bazel-bsp/pull/433)
- Add support for Scala SDK provided by rules_jvm_external
  | [#403](https://github.com/JetBrains/bazel-bsp/pull/403)

### Performance
-  Reduce peak memory footprint
   | [#428](https://github.com/JetBrains/bazel-bsp/pull/428)

## [2.7.1]

### Fixes 🛠️
-  Publish `build/publishDiagnostics` with an empty array of diagnostics to clear former diagnotics.
   | [#381](https://github.com/JetBrains/bazel-bsp/pull/381)
-  Prioritize most frequently used JDKs when selecting the project JDK.
   | [#420](https://github.com/JetBrains/bazel-bsp/pull/420)
-  Fix ide classpath computation for recent rules_jvm_external.
   | [#421](https://github.com/JetBrains/bazel-bsp/pull/421)
-  Process exit instead of hang in case of uncaught exception in pooled threads
   | [#425](https://github.com/JetBrains/bazel-bsp/pull/425),
   [#426](https://github.com/JetBrains/bazel-bsp/pull/426)

## [2.7.0]

### Features 🎉
- Server uses BEP to log bazel progress.
  | [ae52b8f](https://github.com/JetBrains/bazel-bsp/commit/ae52b8f401b793ba15e84d492ba0f72a462b74dc)

### Fixes 🛠️
-  Add class jars generated during annotation processing.
  | [#372](https://github.com/JetBrains/bazel-bsp/pull/372)
- Set PublishDiagnosticsParams.reset to be true.
  | [#377](https://github.com/JetBrains/bazel-bsp/pull/377)
- Update document about how to use projectview.
  | [#383](https://github.com/JetBrains/bazel-bsp/pull/383)
- Fixup failed target names from BEP in bazel 6+ in the bloop mode.
  | [#402](https://github.com/JetBrains/bazel-bsp/pull/402)

## [2.6.1]

### SECURITY 🚨

- Make BEP Server listening on localhost instead of 0.0.0.0.
  | [#369](https://github.com/JetBrains/bazel-bsp/pull/369)

### Fixes 🛠️

- Create BEP server on demand for each Bazel call.
  | [#370](https://github.com/JetBrains/bazel-bsp/pull/370)

## [2.6.0]

### Features 🎉

- Project view file flag for disabling trace log.
  | [#344](https://github.com/JetBrains/bazel-bsp/pull/344)
- Create BEP connection on demands instead of keeping it as a service (reduces memory footprint)
  | [#356](https://github.com/JetBrains/bazel-bsp/pull/356)

### Fixes 🛠️

- Fixed handle bazelisk exec on windows.
  | [#6294219](https://github.com/JetBrains/bazel-bsp/commit/629421998dcd1adb1e9c87973a128b207b9993eb)
- Reduced memory footprint of the application after project import is done
  | [#359](https://github.com/JetBrains/bazel-bsp/pull/359)
- Exit bazel-bsp process when std io streams are closed
  | [#356](https://github.com/JetBrains/bazel-bsp/pull/356)

## [2.5.1] - 08.02.2023

### Changes 🔄
 
- Initialize the hash sets with the number of elements they are expected to hold.
  | [#339](https://github.com/JetBrains/bazel-bsp/pull/339)

### Fixes 🛠️

- Current target jar is excluded from dependencies - aka jump to definition within a module should work now.
  | [#340](https://github.com/JetBrains/bazel-bsp/pull/340)
- Changed separator for classpath to os-agnostic.
  | [#BAZEL-255](https://github.com/JetBrains/bazel-bsp/commit/8b58b265dbdf5888b1d355866b41a7feda5c0e11)

## [2.5.0] - 29.01.2023

### Features 🎉

- Add mainClasses parameters to `JvmEnvironmentItem`.
  | [#309](https://github.com/JetBrains/bazel-bsp/pull/309)

- Exclude bazel-* symlinks; `outputPaths` endpoint implemented.
  | [#322](https://github.com/JetBrains/bazel-bsp/pull/322)

## [2.4.0] - 2.01.2023

### Features 🎉

- Support Bazel 6.0.0
  | [#318](https://github.com/JetBrains/bazel-bsp/pull/318)

## [2.3.1] - 21.12.2022

### Fixes 🛠️

- Correctly handle java files with package names not corresponding to file paths
  | [#312](https://github.com/JetBrains/bazel-bsp/pull/312)
- Correctly determine language type for JVM binary targets.
  | [#306](https://github.com/JetBrains/bazel-bsp/pull/306)

## [2.3.0] - 26.10.2022

### Features 🎉

- Send notifications to client while JUnit5 testing.
  | [#299](https://github.com/JetBrains/bazel-bsp/pull/299)

### Changes 🔄

- Add async and sync output processors.
  | [#288](https://github.com/JetBrains/bazel-bsp/pull/288)
- Pass originId into diagnostics.
  | [#291](https://github.com/JetBrains/bazel-bsp/pull/291)
- Pass originId into server responses.
  | [#289](https://github.com/JetBrains/bazel-bsp/pull/289)

### Fixes 🛠️

- `BuildTargetIdentifier` mapping uses `.uri` instead of `.toString`.
  | [#295](https://github.com/JetBrains/bazel-bsp/pull/295)
- `JVMLanguagePluginParser.calculateJVMSourceRoot` does not throw 
  an exception for package longer than the path.
  | [#294](https://github.com/JetBrains/bazel-bsp/pull/294)
- Fix transitive target failure check in bloop export.
  | [#287](https://github.com/JetBrains/bazel-bsp/pull/287)
- Use direct source dependencies for thrift targets if present.
  | [#298](https://github.com/JetBrains/bazel-bsp/pull/298)
- Postpone bazel info.
  | [#300](https://github.com/JetBrains/bazel-bsp/pull/300)

## [2.2.1] - 09.08.2022

### Fixes 🛠️

- Adding generated jars to source jars list.
  | [#285](https://github.com/JetBrains/bazel-bsp/pull/285)
- Workspace root is always used as a bazel directory.
  | [#284](https://github.com/JetBrains/bazel-bsp/pull/284)
- Kebab case in the installer flags & help typo fix & log typo fix.
  | [#283](https://github.com/JetBrains/bazel-bsp/pull/283)
- Don't use `Label.toString` now that it's no longer overriden.
  | [#282](https://github.com/JetBrains/bazel-bsp/pull/282)

## [2.2.0] - 27.07.2022

### Features 🎉

- Server can be used with bloop (`--use_bloop` flag).
  | [#246](https://github.com/JetBrains/bazel-bsp/pull/246)
- Project view supports `build_manual_targets` - now it is possible to build targets with `manual` tag.
  | [#243](https://github.com/JetBrains/bazel-bsp/pull/243)
- `directories` and `derive_targets_from_directories` sections are now available in project view files.
  | [#247](https://github.com/JetBrains/bazel-bsp/pull/247)
- Dependent targets can be imported as modules based on `import_depth` project view parameter.
  | [#248](https://github.com/JetBrains/bazel-bsp/pull/248)
- BSP can be installed in a separate directory. `-w` flag sets Bazel workspace, `-d` sets the BSP installation root

### Changes 🔄

- Added cache of targets info in incremental syncs.
  | [#273](https://github.com/JetBrains/bazel-bsp/pull/273)
- Dependency sources are now filtered if they belong to other root target
  | [#265](https://github.com/JetBrains/bazel-bsp/pull/265)
- Packages are parsed in order to determine source roots.
  | [#258](https://github.com/JetBrains/bazel-bsp/pull/258)
- BazelPathsResolver caches uris and paths (preformance improvement).
  | [#256](https://github.com/JetBrains/bazel-bsp/pull/256)
- Scala version regex has been fixed.
  | [#249](https://github.com/JetBrains/bazel-bsp/pull/249)
- e2e test for local jdk and remote jdk
  | [#253](https://github.com/JetBrains/bazel-bsp/pull/253)
- Dependency sources are now filtered if they belong to other root target
  | [#265](https://github.com/JetBrains/bazel-bsp/pull/265)
- JvmEnvironmentItem is set using env from target
  | [#260](https://github.com/JetBrains/bazel-bsp/pull/260)
- Server has undergone basic changes leading to the ability to work with CPP language.
  | [#277](https://github.com/JetBrains/bazel-bsp/pull/277)
  
### Fixes 🛠️
- CLI project view generator now supports `--excluded-targets` and `--excluded-directories` instead of excluded `-` prefix.
  | [#267](https://github.com/JetBrains/bazel-bsp/pull/267)

## [2.1.0] - 11.05.2022

### Features 🎉

- Manual tag and `build_manual_targets` support in project view. 
  Using this flag it is possible to build targets marked as manual.
  | [#236](https://github.com/JetBrains/bazel-bsp/pull/236)
- Build flags are reloaded during sync.
  | [#242](https://github.com/JetBrains/bazel-bsp/pull/242)

### Changes 🔄

- Support for detecting jdk through runtime_jdk poperty; infer jdk
  | [#241](https://github.com/JetBrains/bazel-bsp/pull/241)
- New project view parser without default file.
  | [#242](https://github.com/JetBrains/bazel-bsp/pull/242)
- Map label->module is no longer serialized -
  it was doubling size of the file.
  | [#233](https://github.com/JetBrains/bazel-bsp/pull/233)
- Diagnostic message doesn't contain path to file anymore.
  Also, column number is now inferred from the message.
  | [#238](https://github.com/JetBrains/bazel-bsp/pull/238)

## [2.0.0] - 22.04.2022

### Features 🎉

- New installer with project view generator -
  now it is possible to create a project view using installer.
  | [#227](https://github.com/JetBrains/bazel-bsp/pull/227)
- New implementation of diagnostics extraction. It collects
  both warnings and errors, doesn't show irrelevant errors
  in BUILD files, shows line and character position and does
  not run bazel query before each build anymore.
  | [#225](https://github.com/JetBrains/bazel-bsp/pull/225)
- `build_flags` support in project view.
  | [#194](https://github.com/JetBrains/bazel-bsp/pull/194)
- New installer - it is possible to specify installation directory
  and project view file using flags.
  | [#201](https://github.com/JetBrains/bazel-bsp/pull/201)
- Server now keeps the state of the project between runs.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Improved bazel runner. The BSP Client now receives invoked bazel command,
  its output, duration and exit code.
  | [#198](https://github.com/JetBrains/bazel-bsp/pull/198)
- Scala diagnostics work without a fork (technically it worked already for a while).
  | [#170](https://github.com/JetBrains/bazel-bsp/pull/170)

### Fixes 🛠️

- Parsing of project view file fails if the file doesn't exist.
  | [#215](https://github.com/JetBrains/bazel-bsp/pull/215)
- Project view path is mapped to the absolute path in the installer.
  | [#213](https://github.com/JetBrains/bazel-bsp/pull/213)
- Kotlin targets don't break import.
  | [#211](https://github.com/JetBrains/bazel-bsp/pull/211)
- Now sources of thrift dependencies are included as dependencies.
  | [#202](https://github.com/JetBrains/bazel-bsp/pull/202)
- Handle the case when there is no JDK in the project.
  | [#200](https://github.com/JetBrains/bazel-bsp/pull/200)
- Fixed extraction of java version and java home for bazel `5.0.0`.
  | [#165](https://github.com/JetBrains/bazel-bsp/pull/165)
- Log messages are no longer trimmed.
  | [#157](https://github.com/JetBrains/bazel-bsp/pull/157)
- Memoize BazelProcess output so that it doesn't get lost.
  | [#154](https://github.com/JetBrains/bazel-bsp/pull/154)

### Changes 🔄

- `bazel info` call caching mechanism.
  | [#228](https://github.com/JetBrains/bazel-bsp/pull/228)
- Logging level has been set to info and doesn't include events.
  | [#223](https://github.com/JetBrains/bazel-bsp/pull/223)
- Deletion of semantic version class.
  | [#216](https://github.com/JetBrains/bazel-bsp/pull/216)
- Duplicate bazel output lines are not shown in bsp-client.
  | [#209](https://github.com/JetBrains/bazel-bsp/pull/209)
- Project uses bazel `5.1.0`.
  | [#208](https://github.com/JetBrains/bazel-bsp/pull/208)
- Parse bazel query output from stream rather than from all bytes.
  | [#210](https://github.com/JetBrains/bazel-bsp/pull/210)
- JUnit5!
  | [#206](https://github.com/JetBrains/bazel-bsp/pull/206)
- Do not throw on aborted event from BEP. Show warning instead.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Readable `toString` for all `Project` related classes.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Measure and report time of longer operations during sync.
  | [#205](https://github.com/JetBrains/bazel-bsp/issues/205)
- Support for excluded targets in the sync mechanism.
  | [#196](https://github.com/JetBrains/bazel-bsp/pull/196)
- Introduction of installation context.
  | [#184](https://github.com/JetBrains/bazel-bsp/pull/184)
- Project uses (mostly) `io.vavr.Option` and `io.vavr.List`.
  | [#192](https://github.com/JetBrains/bazel-bsp/pull/192)
- Introducing execution context and workspace context.
  | [#172](https://github.com/JetBrains/bazel-bsp/pull/172)
- Changed the structure of the [README](README.md) and other documents and added contribution guide.
  | [#181](https://github.com/JetBrains/bazel-bsp/pull/181)
- Project view parser has more logging, targets section is optional and
  sections are using specific types instead of raw strings.
  | [#166](https://github.com/JetBrains/bazel-bsp/pull/166)
- Improve the `install.sh` script.
  | [#167](https://github.com/JetBrains/bazel-bsp/pull/167)
- Now the project is using the latest bazel version - `5.0.0`.
  Installer skips bazelisk cache binaries during bazel binary discovery mechanism.
  | [#160](https://github.com/JetBrains/bazel-bsp/pull/160)
- Rewrite of all endpoints to rely on single bazel aspect to extract all necessary data.
  It improves performance and correctness as wel as extensibility of the server.
  With this change also c++ support is temporarily dropped.
  | [#147](https://github.com/JetBrains/bazel-bsp/pull/147)

## [1.1.1] - 16.02.2022

### Features 🎉

- New, better project view! Now you can configure installer as well!
  | [#143](https://github.com/JetBrains/bazel-bsp/pull/143)
- Server returns only the relevant output jar rather than 5-6 jars.
  | [#136](https://github.com/JetBrains/bazel-bsp/pull/136)
- Server supports multiple subprojects in one workspace (first step).
  | [#130](https://github.com/JetBrains/bazel-bsp/pull/130)
- `JvmBuildServer` implementation - now tests execution should be working!
  | [#128](https://github.com/JetBrains/bazel-bsp/pull/128)
- Improved heuristics for guessing source roots. It looks directory structures such as `src/java` or `main/java`.
  | [#126](https://github.com/JetBrains/bazel-bsp/pull/126)

### Changes 🔄

- Server filters out non-runtime jars for running apps and tests.
  | [#131](https://github.com/JetBrains/bazel-bsp/pull/131)
- Now server uses Java 11.
  | [#129](https://github.com/JetBrains/bazel-bsp/pull/129)
- Bazel runner allows running a bazel command without positional arguments.
  | [#123](https://github.com/JetBrains/bazel-bsp/pull/123)

### Fixes 🛠️

- Error diagnostics are now also sent for source files, including targets.
  | [#146](https://github.com/JetBrains/bazel-bsp/pull/146)
- Badges in the README work now (and there are even more of them).
  | [#142](https://github.com/JetBrains/bazel-bsp/pull/142)
- Server builds a project during sync in order to fix dependency resolving mechanism.
  | [#137](https://github.com/JetBrains/bazel-bsp/pull/137)
- Server handles aspects properly when multiple projects are in workspace.
  | [#132](https://github.com/JetBrains/bazel-bsp/pull/132)
- Now the project is built using bazel version `3.7.2`, as the rules currently used are no longer supported by bazel.
  | [#141](https://github.com/JetBrains/bazel-bsp/pull/141)

## [1.0.1] - 24.09.2021

### Features 🎉

- [This](CHANGELOG.md) changelog.
  | [#97](https://github.com/JetBrains/bazel-bsp/pull/97)

### Changes 🔄

- Implementation of e2e tests. Have been moved to the `e2e` module and are now based on execution scenarios.
  | [#83](https://github.com/JetBrains/bazel-bsp/pull/83)
- Github actions e2e tests execution - e2e tests are executed in parallel.
  | [#84](https://github.com/JetBrains/bazel-bsp/pull/84)
- Project structure - now it is multi module project.
  | [#87](https://github.com/JetBrains/bazel-bsp/pull/87)
- Github actions unit tests execution - each module has a separate job.
  | [#89](https://github.com/JetBrains/bazel-bsp/pull/89)
- Created [document](docs/dev/BUMPVERSION.md) with release tips.
  | [#91](https://github.com/JetBrains/bazel-bsp/pull/91)

### Removed ✂️

- Old implementation of e2e tests.
  | [#86](https://github.com/JetBrains/bazel-bsp/pull/86)

### Fixes 🛠️

- Warnings generated by the [WORKSPACE file](WORKSPACE).
  | [#81](https://github.com/JetBrains/bazel-bsp/pull/81)
- Semantic versioning parser - now it can parse every valid version.
  | [#93](https://github.com/JetBrains/bazel-bsp/pull/93)
- `exports` attribute propagation to the BSP
  | [#98](https://github.com/JetBrains/bazel-bsp/pull/98)
- Now all `scala_junit_test` based rules (including `scala_specs2_junit_test`) are included in the BSP tests,
  unfortunately without test classes.
  | [#101](https://github.com/JetBrains/bazel-bsp/pull/101)

## [1.0.0] - 23.08.2021

- Everything... 🎉

[Unreleased]: https://github.com/JetBrains/bazel-bsp/compare/3.2.0...HEAD

[3.2.0]: https://github.com/JetBrains/bazel-bsp/compare/3.1.0...3.2.0

[3.1.0]: https://github.com/JetBrains/bazel-bsp/compare/3.0.0...3.1.0

[3.0.0]: https://github.com/JetBrains/bazel-bsp/compare/2.7.2...3.0.0

[2.7.2]: https://github.com/JetBrains/bazel-bsp/compare/2.7.1..2.7.2

[2.7.1]: https://github.com/JetBrains/bazel-bsp/compare/2.7.0..2.7.1

[2.7.0]: https://github.com/JetBrains/bazel-bsp/compare/2.6.1..2.7.0

[2.6.1]: https://github.com/JetBrains/bazel-bsp/compare/2.6.0..2.6.1

[2.6.0]: https://github.com/JetBrains/bazel-bsp/compare/2.5.1..2.6.0

[2.5.1]: https://github.com/JetBrains/bazel-bsp/compare/2.5.0..2.5.1

[2.5.0]: https://github.com/JetBrains/bazel-bsp/compare/2.4.0..2.5.0

[2.4.0]: https://github.com/JetBrains/bazel-bsp/compare/2.3.1...2.4.0

[2.3.1]: https://github.com/JetBrains/bazel-bsp/compare/2.3.0...2.3.1

[2.3.0]: https://github.com/JetBrains/bazel-bsp/compare/2.2.1...2.3.0

[2.2.1]: https://github.com/JetBrains/bazel-bsp/compare/2.2.0...2.2.1

[2.2.0]: https://github.com/JetBrains/bazel-bsp/compare/2.1.0...2.2.0

[2.1.0]: https://github.com/JetBrains/bazel-bsp/compare/2.0.0...2.1.0

[2.0.0]: https://github.com/JetBrains/bazel-bsp/compare/1.1.1...2.0.0

[1.1.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.1...1.1.1

[1.0.1]: https://github.com/JetBrains/bazel-bsp/compare/1.0.0...1.0.1

[1.0.0]: https://github.com/JetBrains/bazel-bsp/releases/tag/1.0.0
