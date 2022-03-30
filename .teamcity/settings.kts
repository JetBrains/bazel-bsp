import configurations.*
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version

version = "2021.2"

project {
    vcsRoot(BaseConfiguration.BazelBspVcs)

    val steps = sequential {
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

            buildType(E2eTests.CppProjectE2ETest)

            buildType(E2eTests.EntireRepositoryImportE2ETest)
        }
    }.buildTypes()

    steps.forEach { buildType(it) }

    steps.last().triggers {
        vcs { }
    }
}
