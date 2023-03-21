package org.jetbrains.bsp.bazel.server.sync.languages.rust

import ch.epfl.scala.bsp4j.BuildTarget
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
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
                    crateFeatures = targetInfo.rustCrateInfo.getCrateFeaturesList(),
                    dependencies = targetInfo.rustCrateInfo.getDependenciesList().mapNotNull {
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
}
