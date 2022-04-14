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
})

open class BaseBazelBuildType(name: String, command: String, targets: String?) :
    BaseBuildType(
        name = name,
        steps = {
            script {
                this.scriptContent = """
                wget $bazeliskUrl --directory-prefix=$cacheDir --no-clobber
                chmod +x $bazelPath
            """.trimIndent()
            }

            bazel {
                this.command = command
                this.targets = targets
                this.arguments = "--disk_cache=bazel-cache"

                param("toolPath", bazelPath)
            }
        }) {

    companion object {
        private const val bazeliskUrl =
            "https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64"
        private const val cacheDir = "%system.agent.persistent.cache%/bazel/"
        private const val bazelPath = "${cacheDir}bazelisk-linux-amd64"
    }
}

open class BaseBazelBuildTypeClean(
    name: String,
    command1: String,
    targets1: String?,
    arguments1: String? = null,
    command2: String? = null,
    targets2: String? = null,
    arguments2: String? = null,
) : BaseBuildType(
    name = name, steps = {
        script {
            this.scriptContent = """
                wget $bazeliskUrl --directory-prefix=$bazeliskCacheDir --no-clobber
                chmod +x $bazelPath
            """.trimIndent()
        }

        bazel {
            this.command = "clean"
            param("toolPath", bazelPath)
        }

//        bazel {
//            this.command = "clean"
//            this.arguments = "--disk_cache=$cacheDir"
//            param("toolPath", bazelPath)
//        }

        bazel {
            this.command = command1
            this.targets = targets1
//            this.arguments = "--disk_cache=$cacheDir $arguments1"

            param("toolPath", bazelPath)
        }

        if (arguments2 != null) {
            bazel {
                this.command = command2
                this.targets = targets2
//                this.arguments = "--disk_cache=$cacheDir $arguments2"

                param("toolPath", bazelPath)
            }
        }
    },
//    artifactRules = "$cacheDir => $cacheDir"
) {

    companion object {
        private const val bazeliskUrl =
            "https://github.com/bazelbuild/bazelisk/releases/download/v1.11.0/bazelisk-linux-amd64"
        private const val bazeliskCacheDir = "%system.agent.persistent.cache%/bazel/"
        private const val bazelPath = "${bazeliskCacheDir}bazelisk-linux-amd64"

        private const val cacheDir = "bazel-cache"
    }
}

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "refs/heads/(*)"
})

