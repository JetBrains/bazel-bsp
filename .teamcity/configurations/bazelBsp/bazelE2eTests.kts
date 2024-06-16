package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.Requirements
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.BazelStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot


open class E2ETest(
    vcsRoot: GitVcsRoot,
    targets: String,
    steps: (BuildSteps.() -> Unit)? = null,
    requirements: (Requirements.() -> Unit)? = null,
) : BaseConfiguration.BaseBuildType(
    name = "[e2e tests] $targets",
    vcsRoot = vcsRoot,
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
    },
    requirements = requirements
)

object SampleRepoGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:sample_repo_test",
)

object SampleRepoSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:sample_repo_test",
)

object LocalJdkTestGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
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

object LocalJdkTestSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
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

object RemoteJdkTestGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:remote_jdk_test",
)

object RemoteJdkTestSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:remote_jdk_test",
)

object ServerDownloadsBazeliskGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:server_downloads_bazelisk_test",
)

object ServerDownloadsBazeliskSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:server_downloads_bazelisk_test",
)

object KotlinProjectGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:kotlin_project_test",
)

object KotlinProjectSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:kotlin_project_test",
)

object AndroidProjectGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:android_project_test",
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object AndroidProjectSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:android_project_test",
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object AndroidKotlinProjectGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:android_kotlin_project_test",
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object AndroidKotlinProjectSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:android_kotlin_project_test",
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object ScalaProjectGitHub : E2ETest(
    vcsRoot = BaseConfiguration.GitHubVcs,
    targets = "//e2e:enabled_rules_test",
)

object ScalaProjectSpace : E2ETest(
    vcsRoot = BaseConfiguration.SpaceVcs,
    targets = "//e2e:enabled_rules_test",
)