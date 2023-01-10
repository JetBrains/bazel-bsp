package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'E2eTestsE2eBazelBspActionGraphV1TestTest'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("E2eTestsE2eBazelBspActionGraphV1TestTest")) {
    expectSteps {
        script {
            name = "running //e2e:BazelBspActionGraphV1Test e2e test"
            scriptContent = "bazel run //e2e:BazelBspActionGraphV1Test"
            dockerImage = "cbills/build-runner"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
        }
    }
    steps {
        update<ScriptBuildStep>(0) {
            clearConditions()
            dockerImage = "andrefmrocha/bazelisk"
        }
    }
}
