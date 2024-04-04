package org.jetbrains.bsp.bazel.base

import ch.epfl.scala.bsp4j.SourcesParams
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.jetbrains.bsp.JoinedBuildServer
import org.jetbrains.bsp.testkit.client.BasicTestClient
import org.jetbrains.bsp.testkit.client.MockClient
import kotlin.time.Duration.Companion.minutes

typealias BazelTestClient = BasicTestClient<JoinedBuildServer, MockClient>

// This is an example of a test function containing client-side assertions.
fun BazelTestClient.test() {
  test(timeout = 1.minutes) { session, _ ->
    val getWorkspaceTargets = session.server.workspaceBuildTargets().await()
    val targets = getWorkspaceTargets.targets
    val targetIds = targets.map { it.id }
    val sources = session.server.buildTargetSources(SourcesParams(targetIds)).await()
    // do something with sources
    assert(sources.items.isNotEmpty())
  }
}
