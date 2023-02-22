import configurations.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs


version = "2022.10"

project {
    vcsRoot(BaseConfiguration.BazelBspVcs)

    val allSteps = sequential {
//        parallel {
//            buildType(Format.JavaFormat)
//            buildType(Format.BuildifierFormat)
//        }

        buildType(Format.BuildifierFormat, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        buildType(Build.BuildTheProject, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        buildType(UnitTests.UnitTests, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(E2eTests.SampleRepoE2ETest)

            buildType(E2eTests.BazelBspLocalJdkTest)
            buildType(E2eTests.BazelBspRemoteJdkTest)

            buildType(E2eTests.CppProjectE2ETest)
        }

        buildType(ResultsAggregator, options = {
            onDependencyFailure = FailureAction.ADD_PROBLEM
            onDependencyCancel = FailureAction.ADD_PROBLEM
        })

        buildType(Release.Release, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

    }.buildTypes()

    allSteps.forEach { buildType(it) }

    // we dont want to trigger it here for releases
    allSteps.dropLast(1).last().triggers {
        vcs {
            triggerRules = """
                +:*
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

    buildTypesOrderIds = arrayListOf(
        RelativeId("FormatBuildifier"),
        RelativeId("FormatGoogleJavaFormat"),
        RelativeId("BuildBuildTheProject"),
        RelativeId("UnitTestsUnitTests"),
        RelativeId("E2eTestsE2eBazelBspSampleRepoTestTest"),
        RelativeId("E2eTestsE2eBazelBspLocalJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspRemoteJdkTestTest"),
        RelativeId("E2eTestsE2eBazelBspCppProjectTestTest"),
        RelativeId("ReleaseNewRelease"),
        RelativeId("PipelineResults")
    )
}

object ResultsAggregator : BuildType({
    id("pipeline results".toExtId())

    name = "pipeline results"
    type = Type.COMPOSITE

    vcs {
        root(BaseConfiguration.BazelBspVcs)
        showDependenciesChanges = true
    }

    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:ac2b6c0a-11e0-47db-b113-784eb5266027"
                }
            }
        }
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_486"
                sendTo = "#bazel-build"
                messageFormat = verboseMessageFormat {
                    addBranch = true
                    addChanges = true
                    addStatusText = true
                    maximumNumberOfChanges = 10
                }
            }
            branchFilter = "+:<default>"
            buildFailed = true
            buildFinishedSuccessfully = true
        }
        pullRequests {
            vcsRootExtId = "${BaseConfiguration.BazelBspVcs.id}"
            provider = jetbrainsSpace {
                filterTargetBranch = "+:<default>"
                authType = connection {
                    connectionId = "PROJECT_EXT_2845"
                }
            }
        }
    }
})
