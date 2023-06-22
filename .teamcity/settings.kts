import configurations.bazelBsp.*
import configurations.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
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
            buildType(BazelE2eTests.SampleRepoE2ETest)
            buildType(BazelE2eTests.BazelBspLocalJdkTest)
            buildType(BazelE2eTests.BazelBspRemoteJdkTest)
            buildType(BazelE2eTests.CppProjectE2ETest)
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
                +:pull/*
            """.trimIndent()
        }
    }

    // setup display order for bazel-bsp pipeline
    buildTypesOrderIds = arrayListOf(
        RelativeId("FormatBuildifier"),
        RelativeId("BuildBuildBazelBsp"),
        RelativeId("UnitTestsUnitTests"),
        RelativeId("E2eTestsE2eBazelBspSampleRepoTestTest"),
        RelativeId("E2eTestsE2eBazelBspLocalJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspRemoteJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspCppProjectTestTest"),
        RelativeId("BazelBspResults")
    )
})
