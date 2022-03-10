import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.sequential
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {
    vcsRoot(BazelBspVcs)

    val steps = sequential {
        parallel {
            buildType(JavaFormat)
            buildType(BuildifierFormat)
        }

        buildType(BuildTheProject)
    }.buildTypes()

    steps.forEach { buildType(it) }

    steps.last().triggers {
        vcs { }
    }

}

object BuildTheProject : BuildType({
    name = "build"

    steps {
        script {
            name = "build the project"
            scriptContent = """bazel build //..."""
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "andrefmrocha/bazelisk"
        }
    }

    vcs {
        root(BazelBspVcs)
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

object JavaFormat : BuildType({
    name = "[format] google java format"

    steps {
        script {
            name = "formatting check with google java format"
            scriptContent = """google-java-format -i --set-exit-if-changed ${'$'}(find . -type f -name "*.java")"""
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "vandmo/google-java-format"
        }
    }

    vcs {
        root(BazelBspVcs)
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

object BuildifierFormat : BuildType({
    name = "[format] buildifier"

    steps {
        script {
            name = "formatting check with buildifier"
            scriptContent = """
                buildifier -r .
                buildifier --lint=fix -r .
                git diff --exit-code
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "andrefmrocha/buildifier"
        }
    }

    vcs {
        root(BazelBspVcs)
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

object BazelBspVcs : GitVcsRoot({
    name = "bazel-bsp"
    url = "https://github.com/JetBrains/bazel-bsp.git"
    branch = "master"
    branchSpec = "refs/heads/(*)"
})
