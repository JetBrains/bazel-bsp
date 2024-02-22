package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo

object ClasspathQuery {
    fun classPathQuery(target: BuildTargetIdentifier, cancelChecker: CancelChecker, bspInfo: BspInfo, bazelRunner: BazelRunner): JvmClasspath {
        val queryFile = bspInfo.bazelBspDir().resolve("aspects/runtime_classpath_query.bzl")
        val cqueryResult = bazelRunner.commandBuilder().cquery()
                .withTargets(listOf(target.uri))
                .withFlags(listOf("--starlark:file=$queryFile", "--output=starlark"))
                .executeBazelCommand(parseProcessOutput = false)
                .waitAndGetResult(cancelChecker, ensureAllOutputRead = true)
        if (cqueryResult.isNotSuccess) throw RuntimeException("Could not query target '${target.uri}' for runtime classpath")
        try {
            val classpaths = Gson().fromJson(cqueryResult.stdout, JvmClasspath::class.java)
            return classpaths
        } catch (e: JsonSyntaxException){
            // sometimes Bazel returns two values to a query when multiple configurations apply to a target
            return if (cqueryResult.stdoutLines.size > 1) {
                val allOpts = cqueryResult.stdoutLines.map { Gson().fromJson(it, JvmClasspath::class.java) }
                allOpts.maxByOrNull { it.runtime_classpath.size + it.compile_classpath.size }!!
            }
            else throw e
        }
    }

    data class JvmClasspath (
            val runtime_classpath: List<String>,
            val compile_classpath: List<String>
    )

}