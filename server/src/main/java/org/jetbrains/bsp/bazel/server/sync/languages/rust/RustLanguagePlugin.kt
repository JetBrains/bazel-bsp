package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.RustWorkspaceResult
import ch.epfl.scala.bsp4j.RustPackage
import ch.epfl.scala.bsp4j.RustRawDependency
import ch.epfl.scala.bsp4j.RustDependency
import ch.epfl.scala.bsp4j.RustDepKindInfo
import ch.epfl.scala.bsp4j.RustTarget
import ch.epfl.scala.bsp4j.RustFeature
import ch.epfl.scala.bsp4j.RustEnvData
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.info.BspTargetInfo.RustCrateLocation as ProtoRustCrateLocation
import org.apache.logging.log4j.LogManager // TODO: remove
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language

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
                },
                crateRoot = targetInfo.rustCrateInfo.crateRoot,
                version = targetInfo.rustCrateInfo.version
            )
        }
    }

    private fun TargetInfo.getRustCrateInfoOrNull(): BspTargetInfo.RustCrateInfo? =
        this.takeIf(TargetInfo::hasRustCrateInfo)?.rustCrateInfo

    override fun applyModuleData(moduleData: RustModule, buildTarget: BuildTarget) {
        // TODO
    }

    data class BazelPackageTargetInfo(val packageName: String, val targetName: String)

    private fun resolvePackage(rustTarget: Module): BazelPackageTargetInfo {
        val label = rustTarget.label.value
        val packageName = label.substringBefore(":")
        val targetName = label.substringAfter(":")
        return BazelPackageTargetInfo(packageName, targetName)
    }

    // We need to merge targets with the same crate root.
    private fun mergeTargets(rustTargets: List<Pair<Module, RustModule>>): Pair<Module, RustModule> {
        if (rustTargets.size == 1) {
            return rustTargets[0]
        }

        // TODO:
        // We need some heuristic here. As we need the proper label, lets take the target with
        //  the most features. It can be changes as a union of features set, but we need
        //  to give it a proper label.

        return rustTargets.maxByOrNull { (_, rustData) -> rustData.crateFeatures.size }!!
    }

    private fun rustPackages(rustBspTargets: List<Module>): List<RustPackage> {
        for (target in rustBspTargets) {
            LOGGER.info("rust target:")
            LOGGER.info(target)

            if (Language.RUST !in target.languages) {
                LOGGER.info("not rust target")
                continue
            }

            val uri = bazelPathsResolver.labelToDirectoryUri(target.label)
            LOGGER.info("uri: $uri")

            val relPath = bazelPathsResolver.workspaceRoot()
            LOGGER.info("relPath: $relPath")

            val packageTargetInfo = resolvePackage(target)
            LOGGER.info("packageTargetInfo: $packageTargetInfo")
        }
        val workspaceRoot = bazelPathsResolver.workspaceRoot()
        val packages = rustBspTargets
            .groupBy { resolvePackage(it).packageName }
            .mapNotNull { (rustPackage, rustTargets) ->
                val rustTargetsWithData: List<Pair<Module, RustModule>> = rustTargets.mapNotNull {
                    if (it.languageData is RustModule) {
                        Pair(it, it.languageData as RustModule)
                    } else {
                        null
                    }
                }
                    .groupBy { (_, rustData) -> rustData.crateRoot }
                    .map { (_, targets) ->
                        mergeTargets(targets)
                    }

                // target have versions, but package no, as there is no such thing as package in Rust in Bazel
                val version = rustTargetsWithData.map { (_, rustData) -> rustData.version }.maxOf { it }
                val isFromWorkspace = rustTargetsWithData.any { (_, rustData) -> rustData.fromWorkspace }
                val origin = if (isFromWorkspace) {
                    "WORKSPACE"
                } else {
                    "DEPENDENCY"
                }
                val edition = rustTargetsWithData.map { (_, rustData) -> rustData.edition }.maxOf { it }
                val source = if (isFromWorkspace) {
                    null
                } else {
                    "registry+https://github.com/rust-lang/crates.io-index"
                }     // let's hope it is
                val targets = rustTargetsWithData.map { (genericData, rustData) ->
                    RustTarget(
                        resolvePackage(genericData).targetName,
                        "$workspaceRoot${rustData.crateRoot}",
                        genericData.tags.first().toString(),    // TODO: not so sure about that
                        rustData.edition,
                        false,                                  // TODO: check it somehow. I even know where to look for it :/  http://bazelbuild.github.io/rules_rust/rust_doc.html
                        rustData.crateFeatures
                    )
                }
                val allFeatures = rustTargetsWithData.flatMap { (_, rustData) ->
                    rustData.crateFeatures
                }
                RustPackage(
                    BuildTargetIdentifier(rustPackage),         // id. TODO: it shouldn't be a BuildTargetIdentifier!!
                    version,
                    origin,
                    edition,
                    source,
                    targets,
                    allFeatures.map { RustFeature(it, listOf()) },
                    allFeatures,
                    null,
                    listOf(
                        RustEnvData("CARGO_MANIFEST_DIF", "$workspaceRoot${rustPackage.drop(1)}"),
                        RustEnvData("CARGO", "cargo"),
                        RustEnvData("CARGO_PKG_VERSION", "0.0.0"),
                        RustEnvData("CARGO_PKG_VERSION_MAJOR", "0"),
                        RustEnvData("CARGO_PKG_VERSION_MINOR", "0"),
                        RustEnvData("CARGO_PKG_VERSION_PATCH", "0"),
                        RustEnvData("CARGO_PKG_VERSION_PRE", ""),
                        RustEnvData("CARGO_PKG_AUTHORS", ""),
                        RustEnvData("CARGO_PKG_NAME", rustPackage),
                        RustEnvData("CARGO_PKG_DESCRIPTION", ""),
                        RustEnvData("CARGO_PKG_REPOSITORY", ""),
                        RustEnvData("CARGO_PKG_LICENSE", ""),
                        RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                        RustEnvData("CARGO_CRATE_NAME", rustPackage),
                    ),
                    null,
                    null
                )
            }
        LOGGER.info("packages:")
        for (rustPackage in packages) {
            LOGGER.info(rustPackage)
        }

        return packages
    }

    // We need to resolve all dependencies are provide a list of new bazel targets (deps) to be transformed into packages.
    private fun rustDependencies(rustBspTargets: List<Module>): Pair<List<BuildTargetIdentifier>, List<RustDependency>> {

        val rustDependencies = rustBspTargets.flatMap {
            it.directDependencies.map { label -> label.value }
        }

        LOGGER.info("Direct dependencies")
        LOGGER.info(rustDependencies)

        return Pair(listOf(), listOf())
    }

    fun toRustWorkspaceResult(modules: List<Module>): RustWorkspaceResult {

        val packages = rustPackages(modules)
        val dependencies = rustDependencies(modules)

        LOGGER.info("=================================================================================")

        for (module in modules) {
            LOGGER.info("module info: ")
            LOGGER.info(module)
        }

        val samplePath = when (try {
            System.getenv("USER")
        } catch (_: Exception) {
            "matt"
        }) {
            "matt" -> "/home/matt/uw/zpp/rust-bazel-bsp-sample"
            "tudny" -> "/home/tudny/Documents/UW/MIMUW/ZPP/rust-bazel-bsp-sample"
            "mikolaj" -> "/home/mikolaj/Desktop/zpp/rust-bazel-bsp-sample"
            else -> "/Users/przemek/ZPP/rust-bazel-bsp-sample"
        }

        val itertoolsPath = "file:///$samplePath/bazel-rust-bazel-bsp-sample/external/crate_index__itertools-0.10.5";
        val eitherPath = "file:///$samplePath/bazel-rust-bazel-bsp-sample/external/crate_index__either-1.8.0";

        LOGGER.info("$itertoolsPath")
        LOGGER.info("$eitherPath")

        val mockPackages = listOf<RustPackage>(
            RustPackage(
                BuildTargetIdentifier("//hello_world"),
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
                    ),
                    RustTarget(
                        "hello_world_with_feature",
                        "file://$samplePath/hello_world/src/main.rs",
                        "application",
                        "2018",
                        false,
                        listOf<String>(
                            "elitarny_mimuw"
                        )
                    )
                ),
                listOf<RustFeature>(
                    RustFeature(
                        "elitarny_mimuw",
                        listOf<String>()
                    )
                ),
                listOf<String>(
                    "elitarny_mimuw"
                ),
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
                BuildTargetIdentifier("//hello_lib"),
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
                BuildTargetIdentifier("@crate_index__either-1.8.0//"),
                "1.8.0",
                "DEPENDENCY",
                "2018",
                "registry+https://github.com/rust-lang/crates.io-index",
                listOf<RustTarget>(
                    RustTarget(
                        "either",
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
                    RustEnvData(
                        "CARGO_PKG_DESCRIPTION",
                        "The enum `Either` with variants `Left` and `Right` is a general purpose sum type with two cases.\n"
                    ),
                    RustEnvData("CARGO_PKG_REPOSITORY", "https://github.com/bluss/either"),
                    RustEnvData("CARGO_PKG_LICENSE", "MIT/Apache-2.0"),
                    RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                    RustEnvData("CARGO_CRATE_NAME", "either"),
                ),
                null,
                null,
            ),

            RustPackage(
                BuildTargetIdentifier("@crate_index__itertools-0.10.5//"),
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
                    )
                    // Those targets are not seen by Bazel anyway.
                    //,
                    // RustTarget(
                    //         "iris",
                    //         "$itertoolsPath/examples/iris.rs",
                    //         "example",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "flatten_ok",
                    //         "$itertoolsPath/tests/flatten_ok.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "macros_hygiene",
                    //         "$itertoolsPath/tests/macros_hygiene.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "adaptors_no_collect",
                    //         "$itertoolsPath/tests/adaptors_no_collect.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "peeking_take_while",
                    //         "$itertoolsPath/tests/peeking_take_while.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "tuples",
                    //         "$itertoolsPath/tests/tuples.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "test_std",
                    //         "$itertoolsPath/tests/test_std.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "test_core",
                    //         "$itertoolsPath/tests/test_core.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "merge_join",
                    //         "$itertoolsPath/tests/merge_join.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "quick",
                    //         "$itertoolsPath/tests/quick.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "specializations",
                    //         "$itertoolsPath/tests/specializations.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "zip",
                    //         "$itertoolsPath/tests/zip.rs",
                    //         "test",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "tuple_combinations",
                    //         "$itertoolsPath/benches/tuple_combinations.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "tuples",
                    //         "$itertoolsPath/benches/tuples.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "fold_specialization",
                    //         "$itertoolsPath/benches/fold_specialization.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "combinations_with_replacement",
                    //         "$itertoolsPath/benches/combinations_with_replacement.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "tree_fold1",
                    //         "$itertoolsPath/benches/tree_fold1.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "bench1",
                    //         "$itertoolsPath/benches/bench1.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "combinations",
                    //         "$itertoolsPath/benches/combinations.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // ),
                    // RustTarget(
                    //         "powerset",
                    //         "$itertoolsPath/benches/powerset.rs",
                    //         "bench",
                    //         "2018",
                    //         false,
                    //         listOf<String>()
                    // )
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
                    RustEnvData(
                        "CARGO_PKG_DESCRIPTION",
                        "Extra iterator adaptors, iterator methods, free functions, and macros."
                    ),
                    RustEnvData("CARGO_PKG_REPOSITORY", "https://github.com/rust-itertools/itertools"),
                    RustEnvData("CARGO_PKG_LICENSE", "MIT/Apache-2.0"),
                    RustEnvData("CARGO_PKG_LICENSE_FILE", ""),
                    RustEnvData("CARGO_CRATE_NAME", "itertools"),
                ),
                null,
                null
            )
        )

        return RustWorkspaceResult(
            packages,

            // TODO: mappings in BSP are different than those in cargo metadata
            listOf<RustRawDependency>(
                RustRawDependency(
                    "//hello_world",
                    "//hello_lib:hello_lib",
                    null,
                    null,
                    null,
                    false,
                    true,
                    listOf<String>()
                ),
                RustRawDependency(
                    "//hello_lib",
                    "@crate_index__itertools-0.10.5//itertools",
                    null,
                    null,
                    null,
                    false,
                    true,
                    listOf<String>()
                ),
                RustRawDependency(
                    "@crate_index__itertools-0.10.5//",
                    "@crate_index__either-1.8.0//either",
                    null,
                    null,
                    null,
                    false,
                    false,
                    listOf<String>()
                ),
                // Those are dependencies of `@crate_index__itertools-0.10.5//`, but there are not listed in Bazel, so we cannot send them.
                //  They are not used anyway as far as we understand.
                // RustRawDependency(
                //         "@crate_index__itertools-0.10.5//",
                //         "@crate_index__criterion-???//criterion",
                //         null,
                //         "dev",
                //         null,
                //         false,
                //         true,
                //         listOf<String>()
                // ),
                // RustRawDependency(
                //         "@crate_index__itertools-0.10.5//",
                //         "@crate_index__paste-???//paste",
                //         null,
                //         "dev",
                //         null,
                //         false,
                //         true,
                //         listOf<String>()
                // ),
                // RustRawDependency(
                //         "@crate_index__itertools-0.10.5//",
                //         "@crate_index__permutohedron-???//permutohedron",
                //         null,
                //         "dev",
                //         null,
                //         false,
                //         true,
                //         listOf<String>()
                // ),
                // RustRawDependency(
                //         "@crate_index__itertools-0.10.5//",
                //         "@crate_index__quickcheck-???//quickcheck",
                //         null,
                //         "dev",
                //         null,
                //         false,
                //         false,
                //         listOf<String>()
                // ),
                // RustRawDependency(
                //         "@crate_index__itertools-0.10.5//",
                //         "@crate_index__rand-???//rand",
                //         null,
                //         "dev",
                //         null,
                //         false,
                //         true,
                //         listOf<String>()
                // )
            ),

            listOf<RustDependency>(
                RustDependency(
                    "//hello_world",
                    "//hello_lib",
                    "hello_lib",
                    listOf<RustDepKindInfo>(
                        RustDepKindInfo("normal", null)
                    )
                ),
                RustDependency(
                    "//hello_lib",
                    "@crate_index__itertools-0.10.5//",
                    "itertools",
                    listOf<RustDepKindInfo>(
                        RustDepKindInfo("normal", null)
                    )
                ),
                RustDependency(
                    "@crate_index__itertools-0.10.5//",
                    "@crate_index__either-1.8.0//",
                    "either",
                    listOf<RustDepKindInfo>(
                        RustDepKindInfo("normal", null)
                    )
                ),
                // No dependencies for -- "@crate_index__either-1.8.0//"
            )
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(RustLanguagePlugin::class.java)
    }
}
