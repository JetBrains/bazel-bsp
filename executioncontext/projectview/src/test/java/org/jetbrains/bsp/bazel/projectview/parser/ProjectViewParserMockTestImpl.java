package org.jetbrains.bsp.bazel.projectview.parser;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;

public class ProjectViewParserMockTestImpl extends ProjectViewParserImpl {

  @Override
  public Try<ProjectView> parse(Path projectViewFilePath, Path defaultProjectViewFilePath) {
    return readFileContent(defaultProjectViewFilePath)
        .flatMap(
            defaultProjectViewFileContent ->
                parseWithDefault(projectViewFilePath, defaultProjectViewFileContent));
  }

  private Try<ProjectView> parseWithDefault(
      Path projectViewFilePath, String defaultProjectViewFileContent) {
    return readFileContent(projectViewFilePath)
        .flatMap(
            projectViewFileContent -> parse(projectViewFileContent, defaultProjectViewFileContent))
        .orElse(parse(defaultProjectViewFileContent));
  }

  @Override
  public Try<ProjectView> parse(Path projectViewFilePath) {
    return readFileContent(projectViewFilePath).flatMap(this::parse);
  }

  private Try<String> readFileContent(Path filePath) {
    // we read file content instead of passing plain file due to bazel resources packaging
    var inputStream = ProjectViewParserMockTestImpl.class.getResourceAsStream(filePath.toString());

    return Option.of(inputStream)
        .map(this::readInputStream)
        .getOrElse(Try.failure(new IOException(filePath + " file does not exist!")));
  }

  private Try<String> readInputStream(InputStream inputStream) {
    return Try.success(new InputStreamReader(inputStream, Charsets.UTF_8))
        .mapTry(CharStreams::toString);
  }
}
