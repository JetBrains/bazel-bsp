package configurations

import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

open class BaseBuildType(name: String, steps: BuildSteps.() -> Unit, artifactRules: String = "") : BuildType({
    id(name.toExtId())
    this.name = name
    this.artifactRules = artifactRules
    this.steps(steps)

    vcs {
        root(BazelBspVcs)
    }

    requirements {
        equals("teamcity.agent.jvm.os.name", "Linux")
    }

    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:3f56fecd-4c69-4c60-85f2-13bc42792558"
                }
            }
        }
    }
})

open class BaseBazelBuildType(name: String, command: String, targets: String?, arguments: String? = null) :
    BaseBuildType(name, {
        script {
            this.scriptContent = """
                wget $bazeliskUrl --directory-prefix=$cacheDir --no-clobber
                chmod +x $bazelPath
                rm -r .bazel-build-output
            """.trimIndent()
        }

        bazel {
            this.command = command
            this.targets = targets
            this.arguments = arguments

            param("toolPath", bazelPath)
        }

    }, "~/.cache/bazel => ~/.cache/bazel") {
    companion object {
        private const val bazeliskUrl =
            "https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64"
        private const val cacheDir = "%system.agent.persistent.cache%/bazel/"
        private const val bazelPath = "${cacheDir}bazelisk-linux-amd64"
    }
}

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "refs/heads/(*)"
})

