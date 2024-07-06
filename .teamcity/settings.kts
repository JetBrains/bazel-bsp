import configurations.*
import configurations.bazelBsp.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.Project


version = "2024.03"

project{
    subProject(BazelBspGitHub)
    subProject(BazelBspSpace)
}

object BazelBspGitHub : Project({

    name = "Bazel-BSP GH"
    id("GitHub".toExtId())

    vcsRoot(BaseConfiguration.GitHubVcs)

    // setup pipeline chain for bazel-bsp
    val allSteps = sequential {

        buildType(BazelFormat.GitHub, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(BazelBuild.GitHub)
            buildType(BazelUnitTests.GitHub)
            buildType(BazelE2eTests.SampleRepoGitHub)
            buildType(BazelE2eTests.LocalJdkGitHub)
            buildType(BazelE2eTests.RemoteJdkGitHub)
            buildType(BazelE2eTests.ServerDownloadsBazeliskGitHub)
            buildType(BazelE2eTests.AndroidProjectGitHub)
            buildType(BazelE2eTests.AndroidKotlinProjectGitHub)
            buildType(BazelE2eTests.ScalaProjectGitHub)
            buildType(BazelE2eTests.KotlinProjectGitHub)
//            buildType(BazelE2ePluginTests.GitHub)
            buildType(BazelBenchmark.GitHub)
        }

        buildType(ResultsAggregator.GitHub, options = {
            onDependencyFailure = FailureAction.ADD_PROBLEM
            onDependencyCancel = FailureAction.ADD_PROBLEM
        })

    }.buildTypes()

    // initialize all build steps for bazel-bsp
    allSteps.forEach { buildType(it) }

    // setup trigger for bazel-bsp pipeline
    allSteps.last().triggers {
        vcs {
            branchFilter = "+:pull/*"
            triggerRules = """
                -:**.md
                -:**.yaml
                -:/.teamcity/**
            """.trimIndent()
        }

    }

    // setup display order for bazel-bsp pipeline
    buildTypesOrderIds = arrayListOf(
        RelativeId("GitHubFormatBuildifier"),
        RelativeId("GitHubBuildBuildBazelBsp"),
        RelativeId("GitHubUnitTestsUnitTests"),
        RelativeId("GitHubE2eTestsE2eSampleRepoTest"),
        RelativeId("GitHubE2eTestsE2eLocalJdkTest"),
        RelativeId("GitHubE2eTestsE2eRemoteJdkTest"),
        RelativeId("GitHubE2eTestsE2eServerDownloadsBazeliskTest"),
        RelativeId("GitHubE2eTestsE2eKotlinProjectTest"),
        RelativeId("GitHubE2eTestsE2eAndroidProjectTest"),
        RelativeId("GitHubE2eTestsE2eAndroidKotlinProjectTest"),
        RelativeId("GitHubE2eTestsE2eEnabledRulesTest"),
//        RelativeId("GitHubE2eTestsPluginRun"),
        RelativeId("GitHubBenchmark10Targets"),
        RelativeId("GitHubResults")
    )
})

object BazelBspSpace : Project({

    name = "Bazel-BSP Space"
    id("Space".toExtId())

    vcsRoot(BaseConfiguration.SpaceVcs)

    // setup pipeline chain for bazel-bsp
    val allSteps = sequential {

        buildType(BazelFormat.Space, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(BazelBuild.Space)
            buildType(BazelUnitTests.Space)
            buildType(BazelE2eTests.SampleRepoSpace)
            buildType(BazelE2eTests.LocalJdkSpace)
            buildType(BazelE2eTests.RemoteJdkSpace)
            buildType(BazelE2eTests.ServerDownloadsBazeliskSpace)
            buildType(BazelE2eTests.AndroidProjectSpace)
            buildType(BazelE2eTests.AndroidKotlinProjectSpace)
            buildType(BazelE2eTests.ScalaProjectSpace)
            buildType(BazelE2eTests.KotlinProjectSpace)
//            buildType(BazelE2ePluginTests.Space)
            buildType(BazelBenchmark.Space)
        }

        buildType(ResultsAggregator.Space, options = {
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
                +:*
                -:bazel-steward*
            """.trimIndent()
            triggerRules = """
                -:**.md
                -:**.yaml
                -:/.teamcity/**
            """.trimIndent()
        }
    }

    // setup display order for bazel-bsp pipeline
    buildTypesOrderIds = arrayListOf(
        RelativeId("SpaceFormatBuildifier"),
        RelativeId("SpaceBuildBuildBazelBsp"),
        RelativeId("SpaceUnitTestsUnitTests"),
        RelativeId("SpaceE2eTestsE2eSampleRepoTest"),
        RelativeId("SpaceE2eTestsE2eLocalJdkTest"),
        RelativeId("SpaceE2eTestsE2eRemoteJdkTest"),
        RelativeId("SpaceE2eTestsE2eServerDownloadsBazeliskTest"),
        RelativeId("SpaceE2eTestsE2eKotlinProjectTest"),
        RelativeId("SpaceE2eTestsE2eAndroidProjectTest"),
        RelativeId("SpaceE2eTestsE2eAndroidKotlinProjectTest"),
        RelativeId("SpaceE2eTestsE2eEnabledRulesTest"),
//        RelativeId("SpaceE2eTestsPluginRun"),
        RelativeId("SpaceBenchmark10Targets"),
        RelativeId("SpaceResults")
    )
})

