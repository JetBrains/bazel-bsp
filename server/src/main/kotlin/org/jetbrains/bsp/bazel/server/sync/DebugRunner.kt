package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.RunResult
import ch.epfl.scala.bsp4j.StatusCode
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Module

class DebugRunner(
    private val bazelRunner: BazelRunner,
    private val errorMessageSender: (message: String, originId: String) -> Unit,
) {
    fun runWithDebug(cancelChecker: CancelChecker, params: RunWithDebugParams, moduleToRun: Module): RunResult {
        val uri = BspMappings.toBspUri(moduleToRun)
        val requestedDebugType = DebugType.fromDebugData(params.debug)
        val debugArguments = generateRunArguments(requestedDebugType)
        val requestIsValid = verifyDebugRequest(requestedDebugType, moduleToRun, params.originId)
        return if (requestIsValid) {
            runBazel(cancelChecker, params, uri, debugArguments)
        } else {
            RunResult(StatusCode.ERROR).apply { this.originId = params.originId }
        }
    }

    private fun generateRunArguments(debugType: DebugType?): List<String> =
            when (debugType) {
                is DebugType.JDWP -> listOf(jdwpArgument(debugType.port))
                else -> emptyList()
            }

    /**
     * @return `true` if the run request is a valid debug request, or is not a debug request at all
     */
    private fun verifyDebugRequest (
            debugType: DebugType?,
            moduleToRun: Module,
            originId: String,
    ): Boolean =
            when (debugType) {
                null -> true  // not a debug request, nothing to check
                is DebugType.JDWP -> {
                    if (!moduleToRun.isJavaOrKotlin()) {
                        errorMessageSender("JDWP debugging is only available for Java and Kotlin targets", originId)
                        false
                    } else true
                }
                is DebugType.UNKNOWN -> {
                    errorMessageSender("\"${debugType.name}\" is not a supported debugging type", originId)
                    false
                }
            }

    // TODO [#BAZEL-721] - quite a naive predicate, but otherwise we'll need to have rule type info in Module instance
    private fun Module.isJavaOrKotlin() = languages.contains(Language.JAVA) || languages.contains(Language.KOTLIN)

    /**
     * If `debugArguments` is empty, run task will be executed normally without any debugging options
     */
    private fun runBazel(
            cancelChecker: CancelChecker,
            params: RunWithDebugParams,
            bspUri: String,
            debugArguments: List<String>,
    ): RunResult {
        val bazelProcessResult =
                bazelRunner.commandBuilder()
                        .run()
                        .withArguments(debugArguments)
                        .withArgument(bspUri)
                        .withArguments(params.runParams.arguments)
                        .withFlag(BazelFlag.color(true))
                        .executeBazelCommand(params.originId)
                        .waitAndGetResult(cancelChecker)
        return RunResult(bazelProcessResult.statusCode).apply { originId = params.originId }
    }

    private fun jdwpArgument(port: Int): String =
            // all used options are defined in https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/conninv.html#Invocation
            "--jvmopt=\"-agentlib:jdwp=" +
                    "transport=dt_socket," +
                    "server=n," +
                    "suspend=y," +
                    "address=localhost:$port," +
                    "\""
}

private sealed interface DebugType {
    data class UNKNOWN(val name: String) : DebugType  // debug type unknown
    data class JDWP(val port: Int) : DebugType  // used for Java and Kotlin

    companion object {
        fun fromDebugData(params: RemoteDebugData?): DebugType? =
                when (params?.debugType?.lowercase()) {
                    null -> null
                    "jdwp" -> JDWP(params.port)
                    else -> UNKNOWN(params.debugType)
                }
    }
}
