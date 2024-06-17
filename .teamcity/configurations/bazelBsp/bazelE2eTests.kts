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

open class SampleRepo(
    vcsRoot: GitVcsRoot,
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:sample_repo_test"
)

object SampleRepoGitHub : SampleRepo(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object SampleRepoSpace : SampleRepo(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class LocalJdk(
    vcsRoot: GitVcsRoot,
) : E2ETest(
    vcsRoot = vcsRoot,
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

object LocalJdkGitHub : LocalJdk(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object LocalJdkSpace : LocalJdk(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class RemoteJdk(
    vcsRoot: GitVcsRoot
): E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:remote_jdk_test"
)

object RemoteJdkGitHub : RemoteJdk(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object RemoteJdkSpace : RemoteJdk(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class ServerDownloadsBazelisk(
    vcsRoot: GitVcsRoot
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:server_downloads_bazelisk_test",
)

object ServerDownloadsBazeliskGitHub : ServerDownloadsBazelisk(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object ServerDownloadsBazeliskSpace : ServerDownloadsBazelisk(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class KotlinProject(
    vcsRoot: GitVcsRoot
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:kotlin_project_test",
)

object KotlinProjectGitHub : KotlinProject(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object KotlinProjectSpace : KotlinProject(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class AndroidProject(
    vcsRoot: GitVcsRoot,
    requirements: (Requirements.() -> Unit)? = null
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:android_project_test",
    requirements = requirements
)

object AndroidProjectGitHub : AndroidProject(
    vcsRoot = BaseConfiguration.GitHubVcs,
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object AndroidProjectSpace : AndroidProject(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class AndroidKotlinProject(
    vcsRoot: GitVcsRoot,
    requirements: (Requirements.() -> Unit)? = null
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:android_kotlin_project_test",
    requirements = requirements
)

object AndroidKotlinProjectGitHub : AndroidKotlinProject(
    vcsRoot = BaseConfiguration.GitHubVcs,
    requirements = {
        endsWith("cloud.amazon.agent-name-prefix", "-Large")
        equals("container.engine.osType", "linux")
    }
)

object AndroidKotlinProjectSpace : AndroidKotlinProject(
    vcsRoot = BaseConfiguration.SpaceVcs
)

open class ScalaProject(
    vcsRoot: GitVcsRoot
) : E2ETest(
    vcsRoot = vcsRoot,
    targets = "//e2e:enabled_rules_test",
)

object ScalaProjectGitHub : ScalaProject(
    vcsRoot = BaseConfiguration.GitHubVcs
)

object ScalaProjectSpace : ScalaProject(
    vcsRoot = BaseConfiguration.SpaceVcs
)