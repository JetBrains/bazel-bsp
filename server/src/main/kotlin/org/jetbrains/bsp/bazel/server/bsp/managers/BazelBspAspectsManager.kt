package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.aspect
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.buildManualTests
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.color
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.curses
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.keepGoing
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.outputGroups
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.repositoryOverride
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec

data class BazelBspAspectsManagerResult(val bepOutput: BepOutput, val isFailure: Boolean)

class BazelBspAspectsManager(
    private val bazelBspCompilationManager: BazelBspCompilationManager,
    private val aspectsResolver: InternalAspectsResolver,
    private val bazelBspEnvironmentManager: BazelBspEnvironmentManager,
) {
    fun fetchFilesFromOutputGroups(
        cancelChecker: CancelChecker,
        targetSpecs: TargetsSpec,
        aspect: String,
        outputGroups: List<String>,
    ): BazelBspAspectsManagerResult {
        bazelBspEnvironmentManager.generateLanguageExtensions(cancelChecker)

        return if (targetSpecs.values.isEmpty()) BazelBspAspectsManagerResult(BepOutput(), isFailure = false)
        else bazelBspCompilationManager
            .buildTargetsWithBep(
                cancelChecker,
                targetSpecs,
                listOf(
                    repositoryOverride(
                        Constants.ASPECT_REPOSITORY, aspectsResolver.bazelBspRoot
                    ),
                    aspect(aspectsResolver.resolveLabel(aspect)),
                    outputGroups(outputGroups),
                    keepGoing(),
                    color(true),
                    buildManualTests(),
                    curses(false)
                ),
                null
            ).let {
                BazelBspAspectsManagerResult(it.bepOutput, it.processResult.isNotSuccess)
            }
    }
}
