import configurations.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

version = "2022.10"

project {
    vcsRoot(BaseConfiguration.BazelBspVcs)

    val allSteps = sequential {
        parallel {
            buildType(Format.JavaFormat)
            buildType(Format.BuildifierFormat)
        }

        buildType(Build.BuildTheProject, options = {
            onDependencyFailure = FailureAction.IGNORE
            onDependencyCancel = FailureAction.CANCEL
        })

        buildType(UnitTests.UnitTests, options = {
            onDependencyFailure = FailureAction.IGNORE
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.IGNORE
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(E2eTests.SampleRepoE2ETest)

            buildType(E2eTests.BazelBspLocalJdkTest)
            buildType(E2eTests.BazelBspRemoteJdkTest)

            buildType(E2eTests.CppProjectE2ETest)
        }

        buildType(ResultsAggregator)

        buildType(Release.Release)

    }.buildTypes()

    allSteps.forEach { buildType(it) }

    // we dont want to trigger it here for releases
    allSteps.dropLast(1).last().triggers {
        vcs {
            triggerRules = """
                +:.
                -:comment=^\[release\]:**
            """.trimIndent()
        }
    }

    allSteps.last().triggers {
        vcs {
            triggerRules = """
                +:comment=^\[release\]:**
            """.trimIndent()

            branchFilter = "+:<default>"
        }
    }

    buildTypesOrderIds = arrayListOf(RelativeId("FormatBuildifier"),
        RelativeId("FormatGoogleJavaFormat"),
        RelativeId("BuildBuildTheProject"),
        RelativeId("UnitTestsUnitTests"),
        RelativeId("E2eTestsE2eBazelBspSampleRepoTestTest"),
        RelativeId("E2eTestsE2eBazelBspLocalJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspRemoteJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspCppProjectTestTest"),
        RelativeId("ReleaseNewRelease"),
        RelativeId("PipelineResults"))

}

object ResultsAggregator : BuildType({
    id("pipeline results".toExtId())

    name = "pipeline results"
    type = Type.COMPOSITE

    vcs {
        root(BaseConfiguration.BazelBspVcs)
        showDependenciesChanges = true
    }
})
