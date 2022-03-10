import configurations.BaseConfiguration
import configurations.Build
import configurations.Format
import configurations.UnitTests
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

        buildType(Build.BuildTheProject)

        parallel {
            buildType(UnitTests.BazelRunnerUnitTests)
            buildType(UnitTests.CommonsUnitTests)
            buildType(UnitTests.ExecutionContextUnitTests)
            buildType(UnitTests.ServerUnitTests)
        }
    }.buildTypes()

    steps.forEach { buildType(it) }

    steps.last().triggers {
        vcs { }
    }
}
