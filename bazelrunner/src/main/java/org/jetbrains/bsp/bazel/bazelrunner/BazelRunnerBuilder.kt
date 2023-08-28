package org.jetbrains.bsp.bazel.bazelrunner

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import com.google.common.collect.ImmutableList
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelQueryKindParameters
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelArgumentsUtils
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.nio.file.Path

open class BazelRunnerBuilder internal constructor(
    private val bazelRunner: BazelRunner,
    private val bazelCommand: String
) {

    private val flags = mutableListOf<String>()
    private val arguments = mutableListOf<String>()

    fun withFlag(bazelFlag: String): BazelRunnerBuilder {
        flags.add(bazelFlag)
        return this
    }

    fun withFlags(bazelFlags: List<String>?): BazelRunnerBuilder {
        flags.addAll(bazelFlags!!)
        return this
    }

    fun withArgument(bazelArgument: String): BazelRunnerBuilder {
        arguments.add(bazelArgument)
        return this
    }

    fun withArguments(bazelArguments: List<String>?): BazelRunnerBuilder {
        arguments.addAll(bazelArguments!!)
        return this
    }

    open fun withTargets(bazelTargets: List<String>): BazelRunnerBuilder {
        val joinedTargets = BazelArgumentsUtils.getJoinedBazelTargets(bazelTargets)
        arguments.add(joinedTargets)
        return this
    }

    open fun withTargets(targetsSpec: TargetsSpec): BazelRunnerBuilder {
        val joinedTargets = BazelArgumentsUtils.joinBazelTargets(targetsSpec.values, targetsSpec.excludedValues)
        arguments.add(joinedTargets)
        return this
    }

    open fun withTargets(
        includedTargets: List<BuildTargetIdentifier>,
        excludedTargets: List<BuildTargetIdentifier>
    ): BazelRunnerBuilder? {
        val joinedTargets = BazelArgumentsUtils.joinBazelTargets(includedTargets, excludedTargets)
        arguments.add(joinedTargets)
        return this
    }

    fun withMnemonic(bazelTargets: List<String>, languageIds: List<String>): BazelRunnerBuilder {
        val argument = BazelArgumentsUtils.getMnemonicWithJoinedTargets(bazelTargets, languageIds)
        arguments.add(argument)
        return this
    }

    fun withKind(bazelParameter: BazelQueryKindParameters): BazelRunnerBuilder {
        return withKinds(ImmutableList.of(bazelParameter))
    }

    fun withKinds(bazelParameters: List<BazelQueryKindParameters>): BazelRunnerBuilder {
        val argument = BazelArgumentsUtils.getQueryKindForPatternsAndExpressions(bazelParameters)
        arguments.add(argument)
        return this
    }

    fun withKindsAndExcept(
        parameters: BazelQueryKindParameters,
        exception: String
    ): BazelRunnerBuilder {
        val argument = BazelArgumentsUtils.getQueryKindForPatternsAndExpressionsWithException(
            listOf(parameters), exception
        )
        arguments.add(argument)
        return this
    }

    fun executeBazelCommand(originId: String? = null): BazelProcess {
        return bazelRunner.runBazelCommand(bazelCommand, flags, arguments, originId)
    }

    fun executeBazelBesCommand(originId: String? = null, buildEventFile: Path): BazelProcess {
        return bazelRunner.runBazelCommandBes(bazelCommand, flags, arguments, originId, buildEventFile.toAbsolutePath())
    }
}
