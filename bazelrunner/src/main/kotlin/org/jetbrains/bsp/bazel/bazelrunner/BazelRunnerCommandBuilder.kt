package org.jetbrains.bsp.bazel.bazelrunner

class BazelRunnerCommandBuilder internal constructor(private val bazelRunner: BazelRunner) {
  fun aquery() = BazelRunnerBuilder(bazelRunner, listOf("aquery"))
  fun clean()  = BazelRunnerBuilder(bazelRunner, listOf("clean"))
  fun fetch()  = BazelRunnerBuilder(bazelRunner, listOf("fetch"))
  fun info()   = BazelRunnerBuilder(bazelRunner, listOf("info"))
  fun run()    = BazelRunnerBuilder(bazelRunner, listOf("run"))
  fun mod(subcommand: String) = BazelRunnerBuilder(bazelRunner, listOf("mod", subcommand))
  fun graph() = mod("graph")
  fun deps() = mod("deps")
  fun allPaths() = mod("all_paths")
  fun path() = mod("path")
  fun explain() = mod("explain")
  fun showRepo() = mod("show_repo")
  fun showExtension() = mod("show_extension")
  fun query()  = BazelRunnerBuilder(bazelRunner, listOf("query"))
  fun build()  = BazelRunnerBuildBuilder(bazelRunner, listOf("build"))
  fun test()   = BazelRunnerBuildBuilder(bazelRunner, listOf("test"))
}
