package org.jetbrains.bsp.bazel.server.bsp.managers

import ch.epfl.scala.bsp4j.BuildClient
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.server.bep.BepServer
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.nio.file.Path
import java.util.Optional

class BazelBspCompilationManager(
    private val bazelRunner: BazelRunner,
    private val hasAnyProblems: Map<String, Set<TextDocumentIdentifier>>,
) {
    var client: BuildClient? = null
    var workspaceRoot: Path? = null
    fun buildTargetsWithBep(
        cancelChecker: CancelChecker, targetSpecs: TargetsSpec, originId: String,
    ): BepBuildResult {
        return buildTargetsWithBep(cancelChecker, targetSpecs, emptyList(), originId)
    }

    fun buildTargetsWithBep(
        cancelChecker: CancelChecker,
        targetSpecs: TargetsSpec,
        extraFlags: List<String>,
        originId: String?,
    ): BepBuildResult {
        val bepServer = BepServer.newBepServer(client, workspaceRoot, hasAnyProblems, Optional.ofNullable(originId))
        val bepReader = BepReader(bepServer)
        return try {
            bepReader.start()
            val result = bazelRunner
                .commandBuilder()
                .build()
                .withFlags(extraFlags)
                .withTargets(targetSpecs)
                // Setting `CARGO_BAZEL_REPIN=1` updates `cargo_lockfile`
                // (`Cargo.lock` file) based on dependencies specified in `manifest`
                // (`Cargo.toml` file) and syncs `lockfile` (`Cargo.bazel.lock` file) with `cargo_lockfile`.
                // Ensures that both Bazel and Cargo are using the same versions of dependencies.
                // Mentioned `cargo_lockfile`, `lockfile` and `manifest` are defined in
                // `crates_repository` from `rules_rust`,
                // see: https://bazelbuild.github.io/rules_rust/crate_universe.html#crates_repository.
                // In our server used only with `bazel build` command.
                .withEnvironment(Pair("CARGO_BAZEL_REPIN", "1"))
                .executeBazelBesCommand(originId, bepReader.eventFile.toPath().toAbsolutePath())
                .waitAndGetResult(cancelChecker, true)
            bepReader.finishBuild()
            bepReader.await()
            BepBuildResult(result, bepServer.bepOutput)
        } finally {
            bepReader.finishBuild()
        }
    }

}
