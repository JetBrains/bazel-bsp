package org.jetbrains.bsp.bazel.projectview.parser;

import org.jetbrains.bsp.bazel.commons.FileUtils;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

import java.nio.file.Path;

/**
 * Project view file parser. Its purpose is to parse *.bazelproject file and create ProjectView
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewParser {

  default ProjectView parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    String projectViewFileContent = FileUtils.readFileContentOrEmpty(projectViewFilePath);
    String defaultProjectViewFileContent = FileUtils.readFileContentOrEmpty(defaultProjectViewFilePath);

    return parse(projectViewFileContent, defaultProjectViewFileContent);
  }

  ProjectView parse(String projectViewFileContent, String defaultProjectViewFileContent);

  default ProjectView parse(Path projectViewFilePath) {
    String projectViewFileContent = FileUtils.readFileContentOrEmpty(projectViewFilePath);

    return parse(projectViewFileContent);
  }

  ProjectView parse(String projectViewFileContent);
}
