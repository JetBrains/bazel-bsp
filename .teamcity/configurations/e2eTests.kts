package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureConditions


open class BazelBspE2ETestsBuildType(targets: String, failureConditions: FailureConditions.() -> Unit = {}) : BaseConfiguration.BaseBuildType(
    name = "[e2e tests] $targets test",
    setupSteps = true,
    steps = {
        script {
            this.name = "Switch Java to Java11"
            scriptContent = "export JAVA_HOME=%env.JDK_11_0%"
        }
        bazel {
            this.name = "test $targets"
            this.command = "run"
            this.targets = targets
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "%system.agent.persistent.cache%/bazel")
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
    }
)

object BazelBspRemoteJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspRemoteJdkTest",
)


object CppProjectE2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspCppProjectTest",
)
