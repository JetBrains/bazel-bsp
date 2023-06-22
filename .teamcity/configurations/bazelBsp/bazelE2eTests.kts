package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script


open class BazelBspE2ETestsBuildType(
    targets: String,
    steps: (BuildSteps.() -> Unit)? = null
) : BaseConfiguration.BaseBuildType(

    name = "[e2e tests] $targets test",
    vcsRoot = BaseConfiguration.BazelBspVcs,
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
            arguments = "--test_output=errors"
            param("toolPath", "/usr/local/bin")
        }
    },
    requirements =  {
        contains("cloud.amazon.agent-name-prefix", "Linux-Large")
    }
)

object SampleRepoE2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspSampleRepoTest",
)

object BazelBspLocalJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspLocalJdkTest",
    steps = {
        script {
            this.name = "set JDK to 17"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail

                echo "##teamcity[setParameter name='env.JAVA_HOME' value='%env.JDK_17_0%']"
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
