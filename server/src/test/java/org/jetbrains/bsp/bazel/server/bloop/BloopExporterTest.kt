package org.jetbrains.bsp.bazel.server.bloop

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.server.bloop.BloopExporter.BazelExportFailedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class BloopExporterTest {
    @Test
    fun export_project_with_failed_direct_target_succeeds() {
        val projectTargets = setOf(BuildTargetIdentifier("a"), BuildTargetIdentifier("b"))
        val failedTargets = setOf(BuildTargetIdentifier("a"))

        try {
            BloopExporter.validateNoFailedExternalTargets(projectTargets, failedTargets)
        } catch (ex: BazelExportFailedException) {
            fail(ex)
        }
    }

    @Test
    fun export_project_with_failed_transitive_target_fails() {
        val projectTargets = setOf(BuildTargetIdentifier("a"), BuildTargetIdentifier("b"))
        val failedTargets = setOf(BuildTargetIdentifier("c"))

        try {
            BloopExporter.validateNoFailedExternalTargets(projectTargets, failedTargets)
        } catch (ex: BazelExportFailedException) {
            return
        }
        fail("a BazelExportFailedException was expected but none was thrown")
    }
}
