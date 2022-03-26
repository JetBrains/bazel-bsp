package org.jetbrains.bsp.bazel;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario;
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep;

public class BazelBspEntireRepositoryImportTest extends BazelBspTestBaseScenario {

  public BazelBspEntireRepositoryImportTest() {
    super();
  }

  // we cannot use `bazel test ...` because test runner blocks bazel daemon,
  // but testing server needs it for queries and etc
  public static void main(String[] args) {
    BazelBspEntireRepositoryImportTest test = new BazelBspEntireRepositoryImportTest();
    test.executeScenario();
  }

  @Override
  protected List<BazelBspTestScenarioStep> getScenarioSteps() {
    return ImmutableList.of(importEntireRepository());
  }

  private BazelBspTestScenarioStep importEntireRepository() {
    return new BazelBspTestScenarioStep(
        "import entire Bazel BSP repository",
        () -> testClient.testResolveProject(Duration.ofMinutes(5)));
  }
}
