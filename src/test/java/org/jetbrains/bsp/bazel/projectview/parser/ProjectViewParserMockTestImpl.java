package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class ProjectViewParserMockTestImpl extends ProjectViewParserImpl {

  @Override
  public ProjectView parse(Path projectViewPath) throws IOException {
    InputStream inputStream =
        ProjectViewParserMockTestImpl.class.getResourceAsStream(projectViewPath.toString());
    // we read file content instead of passing plain file due to bazel resources packaging
    String fileContent = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));

    return parse(fileContent);
  }
}
