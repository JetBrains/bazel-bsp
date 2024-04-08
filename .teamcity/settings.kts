import configurations.bazelBsp.*
import configurations.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


version = "2024.03"

project(BazelBsp)

object BazelBsp : Project({

    vcsRoot(BazelBspVcs)

    // setup pipeline chain for bazel-bsp
    val allSteps = sequential {

        buildType(BuildifierFormat, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(BuildTheProject)
            buildType(UnitTests)
            buildType(SampleRepoBazelE2ETest)
            buildType(BazelBspLocalBazelJdkTest)
            buildType(BazelBspRemoteBazelJdkTest)
            buildType(ServerDownloadsBazeliskTest)
            buildType(AndroidProjectTest)
            buildType(AndroidKotlinProjectTest)
            buildType(KotlinProjectTest)
            buildType(RegularBenchmark)
        }

        buildType(BazelBspAggregator, options = {
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
        RelativeId("Benchmark1001Targets"),
        RelativeId("BazelBspResults")
    )
})
