package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspE2ETestsBuildType(testLabel: String) : BaseConfiguration.BaseBuildType(
    name = "[e2e tests] $testLabel test",
    steps = {
        script {
            this.name = "running $testLabel e2e test"
            this.scriptContent = """bazel run $testLabel"""
            this.dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            this.dockerPull = true
            this.dockerImage = "cbills/build-runner"
        }
    }
)

object SampleRepoE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspSampleRepoTest"
)
object BazelBspLocalJdkTest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspLocalJdkTest"
)

object BazelBspRemoteJdkTest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspRemoteJdkTest"
)

object CppProjectE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspCppProjectTest"
)
