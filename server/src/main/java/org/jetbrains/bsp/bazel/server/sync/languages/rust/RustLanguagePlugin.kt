package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustDependency
// import ch.epfl.scala.bsp4j.RustRawDependencyMapper
// import ch.epfl.scala.bsp4j.RustDependencyMapper
import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustFeature
import ch.epfl.scala.bsp4j.RustCfgOptions
import ch.epfl.scala.bsp4j.RustEnvData
import ch.epfl.scala.bsp4j.RustKeyValueMapper
import ch.epfl.scala.bsp4j.RustProcMacroArtifact
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.info.BspTargetInfo.RustCrateLocation as ProtoRustCrateLocation
import org.apache.logging.log4j.LogManager

class RustLanguagePlugin(private val bazelPathsResolver: BazelPathsResolver) : LanguagePlugin<RustModule>() {
    override fun resolveModule(targetInfo: TargetInfo): RustModule? {
        return targetInfo.getRustCrateInfoOrNull()?.run {
            val location = if (targetInfo.rustCrateInfo.location == ProtoRustCrateLocation.WORKSPACE_DIR) {
                RustCrateLocation.WORKSPACE_DIR
            } else {
                RustCrateLocation.EXEC_ROOT
            }

            RustModule(
                    crateId = targetInfo.rustCrateInfo.crateId,
                    location = location,
                    fromWorkspace = targetInfo.rustCrateInfo.fromWorkspace,
                    name = targetInfo.rustCrateInfo.name,
                    kind = targetInfo.rustCrateInfo.kind,
                    edition = targetInfo.rustCrateInfo.edition,
                    crateFeatures = targetInfo.rustCrateInfo.crateFeaturesList,
                    dependencies = targetInfo.rustCrateInfo.dependenciesList.mapNotNull {
                        RustDependency(
                                crateId = it.crateId,
                                rename = it.rename,
                        )
                    })
        }
    }

    private fun TargetInfo.getRustCrateInfoOrNull(): BspTargetInfo.RustCrateInfo? =
            this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {
        // TODO
    }

