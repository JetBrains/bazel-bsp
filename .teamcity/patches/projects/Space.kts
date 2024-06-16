package patches.projects

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the project with id = 'Space'
accordingly, and delete the patch script.
*/
changeProject(RelativeId("Space")) {
    expectBuildTypesOrder(RelativeId("SpaceFormatBuildifier"), RelativeId("SpaceBuildBuildBazelBsp"), RelativeId("SpaceUnitTestsUnitTests"), RelativeId("SpaceE2eTestsE2eSampleRepoTest"), RelativeId("SpaceE2eTestsE2eLocalJdkTest"), RelativeId("SpaceE2eTestsE2eRemoteJdkTest"), RelativeId("SpaceE2eTestsE2eServerDownloadsBazeliskTest"), RelativeId("SpaceE2eTestsE2eKotlinProjectTest"), RelativeId("SpaceE2eTestsE2eAndroidProjectTest"), RelativeId("SpaceE2eTestsE2eAndroidKotlinProjectTest"), RelativeId("SpaceE2eTestsE2eEnabledRulesTest"), RelativeId("SpaceE2eTestsPluginRun"), RelativeId("SpaceBenchmark1001Targets"), RelativeId("SpaceResults"))
    buildTypesOrderIds = arrayListOf(RelativeId("SpaceFormatBuildifier"), RelativeId("SpaceBuildBuildBazelBsp"), RelativeId("SpaceUnitTestsUnitTests"), RelativeId("SpaceE2eTestsE2eSampleRepoTest"), RelativeId("SpaceE2eTestsE2eLocalJdkTest"), RelativeId("SpaceE2eTestsE2eRemoteJdkTest"), RelativeId("SpaceE2eTestsE2eServerDownloadsBazeliskTest"), RelativeId("SpaceE2eTestsE2eKotlinProjectTest"), RelativeId("SpaceE2eTestsE2eAndroidProjectTest"), RelativeId("SpaceE2eTestsE2eAndroidKotlinProjectTest"), RelativeId("SpaceE2eTestsE2eEnabledRulesTest"), RelativeId("SpaceE2eTestsPluginRun"), RelativeId("SpaceResults"))
}
