package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo

// TODO maybe remove
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
        } catch (e: MalformedJsonException){
            // sometimes Bazel returns two values to a query, which are not parseable, the real query should be oneline
            return if (cqueryResult.stdoutLines.size > 1) Gson().fromJson(cqueryResult.stdoutLines[0], JvmClasspath::class.java)
            else JvmClasspath(emptyList(), emptyList())
        }
    }

    data class JvmClasspath (
            val runtime_classpath: List<String>,
            val compile_classpath: List<String>
    )

}