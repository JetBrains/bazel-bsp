package org.jetbrains.bsp.bazel.bazelrunner

class BazelRunnerCommandBuilder internal constructor(private val bazelRunner: BazelRunner) {
  fun aquery() = BazelRunnerBuilder(bazelRunner, "aquery")
  fun clean()  = BazelRunnerBuilder(bazelRunner, "clean")
  fun fetch()  = BazelRunnerBuilder(bazelRunner, "fetch")
  fun info()   = BazelRunnerBuilder(bazelRunner, "info")
  fun run()    = BazelRunnerBuilder(bazelRunner, "run")
  fun query()  = BazelRunnerBuilder(bazelRunner, "query")
  fun build()  = BazelRunnerBuildBuilder(bazelRunner, "build")
  fun test()   = BazelRunnerBuildBuilder(bazelRunner, "test")
}
