import configurations.bazelBsp.*
import configurations.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


version = "2023.11"

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
            buildType(SampleRepoBazel6E2ETest)
            buildType(SampleRepoBazel5E2ETest)
            buildType(BazelBspLocalBazel6JdkTest)
            buildType(BazelBspLocalBazel5JdkTest)
            buildType(BazelBspRemoteBazel6JdkTest)
            buildType(BazelBspRemoteBazel5JdkTest)
            buildType(ServerDownloadsBazeliskTest)
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
        RelativeId("E2eTestsE2eSampleRepoTestBazel632test"),
        RelativeId("E2eTestsE2eSampleRepoTestBazel532test"),
        RelativeId("E2eTestsE2eLocalJdkTestBazel632test"),
        RelativeId("E2eTestsE2eLocalJdkTestBazel532test"),
        RelativeId("E2eTestsE2eRemoteJdkTestBazel632test"),
        RelativeId("E2eTestsE2eRemoteJdkTestBazel532test"),
        RelativeId("E2eTestsE2eServerDownloadsBazeliskTestBazel632test"),
        RelativeId("BazelBspResults")
    )
})
