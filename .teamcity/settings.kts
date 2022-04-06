import configurations.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

version = "2021.2"

project {
    vcsRoot(BaseConfiguration.BazelBspVcs)

    val allRawSteps = sequential {
        parallel {
            buildType(Format.JavaFormat)
            buildType(Format.BuildifierFormat)
        }

        buildType(Build.BuildTheProject, options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        })

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL
        }) {
            buildType(UnitTests.BazelRunnerUnitTests)
            buildType(UnitTests.CommonsUnitTests)
            buildType(UnitTests.ExecutionContextUnitTests)
            buildType(UnitTests.ServerUnitTests)
        }

        parallel(options = {
            onDependencyFailure = FailureAction.CANCEL
            onDependencyCancel = FailureAction.CANCEL

        }) {
            buildType(E2eTests.SampleRepoE2ETest)

            buildType(E2eTests.ActionGraphV1E2ETest)
            buildType(E2eTests.ActionGraphV2E2ETest)

            buildType(E2eTests.Java8ProjectE2ETest)
            buildType(E2eTests.Java11ProjectE2ETest)

            // buildType(E2eTests.CppProjectE2ETest)

            buildType(E2eTests.EntireRepositoryImportE2ETest)
        }

        buildType(TestAggregator)
    }.buildTypes()

    val indexOfBuildStep = allRawSteps.indexOf(Build.BuildTheProject)
    val stepsBeforeBuild = allRawSteps.take(indexOfBuildStep)
    val stepsAfterBuild = allRawSteps.drop(indexOfBuildStep)

    val stepsAfterBuildWithArtifactDependency = stepsAfterBuild.map {
        it.dependencies.artifacts(Build.BuildTheProject) {
            buildRule = lastSuccessful()
            artifactRules = "bazel-cache"
        }
    }

    val allSteps = stepsBeforeBuild + stepsAfterBuild

    allSteps.forEach { buildType(it) }

    allRawSteps.last().triggers {
        vcs { }
    }
}


object TestAggregator : BuildType({
    id("test aggregator".toExtId())
    name = "test aggregator"
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
                    token = "credentialsJSON:3f56fecd-4c69-4c60-85f2-13bc42792558"
                }
            }
        }
    }
})
