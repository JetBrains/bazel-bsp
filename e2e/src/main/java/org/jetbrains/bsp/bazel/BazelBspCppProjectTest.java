package org.jetbrains.bsp.bazel;

public class BazelBspCppProjectTest {}

// public class BazelBspCppProjectTest extends BazelBspTestBaseScenario {
//
//  private static final String REPO_NAME = "cpp-project";
//
//  public BazelBspCppProjectTest() {
//    super(REPO_NAME);
//  }
//
//  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
//  // but testing server needs it for queries and etc
//  public static void main(String[] args) {
//    BazelBspCppProjectTest test = new BazelBspCppProjectTest();
//    test.executeScenario();
//  }
//
//  @Override
//  protected List<BazelBspTestScenarioStep> scenarioSteps() {
//    return ImmutableList.of(compareWorkspaceTargetsResults(), cppOptions());
//  }
//
//  private BazelBspTestScenarioStep compareWorkspaceTargetsResults() {
//    CppBuildTarget exampleExampleCppBuildTarget =
//        new CppBuildTarget(null, "compiler", "/bin/gcc", "/bin/gcc");
//
//    BuildTarget exampleExampleBuildTarget =
//        new BuildTarget(
//            new BuildTargetIdentifier("//example:example"),
//            ImmutableList.of(),
//            ImmutableList.of(Constants.CPP),
//            ImmutableList.of(new BuildTargetIdentifier("@com_google_googletest//:gtest_main")),
//            new BuildTargetCapabilities(true, false, true));
//    exampleExampleBuildTarget.setData(exampleExampleCppBuildTarget);
//    exampleExampleBuildTarget.setDataKind(BuildTargetDataKind.CPP);
//
//    WorkspaceBuildTargetsResult expectedWorkspaceBuildTargetsResult =
//        new WorkspaceBuildTargetsResult(ImmutableList.of(exampleExampleBuildTarget));
//
//    return new BazelBspTestScenarioStep(
//        "cpp project",
//        () ->
//            getTestClient()
//                .testWorkspaceTargets(Duration.ofSeconds(20),
// expectedWorkspaceBuildTargetsResult));
//  }
//
//  private BazelBspTestScenarioStep cppOptions() {
//    CppOptionsParams cppOptionsParams =
//        new CppOptionsParams(ImmutableList.of(new BuildTargetIdentifier("//example:example")));
//
//    CppOptionsItem exampleExampleCppOptionsItem =
//        new CppOptionsItem(
//            new BuildTargetIdentifier("//example:example"),
//            ImmutableList.of("-Iexternal/gtest/include"),
//            ImmutableList.of("BOOST_FALLTHROUGH"),
//            ImmutableList.of("-pthread"));
//
//    CppOptionsResult expectedCppOptionsResult =
//        new CppOptionsResult(ImmutableList.of(exampleExampleCppOptionsItem));
//
//    return new BazelBspTestScenarioStep(
//        "cpp options",
//        () ->
//            getTestClient()
//                .testCppOptions(
//                    Duration.ofSeconds(20), cppOptionsParams, expectedCppOptionsResult));
//  }
// }
