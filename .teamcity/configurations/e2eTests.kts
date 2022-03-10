package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class BazelBspE2ETestsBuildType(testLabel: String) : BaseConfiguration.BazelBspBaseBuildType(
    name = "[e2e tests] $testLabel test",
    steps = {
        script {
            this.name = "running $testLabel e2e test"
            this.scriptContent = """bazel run $testLabel/..."""
            this.dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            this.dockerPull = true
            this.dockerImage = "andrefmrocha/bazelisk"
        }
    }
)

object SampleRepoE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspSampleRepoTest"
)

object ActionGraphV1E2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspActionGraphV1Test"
)

object ActionGraphV2E2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspActionGraphV2Test"
)

object Java8ProjectE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspJava8ProjectTest"
)

object Java11ProjectE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspJava11ProjectTest"
)

object CppProjectE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspCppProjectTest"
)

object BazelBspEntireRepositoryImportE2ETest : BazelBspE2ETestsBuildType(
    testLabel = "//e2e:BazelBspEntireRepositoryImportTest"
)