package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions


open class BazelBspE2ETestsBuildType(
    targets: String,
    failureConditions: FailureConditions.() -> Unit = {},
    steps: (BuildSteps.() -> Unit)? = null
) : BaseConfiguration.BaseBuildType(

    name = "[e2e tests] $targets test",
    setupSteps = true,
    steps = {
        if (steps != null) {
            steps.invoke(this)
        }
        bazel {
            this.name = "test $targets"
            this.command = "run"
            this.targets = targets
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/bin")
        }
    },
    failureConditions = failureConditions,

    )

object SampleRepoE2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspSampleRepoTest",
)

object BazelBspLocalJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspLocalJdkTest",
    failureConditions = {

        check(nonZeroExitCode == true) {
            "Unexpected option value: nonZeroExitCode = $nonZeroExitCode"
        }
        nonZeroExitCode = false
    },
    steps = {
        script {
            this.name = "set JDK to 11"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                export PATH="%env.JDK_11_0%/bin:($)PATH"
                echo "##teamcity[setParameter name='env.PATH' value='($)PATH']"
                
                which java
            """.trimIndent()
        }
    }
)

object BazelBspRemoteJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspRemoteJdkTest",
)

object CppProjectE2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspCppProjectTest",
)
