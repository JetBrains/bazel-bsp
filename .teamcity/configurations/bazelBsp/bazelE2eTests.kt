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
    name = "[e2e tests] $targets test",
    vcsRoot = BazelBspVcs,
    setupSteps = true,
    artifactRules = "+:/home/teamcity/.cache/bazel/_bazel_teamcity/*/execroot/_main/bazel-out/k8-fastbuild/testlogs/e2e/** => testlogs.zip",
    steps = {
        steps?.invoke(this)
        bazel {
            this.name = "test $targets"
            this.command = "test"
            this.targets = targets
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/local/bin")
        }
    }
)

object SampleRepoBazel6E2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:sample_repo_test_bazel_6_4_0",
)

object ServerDownloadsBazeliskTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:server_downloads_bazelisk_test_bazel_6_4_0",
)

object SampleRepoBazel5E2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:sample_repo_test_bazel_5_3_2",
)

object BazelBspLocalBazel6JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:local_jdk_test_bazel_6_4_0",
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

object BazelBspLocalBazel5JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:local_jdk_test_bazel_5_3_2",
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

object BazelBspRemoteBazel6JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:remote_jdk_test_bazel_6_4_0",
)

object BazelBspRemoteBazel5JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:remote_jdk_test_bazel_5_3_2",
)
