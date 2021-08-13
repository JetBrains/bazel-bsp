package org.jetbrains.bsp.bazel.projectview.model;

import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.DirectoriesSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

public class ProjectViewDefaultProvider implements ProjectViewProvider {

  @Override
  public ProjectView create() {
    DirectoriesSection directoriesSection =
        new DirectoriesSection(ImmutableList.of(Paths.get(".")), ImmutableList.of());
    TargetsSection targetsSection =
        new TargetsSection(ImmutableList.of("//..."), ImmutableList.of());

    return ProjectView.builder().directories(directoriesSection).targets(targetsSection).build();
  }
}
