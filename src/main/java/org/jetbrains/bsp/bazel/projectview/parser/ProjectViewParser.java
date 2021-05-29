package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

/**
 * Project view file parser. Its purpose is to parse *.bazelproject file and create ProjectView
 *
 * @see org.jetbrains.bsp.bazel.projectview.model.ProjectView
 */
public interface ProjectViewParser {

  ProjectView parse(String projectViewFileContent);

  default ProjectView parse(File projectViewFile) throws IOException {
    String fileContent = Files.asCharSource(projectViewFile, Charset.defaultCharset()).read();

    return parse(fileContent);
  }
}
