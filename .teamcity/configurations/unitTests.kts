package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class UnitTestsBuildType(targets: String) : BaseConfiguration.BaseBuildType(
    name = "[unit tests] unit tests $targets",
    setupSteps = true,
    steps = {
        bazel {
            this.name = "bazel test $targets"
            this.command = "test"
            this.targets = targets
            arguments = "--test_output=errors"
            param("toolPath", "/usr/bin")
        }
    },
    failureConditions = {
        check(testFailure == true) {
            "Unexpected option value: testFailure = $testFailure"
        }
        testFailure = false
    }
)

object UnitTests : UnitTestsBuildType(
    targets = "//...",
)
