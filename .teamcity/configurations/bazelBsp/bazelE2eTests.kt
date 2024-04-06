package configurations.bazelBsp

import configurations.BaseBuildType
import configurations.BazelBspVcs
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script


open class BazelBspE2ETestsBuildType(
    targets: String,
    steps: (BuildSteps.() -> Unit)? = null,
) : BaseBuildType(
    name = "[e2e tests] $targets",
    vcsRoot = BazelBspVcs,
    setupSteps = true,
    artifactRules = "+:/home/teamcity/.cache/bazel/_bazel_teamcity/*/execroot/_main/bazel-out/k8-fastbuild/testlogs/e2e/** => testlogs.zip",
    steps = {
        steps?.invoke(this)
        bazel {
            this.name = "test $targets"
            this.command = "test"
            this.targets = targets
            // This fixes FileUtils.getCacheDirectory in integration tests
            this.arguments = "--sandbox_writable_path=/home/teamcity/.cache"
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/local/bin")
        }
    }
)

object SampleRepoBazelE2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:sample_repo_test",
)

object BazelBspLocalBazelJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:local_jdk_test",
    steps = {
        script {
            this.name = "set JDK to 17"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail

                echo "##teamcity[setParameter name='env.JAVA_HOME' value='%env.JDK_17_0%']"
            """.trimIndent()
        }
    },
)

object BazelBspRemoteBazelJdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:remote_jdk_test",
)

object ServerDownloadsBazeliskTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:server_downloads_bazelisk_test",
)

object KotlinProjectTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:kotlin_project_test",
)

object AndroidProjectTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:android_project_test",
)

object AndroidKotlinProjectTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:android_kotlin_project_test",
)
