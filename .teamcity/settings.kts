import configurations.bazelBsp.*
import configurations.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


version = "2023.05"

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
            buildType(BazelE2eTests.SampleRepoBazel6E2ETest)
            buildType(BazelE2eTests.SampleRepoBazel5E2ETest)
            buildType(BazelE2eTests.BazelBspLocalBazel6JdkTest)
            buildType(BazelE2eTests.BazelBspLocalBazel5JdkTest)
            buildType(BazelE2eTests.BazelBspRemoteBazel6JdkTest)
            buildType(BazelE2eTests.BazelBspRemoteBazel5JdkTest)
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
        RelativeId("E2eTestsE2eBazelBspSampleRepoTestTestWithBazel621"),
        RelativeId("E2eTestsE2eBazelBspSampleRepoTestTestWithBazel541"),
        RelativeId("E2eTestsE2eBazelBspLocalJdkTestTestWithBazel621"),
        RelativeId("E2eTestsE2eBazelBspLocalJdkTestTestWithBazel541"),
        RelativeId("E2eTestsE2eBazelBspRemoteJdkTestTestWithBazel621"),
        RelativeId("E2eTestsE2eBazelBspRemoteJdkTestTestWithBazel541"),
        RelativeId("BazelBspResults")
    )
})
