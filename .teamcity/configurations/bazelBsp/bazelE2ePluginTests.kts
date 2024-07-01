package configurations.bazelBsp

import configurations.BaseConfiguration
import jetbrains.buildServer.configs.kotlin.v2019_2.Requirements
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot


open class PluginRun(
    vcsRoot: GitVcsRoot,
    requirements: (Requirements.() -> Unit)? = null
) : BaseConfiguration.BaseBuildType(
    name = "[e2e tests] plugin run",
    vcsRoot = vcsRoot,
    failureConditions = {
        supportTestRetry = true
        testFailure = true
        executionTimeoutMin = 180
    },
    artifactRules = """
        +:/tmp/intellij-bazel/probe/build/reports => reports.zip
        +:/mnt/agent/temp/buildTmp/ide-probe/screenshots => screenshots.zip
    """.trimIndent(),
    setupSteps = true,
    requirements =  requirements,
    steps = {
        script {
            this.name = "clone bazel-bsp and intellij-bazel to tmp"
            id = "clone_bazel_bsp_and_intellij_bazel_to_tmp"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                git clone https://github.com/JetBrains/bazel-bsp.git /tmp/bazel-bsp
                echo -e "targets:\n  //..." > /tmp/bazel-bsp/projectview.bazelproject
                git clone https://github.com/JetBrains/intellij-bazel.git /tmp/intellij-bazel
            """.trimIndent()
        }
        script {
            this.name = "prepare server for publishing local e2e version"
            id = "prepare_server_for_publishing_local_e2e_version"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                #get current version of the server
                current_version=${'$'}(awk -F '"' '/maven_coordinates =/{print ${'$'}2; exit}' server/src/main/kotlin/org/jetbrains/bsp/bazel/BUILD)
                
                #generate e2e version for maven coordinates and add it to file
                new_version="${'$'}{current_version}-E2E"
                sed -i "s/${'$'}current_version/${'$'}new_version/" server/src/main/kotlin/org/jetbrains/bsp/bazel/BUILD
                
                #generate number-only new version for Constants.java
                current_version=${'$'}(echo ${'$'}current_version | awk -F: '{print ${'$'}NF}')
                new_version=${'$'}(echo ${'$'}new_version | awk -F: '{print ${'$'}NF}')
                sed -i "s/${'$'}current_version/${'$'}new_version/" commons/src/main/kotlin/org/jetbrains/bsp/bazel/commons/Constants.java
                
                #get current server version and replace it with the new one in intellij-bazel dependency
                current_server=${'$'}(awk -F '"' '/module = "org.jetbrains.bsp:bazel-bsp", version =/{print ${'$'}4; exit}' /tmp/intellij-bazel/gradle/libs.versions.toml)
                sed -i "s/${'$'}current_server/${'$'}new_version/" /tmp/intellij-bazel/gradle/libs.versions.toml
                
                #add mavenLocal() repo to intellij-bazel so it can find e2e server version
                sed -i '/mavenCentral()/a \  mavenLocal()' /tmp/intellij-bazel/buildSrc/src/main/kotlin/intellijbazel.kotlin-conventions.gradle.kts
            """.trimIndent()
        }
        script {
            this.name = "publish bsp-server in local maven repo"
            id = "publish_bsp_server_in_local_maven_repo"
            scriptContent = """
                #publish to local maven repo
                bazel run --define "maven_repo=file://${'$'}HOME/.m2/repository" //server/src/main/kotlin/org/jetbrains/bsp/bazel:bsp.publish
            """.trimIndent()
        }
        script {
            this.name = "install ide-probe dependencies"
            id = "install_ide_probe_dependencies"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                #install required cpp and other packages
                sudo sh -c 'echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list'  ||:
                sudo curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add -  ||:
                sudo apt-get update -q ||:
                sudo apt-get install -y libxtst6 ||:
                sudo apt-get install -y libx11-6 ||:
                sudo apt-get install -y libxrender1 ||:
                sudo apt-get install -y xvfb ||:
                sudo apt-get install -y openssh-server ||:
                sudo apt-get install -y python3 ||:
                sudo apt-get install -y python3-pip ||:
                sudo apt-get install -y sbt ||:
                sudo apt-get install -y libssl-dev ||:
                sudo apt-get install -y pkg-config ||:
                sudo apt-get install -y x11-apps ||:
                sudo apt-get install -y imagemagick ||:
            """.trimIndent()
        }
        script {
            this.name = "configure IDE-probe"
            id = "configure_IDE_probe"
            workingDir = "/tmp/intellij-bazel"
            scriptContent = """
                #!/bin/bash
                set -euxo pipefail
                
                #turn on virtual display for ide-probe tests
                sed -i '/driver.vmOptions = \[ "-Dgit.process.ignored=false", "-Xms4g", "-Xmx12g" \]/a \\n  driver.display = "xvfb"\n' ./probe-setup/src/main/resources/ideprobe.conf
            """.trimIndent()
        }
        gradle {
            this.name = "run LocalProbeTests"
            id = "run_LocalProbeTests"
            tasks = ":probe:test --tests LocalProbeTests"
            workingDir = "/tmp/intellij-bazel"
            gradleParams = "-Dorg.gradle.jvmargs=-Xmx12g"
            jdkHome = "%env.JDK_17_0%"
            jvmArgs = "-Xmx12g"
        }
    }
)

object GitHub : PluginRun(
    vcsRoot = BaseConfiguration.GitHubVcs,
    requirements =  {
        endsWith("cloud.amazon.agent-name-prefix", "XLarge")
        equals("container.engine.osType", "linux")
    },
)

object Space : PluginRun(
    vcsRoot = BaseConfiguration.SpaceVcs
)