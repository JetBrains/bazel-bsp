package org.jetbrains.bsp.bazel.projectview.parser;

import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

import java.io.File;
import java.io.IOException;

/**
 * Project view file parser.
 * Its purpose is to parse *.bazelproject file and create ProjectView
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewParser {

  ProjectView parse(File projectViewFile) throws IOException;

  ProjectView parse(String projectViewFileContent);
}
