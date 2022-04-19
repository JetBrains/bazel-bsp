import configurations.*
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

version = "2021.2"

project {
    vcsRoot(BaseConfiguration.BazelBspVcs)

    val allSteps = sequential {
        parallel {
            buildType(Format.JavaFormat)
            buildType(Format.BuildifierFormat)
        }

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

            buildType(E2eTests.ActionGraphV1E2ETest)
            buildType(E2eTests.ActionGraphV2E2ETest)

            buildType(E2eTests.Java8ProjectE2ETest)
            buildType(E2eTests.Java11ProjectE2ETest)

            // buildType(E2eTests.CppProjectE2ETest)

            buildType(E2eTests.EntireRepositoryImportE2ETest)
        }

        buildType(ResultsAggregator)
    }.buildTypes()

    allSteps.forEach { buildType(it) }

    // we dont want to trigger it here for releases
    allSteps.last().triggers {
        vcs {
            triggerRules = """
                "-:comment=^[release]:**"
            """.trimIndent()

        }
    }
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
                    token = "credentialsJSON:3f56fecd-4c69-4c60-85f2-13bc42792558"
                }
            }
        }
    }
})
