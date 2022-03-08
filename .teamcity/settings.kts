import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.version

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

version = "2018.2"

project {
    buildType(BuildTheProject)
//    buildType(Format)
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

    triggers {
        vcs {
        }
    }
})


//object Format : BuildType({
//    name = "format"
//
//    steps {
//        script {
//            name = "Google Java Format"
//            scriptContent = """google-java-format -i --set-exit-if-changed ${'$'}(find . -type f -name "*.java")"""
//            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
//            dockerPull = true
//            dockerImage = "vandmo/google-java-format"
//        }
//    }
//
//    triggers {
//        vcs {
//        }
//    }
//})
