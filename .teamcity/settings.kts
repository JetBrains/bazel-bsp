import configurations.*
import configurations.bazelBsp.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


version = "2024.03"

project(BazelBsp)

object BazelBsp : Project({

    vcsRoot(BaseConfiguration.BazelBspVcs)

    // setup pipeline chain for bazel-bsp
    val allSteps = sequential {

        buildType(BazelFormat.BuildifierFormat, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(BazelBuild.BuildTheProject)
            buildType(BazelUnitTests.UnitTests)
            buildType(BazelE2eTests.SampleRepoBazelE2ETest)
            buildType(BazelE2eTests.BazelBspLocalBazelJdkTest)
            buildType(BazelE2eTests.BazelBspRemoteBazelJdkTest)
            buildType(BazelE2eTests.ServerDownloadsBazeliskTest)
            buildType(BazelE2eTests.AndroidProjectTest)
            buildType(BazelE2eTests.AndroidKotlinProjectTest)
            buildType(BazelE2eTests.KotlinProjectTest)
            buildType(BazelE2ePluginTests.LocalProbeTests)
            buildType(BazelBenchmark.RegularBenchmark)
        }

        buildType(ResultsAggregator.BazelBspAggregator, options = {
            onDependencyFailure = FailureAction.ADD_PROBLEM
            onDependencyCancel = FailureAction.ADD_PROBLEM
        })

    }.buildTypes()

    // initialize all build steps for bazel-bsp
    allSteps.forEach { buildType(it) }

    // setup trigger for bazel-bsp pipeline
    allSteps.last().triggers {
        vcs {
            branchFilter = """
                +:<default>
                +:pull/*
            """.trimIndent()
        }
    }

    // setup display order for bazel-bsp pipeline
    buildTypesOrderIds = arrayListOf(
        RelativeId("FormatBuildifier"),
        RelativeId("BuildBuildBazelBsp"),
        RelativeId("UnitTestsUnitTests"),
        RelativeId("E2eTestsE2eSampleRepoTest"),
        RelativeId("E2eTestsE2eLocalJdkTest"),
        RelativeId("E2eTestsE2eRemoteJdkTest"),
        RelativeId("E2eTestsE2eServerDownloadsBazeliskTest"),
        RelativeId("E2eTestsE2eKotlinProjectTest"),
        RelativeId("E2eTestsE2eAndroidProjectTest"),
        RelativeId("E2eTestsE2eAndroidKotlinProjectTest"),
        RelativeId("E2eTestsPluginRun"),
        RelativeId("Benchmark1001Targets"),
        RelativeId("BazelBspResults")
    )
})
