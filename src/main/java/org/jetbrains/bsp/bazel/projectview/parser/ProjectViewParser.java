package org.jetbrains.bsp.bazel.projectview.parser;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

import java.io.File;
import java.io.IOException;

public interface ProjectViewParser {

  ProjectView parse(File projectViewFile) throws IOException;

  ProjectView parse(String projectViewFileContent);
}
