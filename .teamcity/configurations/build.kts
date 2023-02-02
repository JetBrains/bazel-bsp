package configurations

import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.bazel

open class BuildBuildType(command: String, targets: String) : BaseConfiguration.BaseBuildType(
    name = "[build] build the project",
    steps = {
        script {
            name = "Install necessary pakcages via dockerized Ubuntu"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                apt-get update -q
                apt-get install -y build-essential
                
                # check installation
                gcc --version ||:
                find / -name 'cc1plus' 2>/dev/null ||:
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerPull = true
            dockerImage = "ubuntu:focal"
            dockerRunParameters = "-v /usr/:/usr/"
        }
        script {
            name = "Install Bazelisk"
            scriptContent = """
                    #!/bin/bash
                    set -euxo pipefail
                    
                    mkdir -p "%system.agent.persistent.cache%/bazel"
                    curl -L https://github.com/bazelbuild/bazelisk/releases/download/v1.15.0/bazelisk-linux-amd64 -o  \
                    "%system.agent.persistent.cache%/bazel/bazel"
                    
                    chmod +x "%system.agent.persistent.cache%/bazel/bazel"
                    export PATH="%system.agent.persistent.cache%/bazel:${'$'}PATH"
                    
                    
                    # check installation
                    gcc --version ||:
                    find / -name 'cc1plus' 2>/dev/null ||:
                    bazel version
                """.trimIndent()
        }
        bazel {
            this.name = "$command $targets"
            this.command = command
            this.targets = targets
            param("toolPath", "%system.agent.persistent.cache%/bazel")
        }
    },
    failureConditions = { },
)

object BuildTheProject : BuildBuildType(
    command = "build",
    targets = "//...",
)
