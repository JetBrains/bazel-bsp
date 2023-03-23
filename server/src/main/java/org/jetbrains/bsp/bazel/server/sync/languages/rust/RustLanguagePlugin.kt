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

    fun toRustWorkspaceResult(module: Module): RustWorkspaceResult {
        val samplePath = when (try { System.getenv("USER") } catch (_: Exception) { "matt" }) {
            "matt" -> "/home/matt/uw/zpp/rust-bazel-bsp-sample"
            "tudny" -> "/home/tudny/Documents/UW/MIMUW/ZPP/rust-bazel-bsp-sample"
            "mikolaj" -> "/home/mikolaj/Desktop/zpp/rust-bazel-bsp-sample"
            else -> "/home/matt/uw/zpp/rust-bazel-bsp-sample" // dla Pana marudy
        }

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
                        )
                ),

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
}
