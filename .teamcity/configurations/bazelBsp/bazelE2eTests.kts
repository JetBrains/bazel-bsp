package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script


open class BazelBspE2ETestsBuildType(
    targets: String,
    steps: (BuildSteps.() -> Unit)? = null,
    bazelVersion: String,
) : BaseConfiguration.BaseBuildType(
    name = "[e2e tests] $targets test with bazel $bazelVersion",
    vcsRoot = BaseConfiguration.BazelBspVcs,
    setupSteps = true,
    steps = {
        steps?.invoke(this)

        script {
            this.name = "set bazel version"
            scriptContent = """
                |#!/bin/bash
                |set -euxo pipefail
                |
                |export USE_BAZEL_VERSION=$bazelVersion
                |echo "##teamcity[setParameter name='env.USE_BAZEL_VERSION' value='${'$'}USE_BAZEL_VERSION']"
                """.trimMargin()
        }
        bazel {
            this.name = "test $targets"
            this.command = "run"
            this.targets = targets
            logging = BazelStep.Verbosity.Diagnostic
            param("toolPath", "/usr/local/bin")
        }
    }
)

object SampleRepoBazel6E2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspSampleRepoTest",
    bazelVersion = "6.2.1"
)

object SampleRepoBazel5E2ETest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspSampleRepoTest",
    bazelVersion = "5.4.1"
)

object BazelBspLocalBazel6JdkTest : BazelBspE2ETestsBuildType(
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
    },
    bazelVersion = "6.2.1"
)

object BazelBspLocalBazel5JdkTest : BazelBspE2ETestsBuildType(
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
    },
    bazelVersion = "5.4.1"
)

object BazelBspRemoteBazel6JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspRemoteJdkTest",
    bazelVersion = "6.2.1"
)

object BazelBspRemoteBazel5JdkTest : BazelBspE2ETestsBuildType(
    targets = "//e2e:BazelBspRemoteJdkTest",
    bazelVersion = "5.4.1"
)
