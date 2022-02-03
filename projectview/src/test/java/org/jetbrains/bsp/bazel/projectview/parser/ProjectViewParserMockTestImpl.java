package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.vavr.control.Try;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class ProjectViewParserMockTestImpl extends ProjectViewParserImpl {

  @Override
  public ProjectView parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    var projectViewFileContent = readFileContent(projectViewFilePath);
    var defaultProjectViewFileContent = readFileContent(defaultProjectViewFilePath);

    return parse(projectViewFileContent, defaultProjectViewFileContent);
  }

  @Override
  public ProjectView parse(Path projectViewFilePath) {
    var projectViewFileContent = readFileContent(projectViewFilePath);

    return parse(projectViewFileContent);
  }

  private String readFileContent(Path filePath) {
    // we read file content instead of passing plain file due to bazel resources packaging
    var inputStream = ProjectViewParserMockTestImpl.class.getResourceAsStream(filePath.toString());

    return Optional.ofNullable(inputStream).map(this::readInputStream).orElse("");
  }

  private String readInputStream(InputStream inputStream) {
    return Try.success(new InputStreamReader(inputStream, Charsets.UTF_8))
        .mapTry(CharStreams::toString)
        .get();
  }
}
