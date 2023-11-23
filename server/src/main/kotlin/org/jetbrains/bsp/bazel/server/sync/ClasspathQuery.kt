package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import com.google.gson.Gson
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
        val classpaths = Gson().fromJson(cqueryResult.stdout, JvmClasspath::class.java)
        return classpaths
    }

    data class JvmClasspath (
            val runtime_classpath: List<String>,
            val compile_classpath: List<String>
    )

}