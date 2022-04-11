package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

open class E2ETestsBuildType(testTarget: String) : BaseConfiguration.BaseBazelBuildType(
    name = "[e2e tests] $testTarget test",
    command = "run",
    targets = testTarget,
)

object SampleRepoE2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspSampleRepoTest",
)

object ActionGraphV1E2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspActionGraphV1Test",
)

object ActionGraphV2E2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspActionGraphV2Test",
)

object Java8ProjectE2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspJava8ProjectTest",
)

object Java11ProjectE2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspJava11ProjectTest",
)

object CppProjectE2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspCppProjectTest",
)

object EntireRepositoryImportE2ETest : E2ETestsBuildType(
    testTarget = "//e2e:BazelBspEntireRepositoryImportTest",
)