    fun toRustWorkspaceResult(modules: List<Module>): RustWorkspaceResult {
        val samplePath = when (try { System.getenv("USER") } catch (_: Exception) { "matt" }) {
            "matt" -> "/home/matt/uw/zpp/rust-bazel-bsp-sample"
            "tudny" -> "/home/tudny/Documents/UW/MIMUW/ZPP/rust-bazel-bsp-sample"
            "mikolaj" -> "/home/mikolaj/Desktop/zpp/rust-bazel-bsp-sample"
            else -> "/Users/przemek/ZPP/rust-bazel-bsp-sample"
        }

        val itertoolsPath = "file:///$samplePath/bazel-rust-bazel-bsp-sample/external/crate_index__itertools-0.10.5";
        val eitherPath = "file:///$samplePath/bazel-rust-bazel-bsp-sample/external/crate_index__either-1.8.0";

        LOGGER.info("$itertoolsPath")
        LOGGER.info("$eitherPath")

        return RustWorkspaceResult(
                listOf<RustPackage>(
                        RustPackage(
                                BuildTargetIdentifier("//hello_world:hello_world"),
                                "0.1.0",
                                "WORKSPACE",
                                "2018",
                                null,
                                listOf<RustTarget>(
                                        RustTarget(
                                                "hello_world",
                                                "file://$samplePath/hello_world/src/main.rs",
                                                "application",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        )
                                ),
                                listOf<RustFeature>(),
                                listOf<String>(),
                                null,
                                listOf<RustEnvData>(
                                        RustEnvData("CARGO_MANIFEST_DIF", "$samplePath/hello-world"),
                                        RustEnvData("CARGO", "cargo"),
                                        RustEnvData("CARGO_PKG_VERSION", "0.1.0"),
                                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_MINOR", "1"),
                                        RustEnvData("CARGO_PKG_VERSION_PATCH", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                                        RustEnvData("CARGO_PKG_AUTHORS", ""),
                                        RustEnvData("CARGO_PKG_NAME", "hello_world"),
                                        RustEnvData("CARGO_PKG_DESCRIPTION", ""),
                                        RustEnvData("CARGO_PKG_REPOSITORY", ""),
                                        RustEnvData("CARGO_PKG_LICENSE", ""),
                                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                                        RustEnvData("CARGO_CRATE_NAME", "hello_world"),
                                ),
                                null,
                                null,
                        ),

                        RustPackage(
                                BuildTargetIdentifier("//hello_lib:hello_lib"),
                                "0.1.0",
                                "WORKSPACE",
                                "2018",
                                null,
                                listOf<RustTarget>(
                                        RustTarget(
                                                "hello_lib",
                                                "file://$samplePath/hello_lib/src/lib.rs",
                                                "library",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        )
                                ),
                                listOf<RustFeature>(),
                                listOf<String>(),
                                null,
                                listOf<RustEnvData>(
                                        RustEnvData("CARGO_MANIFEST_DIF", "$samplePath/hello_lib"),
                                        RustEnvData("CARGO", "cargo"),
                                        RustEnvData("CARGO_PKG_VERSION", "0.1.0"),
                                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_MINOR", "1"),
                                        RustEnvData("CARGO_PKG_VERSION_PATCH", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                                        RustEnvData("CARGO_PKG_AUTHORS", ""),
                                        RustEnvData("CARGO_PKG_NAME", "hello_lib"),
                                        RustEnvData("CARGO_PKG_DESCRIPTION", ""),
                                        RustEnvData("CARGO_PKG_REPOSITORY", ""),
                                        RustEnvData("CARGO_PKG_LICENSE", ""),
                                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                                        RustEnvData("CARGO_CRATE_NAME", "hello_lib"),
                                ),
                                null,
                                null,
                        ),

                        RustPackage(
                                BuildTargetIdentifier("@crate_index//:either"),
                                "1.8.0",
                                "DEPENDENCY",
                                "2018",
                                "registry+https://github.com/rust-lang/crates.io-index",
                                listOf<RustTarget>(
                                        RustTarget(
                                                "hello_lib",
                                                "$eitherPath/src/lib.rs",
                                                "library",
                                                "2018",
                                                true,
                                                listOf<String>()
                                        )
                                ),
                                listOf<RustFeature>(
                                        RustFeature(
                                                "default",
                                                listOf<String>(
                                                        "use_std"
                                                )
                                        ),
                                        RustFeature(
                                                "serde",
                                                listOf<String>(
                                                        "dep:serde"
                                                )
                                        ),
                                        RustFeature(
                                                "use_std",
                                                listOf<String>()
                                        )
                                ),
                                listOf<String>(
                                        "use_std"
                                ),
                                null,
                                listOf<RustEnvData>(
                                        RustEnvData("CARGO_MANIFEST_DIF", "$eitherPath"),
                                        RustEnvData("CARGO", "cargo"),
                                        RustEnvData("CARGO_PKG_VERSION", "1.8.0"),
                                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "1"),
                                        RustEnvData("CARGO_PKG_VERSION_MINOR", "8"),
                                        RustEnvData("CARGO_PKG_VERSION_PATCH", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                                        RustEnvData("CARGO_PKG_AUTHORS", "bluss"),
                                        RustEnvData("CARGO_PKG_NAME", "either"),
                                        RustEnvData("CARGO_PKG_DESCRIPTION", "The enum `Either` with variants `Left` and `Right` is a general purpose sum type with two cases.\n"),
                                        RustEnvData("CARGO_PKG_REPOSITORY", "https://github.com/bluss/either"),
                                        RustEnvData("CARGO_PKG_LICENSE", "MIT/Apache-2.0"),
                                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                                        RustEnvData("CARGO_CRATE_NAME", "either"),
                                ),
                                null,
                                null,
                        ),

                        RustPackage(
                                BuildTargetIdentifier("@crate_index//:itertools"),
                                "0.10.5",
                                "DEPENDENCY",
                                "2018",
                                "registry+https://github.com/rust-lang/crates.io-index",
                                listOf<RustTarget>(
                                        RustTarget(
                                                "itertools",
                                                "$itertoolsPath/src/lib.rs",
                                                "library",
                                                "2018",
                                                true,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "iris",
                                                "$itertoolsPath/examples/iris.rs",
                                                "example",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "flatten_ok",
                                                "$itertoolsPath/tests/flatten_ok.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "macros_hygiene",
                                                "$itertoolsPath/tests/macros_hygiene.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "adaptors_no_collect",
                                                "$itertoolsPath/tests/adaptors_no_collect.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "peeking_take_while",
                                                "$itertoolsPath/tests/peeking_take_while.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "tuples",
                                                "$itertoolsPath/tests/tuples.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "test_std",
                                                "$itertoolsPath/tests/test_std.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "test_core",
                                                "$itertoolsPath/tests/test_core.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "merge_join",
                                                "$itertoolsPath/tests/merge_join.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "quick",
                                                "$itertoolsPath/tests/quick.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "specializations",
                                                "$itertoolsPath/tests/specializations.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "zip",
                                                "$itertoolsPath/tests/zip.rs",
                                                "test",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "tuple_combinations",
                                                "$itertoolsPath/benches/tuple_combinations.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "tuples",
                                                "$itertoolsPath/benches/tuples.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "fold_specialization",
                                                "$itertoolsPath/benches/fold_specialization.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "combinations_with_replacement",
                                                "$itertoolsPath/benches/combinations_with_replacement.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "tree_fold1",
                                                "$itertoolsPath/benches/tree_fold1.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "bench1",
                                                "$itertoolsPath/benches/bench1.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "combinations",
                                                "$itertoolsPath/benches/combinations.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        ),
                                        RustTarget(
                                                "powerset",
                                                "$itertoolsPath/benches/powerset.rs",
                                                "bench",
                                                "2018",
                                                false,
                                                listOf<String>()
                                        )
                                ),
                                listOf<RustFeature>(
                                        RustFeature(
                                                "default",
                                                listOf<String>(
                                                        "use_std"
                                                )
                                        ),
                                        RustFeature(
                                                "use_alloc",
                                                listOf<String>()
                                        ),
                                        RustFeature(
                                                "use_std",
                                                listOf<String>(
                                                        "use_alloc",
                                                        "either/use_std"
                                                )
                                        )
                                ),
                                listOf<String>(
                                        "default",
                                        "use_alloc",
                                        "either/use_std"
                                ),
                                null,
                                listOf<RustEnvData>(
                                        RustEnvData("CARGO_MANIFEST_DIF", "$itertoolsPath"),
                                        RustEnvData("CARGO", "cargo"),
                                        RustEnvData("CARGO_PKG_VERSION", "0.10.5"),
                                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "0"),
                                        RustEnvData("CARGO_PKG_VERSION_MINOR", "10"),
                                        RustEnvData("CARGO_PKG_VERSION_PATCH", "5"),
                                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                                        RustEnvData("CARGO_PKG_AUTHORS", "bluss"),
                                        RustEnvData("CARGO_PKG_NAME", "itertools"),
                                        RustEnvData("CARGO_PKG_DESCRIPTION", "Extra iterator adaptors, iterator methods, free functions, and macros."),
                                        RustEnvData("CARGO_PKG_REPOSITORY", "https://github.com/rust-itertools/itertools"),
                                        RustEnvData("CARGO_PKG_LICENSE", "MIT/Apache-2.0"),
                                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                                        RustEnvData("CARGO_CRATE_NAME", "itertools"),
                                ),
                                null,
                                null
                        )
                ),

                // TODO: mappings in BSP are different than those in cargo metadata
                // listOf<RustRawDependencyMapper>(
                //         RustRawDependencyMapper(
                //                 "//hello_world:hello_world",
                //                 listOf<RustRawDependency>(
                //                         RustRawDependency(
                //                                 "//hello_world:hello_world",
                //                                 "hello_lib",
                //                                 null,
                //                                 null,
                //                                 null,
                //                                 false,
                //                                 true,
                //                                 listOf<String>()
                //                         )
                //                 )
                //         )
                // ),

                // listOf<RustDependencyMapper>(
                //         RustDependencyMapper(
                //                 "//hello_world:hello_world",
                //                 listOf<RustDependency>(
                //                         RustDependency(
                //                                 "//hello_world:hello_world",
                //                                 "//hello_lib:hello_lib",
                //                                 null,
                //                                 listOf<RustDepKindInfo>(
                //                                         RustDepKindInfo("normal", null)
                //                                 )
                //                         )
                //                 )
                //         )
                // )

                listOf<RustRawDependency>(
                        RustRawDependency(
                                "//hello_world:hello_world",
                                "hello_lib",
                                null,
                                null,
                                null,
                                false,
                                true,
                                listOf<String>()
                        )
                ),
                listOf<RustDependency>(
                        RustDependency(
                                "//hello_world:hello_world",
                                "//hello_lib:hello_lib",
                                null,
                                listOf<RustDepKindInfo>(
                                        RustDepKindInfo("normal", null)
                                )
                        )
                )

        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(RustLanguagePlugin::class.java)
    }
}
